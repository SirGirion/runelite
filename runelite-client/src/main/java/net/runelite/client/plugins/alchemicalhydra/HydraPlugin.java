/*
 * Copyright (c) 2019, Lucas <https://github.com/lucwousin>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.alchemicalhydra;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Alchemical Hydra",
	description = "Show what to pray against hydra",
	tags = {"Hydra", "Lazy", "4 headed asshole"},
	enabledByDefault = false
)
@Slf4j
public class HydraPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	private Map<LocalPoint, Projectile> poisonProjectiles = new HashMap<>();

	@Getter(AccessLevel.PACKAGE)
	private Hydra hydra;

	private boolean inHydraInstance;
	private int lastAttackTick;
	private int currentHydraID;

	private static final int[] HYDRA_REGIONS = {
		5279, 5280,
		5535, 5536
	};

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HydraOverlay overlay;

	@Inject
	private HydraSceneOverlay poisonOverlay;

	public static final int PROJ_HYDRA_MAGIC = 1662;
	public static final int PROJ_HYDRA_RANGED = 1663;
	public static final int PROJ_HYDRA_POISON = 1644;
	public static final int PROJ_HYDRA_LIGHTNING = 1664;
	public static final int PROJ_HYDRA_LIGHTNING_2 = 1665;
	public static final int PROJ_HYDRA_FIRE = 1667;

	static final int ANIM_HYDRA_1_1 = 8237;
	static final int ANIM_HYDRA_1_2 = 8238;
	static final int ANIM_HYDRA_LIGHTNING = 8241;
	static final int ANIM_HYDRA_2_1 = 8244;
	static final int ANIM_HYDRA_2_2 = 8245;
	static final int ANIM_HYDRA_FIRE = 8248;
	static final int ANIM_HYDRA_3_1 = 8251;
	static final int ANIM_HYDRA_3_2 = 8252;
	static final int ANIM_HYDRA_4_1 = 8257;
	static final int ANIM_HYDRA_4_2 = 8258;

	static final int ANIM_SWITCH_ATTACKS_GREEN = 9111;
	static final int ANIM_SWITCH_ATTACKS_BLUE = 9112;
	static final int ANIM_SWITCH_ATTACKS_RED = 9113;

	static final int SOUND_EFFECT_RANGE = 4089;
	static final int SOUND_EFFECT_MAGE = 4103;

	public static final int BIG_ASS_GUTHIX_SPELL = 1774;
	public static final int BIG_SUPERHEAT = 1800;
	public static final int BIG_SPEC_TRANSFER = 1959;

	@Override
	protected void startUp()
	{
		inHydraInstance = checkArea();
		lastAttackTick = -1;
		poisonProjectiles.clear();
	}

	@Override
	protected void shutDown()
	{
		inHydraInstance = false;
		hydra = null;
		poisonProjectiles.clear();
		removeOverlays();
		lastAttackTick = -1;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged state)
	{
		if (state.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		inHydraInstance = checkArea();

		if (!inHydraInstance)
		{

			if (hydra != null)
			{
				removeOverlays();
				hydra = null;
			}
			return;
		}

		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() == NpcID.ALCHEMICAL_HYDRA)
			{
				hydra = new Hydra(npc);
				currentHydraID = NpcID.ALCHEMICAL_HYDRA;
				inHydraInstance = true;
				break;
			}
		}

		addOverlays();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		for (NPC npc : client.getNpcs())
		{
			int id = npc.getId();
			if (id == currentHydraID) return;
			if (id == 8619 || id == 8620 || id == 8621)
			{
				log.debug("onGameTick: switching");
				switch (id)
				{
					case NpcID.ALCHEMICAL_HYDRA_8619:
						hydra.changePhase(HydraPhase.TWO);
						break;
					case NpcID.ALCHEMICAL_HYDRA_8620:
						hydra.changePhase(HydraPhase.THREE);
						break;
					case NpcID.ALCHEMICAL_HYDRA_8621:
						hydra.changePhase(HydraPhase.FOUR);
						break;
				}
				currentHydraID = id;
				log.debug("onGameTick: current = {}", currentHydraID);
			}

		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
//		if (!inHydraInstance || event.getNpc().getId() != NpcID.ALCHEMICAL_HYDRA)
//		{
//			return;
//		}
		if (!inHydraInstance) return;
//		hydra = new Hydra(event.getNpc());
//		addOverlays();
		log.debug("npc {} spawned now switching", event.getNpc().getId());
		switch (event.getNpc().getId())
		{
			case NpcID.ALCHEMICAL_HYDRA:
				log.debug("phase 1");
				hydra = new Hydra(event.getNpc());
				inHydraInstance = true;
				break;
			case NpcID.ALCHEMICAL_HYDRA_8619:
				log.debug("phase 2");
				hydra.changePhase(HydraPhase.TWO);
				break;
			case NpcID.ALCHEMICAL_HYDRA_8620:
				log.debug("phase 3");
				hydra.changePhase(HydraPhase.THREE);
				break;
			case NpcID.ALCHEMICAL_HYDRA_8621:
				log.debug("phase 4");
				hydra.changePhase(HydraPhase.FOUR);
				break;
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().getId() == NpcID.ALCHEMICAL_HYDRA_8621)
		{
			hydra = null;
			poisonProjectiles.clear();
			removeOverlays();
			return;
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		Actor actor = animationChanged.getActor();

		if (!inHydraInstance || hydra == null || actor == client.getLocalPlayer())
		{
			return;
		}

		HydraPhase phase = hydra.getPhase();

		if (actor.getAnimation() == phase.getDeathAnim2() &&
			phase != HydraPhase.THREE  // Else log's gonna say "Tried some weird shit"
			|| actor.getAnimation() == phase.getDeathAnim1() &&
			phase == HydraPhase.THREE) // We want the pray to switch ye ok ty
		{
			log.debug("anim: changed switch");

			switch (phase)
			{
				case ONE:
					hydra.changePhase(HydraPhase.TWO);
					log.debug("switch to phase 2");
					return;
				case TWO:
					hydra.changePhase(HydraPhase.THREE);
					log.debug("switch to phase 3");
					return;
				case THREE:
					hydra.changePhase(HydraPhase.FOUR);
					log.debug("switch to phase 4");
					return;
				case FOUR:
					hydra = null;
					poisonProjectiles.clear();
					removeOverlays();
					return;
				default:
					log.debug("Tried some weird shit");
					break;
			}
		}

		else if (actor.getAnimation() == phase.getSpecAnimationId() && phase.getSpecAnimationId() != 0)
		{
			log.debug("anim: changed setting next special");
			hydra.setNextSpecial(hydra.getNextSpecial() + 9);
		}

		if (poisonProjectiles.isEmpty())
		{
			log.debug("anim: pois empty");
			return;
		}

		Set<LocalPoint> exPoisonProjectiles = new HashSet<>();
		for (Map.Entry<LocalPoint, Projectile> entry : poisonProjectiles.entrySet())
		{
			if (entry.getValue().getEndCycle() < client.getGameCycle())
			{
				log.debug("anim: adding poison projectile");
				exPoisonProjectiles.add(entry.getKey());
			}
		}
		for (LocalPoint toRemove : exPoisonProjectiles)
		{
			log.debug("anim: removing projectile");
			poisonProjectiles.remove(toRemove);
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		if (!inHydraInstance || hydra == null
			|| client.getGameCycle() >= event.getProjectile().getStartCycle())
		{
			return;
		}

		Projectile projectile = event.getProjectile();
		int id = projectile.getId();
		log.debug("got projectile {}", id);

		if (hydra.getPhase().getSpecProjectileId() != 0 && hydra.getPhase().getSpecProjectileId() == id)
		{
			if (hydra.getAttackCount() == hydra.getNextSpecial())
			{
				log.debug("projMoved: setting next special");
				// Only add 9 to next special on the first poison projectile (whoops)
				hydra.setNextSpecial(hydra.getNextSpecial() + 9);
			}
			if (id == PROJ_HYDRA_POISON) poisonProjectiles.put(event.getPosition(), projectile);
		}
		else if (client.getTickCount() != lastAttackTick
			&& (id == Hydra.AttackStyle.MAGIC.getProjectileID() || id == Hydra.AttackStyle.RANGED.getProjectileID()))
		{
			log.debug("projMoved: handling attack");
			hydra.handleAttack(id);
			lastAttackTick = client.getTickCount();
		}
	}

	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		if (!inHydraInstance || hydra == null)
		{
			return;
		}
		int id = event.getSoundId();

		if (client.getTickCount() != lastAttackTick
			&& (id == Hydra.AttackStyle.MAGIC.getSoundID() || id == Hydra.AttackStyle.RANGED.getSoundID()))
		{
			log.debug("soundEffectPlayed: handling attack");
			hydra.handleAttackSound(id);
			lastAttackTick = client.getTickCount();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!event.getMessage().equals("The chemicals neutralise the Alchemical Hydra's defences!"))
		{
			return;
		}

		hydra.setWeakened(true);
	}

	private boolean checkArea()
	{
		return Arrays.equals(client.getMapRegions(), HYDRA_REGIONS) && client.isInInstancedRegion();
	}

	private void addOverlays()
	{
		overlayManager.add(overlay);
		overlayManager.add(poisonOverlay);
	}

	private void removeOverlays()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(poisonOverlay);
	}
}
