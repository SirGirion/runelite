package net.runelite.client.plugins.gauntlet;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.ObjectID;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.SoundEffectID;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.Varbits;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectChanged;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Gauntlet",
	description = "Helpful hunleffOverlay for completing the Gauntlet",
	tags = {"gauntlet", "corrupted", "hunllef", "boss"},
	enabledByDefault = false
)
public class GauntletPlugin extends Plugin
{
	protected static final int HUNLLEF_ATTACK_SWITCH_SOUND = SoundEffectID.GE_COIN_TINKLE;
	protected static final int HUNLLEF_PRAYER_SWITCH_SOUND = 2277; // Inventory Full

	private static final int COOLDOWN_ENTER = 5;

	private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.#");

	@Getter
	private Hunllef hunllef = null;

	private int inGameBit = 0;

	@Getter(AccessLevel.PACKAGE)
	private boolean inGauntlet;

	private int cooldownTicks = 0;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SkillIconManager iconManager;

	@Inject
	private GauntletConfig config;

	@Inject
	private GauntletDynamicOverlay hunleffOverlay;

	@Inject
	private GauntletOverlay overlay;

	private static final Set<Integer> FISH_SPOTS = ImmutableSet.of(ObjectID.FISHING_SPOT_35971, ObjectID.FISHING_SPOT_36068);
	private static final Set<Integer> WOOL_SPOTS = ImmutableSet.of(ObjectID.LINUM_TIRINUM, ObjectID.LINUM_TIRINUM_36072);
	private static final Set<Integer> WOOD_SPOTS = ImmutableSet.of(ObjectID.PHREN_ROOTS, ObjectID.PHREN_ROOTS_36066);
	private static final Set<Integer> ORE_SPOTS = ImmutableSet.of(ObjectID.CORRUPT_DEPOSIT, ObjectID.CRYSTAL_DEPOSIT);
	private static final Set<Integer> HERB_SPAWNS = ImmutableSet.of(ObjectID.GRYM_ROOT, ObjectID.GRYM_ROOT_36070);

	private BufferedImage fishIcon;
	private BufferedImage woolIcon;
	private BufferedImage woodIcon;
	private BufferedImage oreIcon;
	private BufferedImage herbIcon;

	@Getter(AccessLevel.PACKAGE)
	private final Map<TileObject, BufferedImage> resources = new HashMap<>();

	@Provides
	GauntletConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletConfig.class);
	}


	private void reset()
	{
		hunllef = null;
		resources.clear();
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(hunleffOverlay);
		overlayManager.add(overlay);
		fishIcon = iconManager.getSkillImage(Skill.FISHING);
		woolIcon = iconManager.getSkillImage(Skill.FARMING);
		woodIcon = iconManager.getSkillImage(Skill.WOODCUTTING);
		oreIcon = iconManager.getSkillImage(Skill.MINING);
		herbIcon = iconManager.getSkillImage(Skill.HERBLORE);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(hunleffOverlay);
		overlayManager.remove(overlay);
		reset();
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		switch (event.getGameState())
		{
			case LOGGING_IN:
			case HOPPING:
				reset();
			case LOADING:
				resources.clear();
		}
	}

	private void playSound(final int id)
	{
		clientThread.invoke(() -> client.playSoundEffect(id));
	}

	protected boolean inBossFight()
	{
		if (hunllef == null)
		{
			return false;
		}

		if (cooldownTicks > 0)
		{
			return false;
		}

		// Timer disappears before fighting the boss
		final Widget timer = client.getWidget(WidgetInfo.GAUNTLET_TIMER_CONTAINER);
		return (timer != null) && (timer.getText().equalsIgnoreCase("final encounter"));
	}

	@Subscribe
	public void onVarbitChanged(final VarbitChanged event)
	{
		final int inGame = client.getVar(Varbits.IN_GAUNTLET);

		if (inGame != inGameBit && inGame == 1)
		{
			cooldownTicks = COOLDOWN_ENTER;
		}

		inGameBit = inGame;
		inGauntlet = inGameBit == 1;
	}

	@Subscribe
	public void onGameTick(final GameTick event)
	{
		if (hunllef != null)
		{
			hunllef.updateOverheads();
		}

		if (cooldownTicks > 0)
		{
			cooldownTicks--;
		}

		if (inBossFight())
		{
			hunllef.countTick();
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(final SoundEffectPlayed event)
	{
		if (!inBossFight())
		{
			return;
		}

		final int id = event.getSoundId();

		// Hunllef attacks
		if (GauntletSounds.isCountableRanged(id) || GauntletSounds.isCountableMagic(id))
		{
			if (hunllef.isMagicNext() ? GauntletSounds.isCountableMagic(id) : GauntletSounds.isCountableRanged(id))
			{
				hunllef.countHunllefAttack();

				final SoundMode mode = config.hunllefStyleChangeSound();
				final int attackCount = hunllef.getAttackCount();

				if (mode == SoundMode.ONE && attackCount == (Hunllef.ATTACK_SWITCH_INTERVAL - 1))
				{
					playSound(HUNLLEF_ATTACK_SWITCH_SOUND);
				}
				else if (mode == SoundMode.ZERO && attackCount == 0)
				{
					playSound(HUNLLEF_ATTACK_SWITCH_SOUND);
				}
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!inBossFight())
		{
			return;
		}

		final Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer != event.getActor())
		{
			return;
		}

		final int id = localPlayer.getAnimation();

		// Player attacks
		if (GauntletAnimations.isPlayerAttack(id))
		{
			hunllef.countPlayerAttack();

			final AttackStyle style = GauntletAnimations.getPlayerAttackStyle(id);
			if (style != hunllef.getLastTickOverhead())
			{
				hunllef.countCorrectAttack(id);

				final SoundMode mode = config.playerStyleChangeSound();
				final int attackCount = hunllef.getPlayerAttackCount();

				if (mode == SoundMode.ONE && attackCount == (Hunllef.PRAYER_SWITCH_INTERVAL - 1))
				{
					playSound(HUNLLEF_PRAYER_SWITCH_SOUND);
				}
				else if (mode == SoundMode.ZERO && attackCount == Hunllef.PRAYER_SWITCH_INTERVAL)
				{
					playSound(HUNLLEF_PRAYER_SWITCH_SOUND);
				}
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (event.getActor() != client.getLocalPlayer())
		{
			return;
		}

		if (hunllef == null || !inBossFight())
		{
			return;
		}

		final int damage = event.getHitsplat().getAmount();
		hunllef.countPlayerDamage(damage);
	}

	@Subscribe
	public void onNpcSpawned(final NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final int id = npc.getId();

		if (Hunllef.NPC_IDS.contains(id))
		{
			hunllef = new Hunllef(npc);
		}
	}

	@Subscribe
	public void onNpcDespawned(final NpcDespawned event)
	{
		if (hunllef == null)
		{
			return;
		}

		final NPC npc = event.getNpc();

		if (npc != null && npc == hunllef.getNpc())
		{
			if (config.showFightStats() && hunllef.getPlayerTotalAttacks() > 0)
			{
				announceStats();
			}

			reset();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		if (!inGauntlet)
		{
			return;
		}

		onTileObject(event.getTile(), null, event.getGameObject());
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged event)
	{
		if (!inGauntlet)
		{
			return;
		}

		onTileObject(event.getTile(), event.getPrevious(), event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		if (!inGauntlet)
		{
			return;
		}

		onTileObject(event.getTile(), event.getGameObject(), null);
	}

	private void onTileObject(Tile tile, TileObject oldObject, TileObject newObject)
	{
		resources.remove(oldObject);

		if (newObject == null)
		{
			return;
		}
		int id = newObject.getId();
		if (FISH_SPOTS.contains(id))
		{
			resources.put(newObject, fishIcon);
		}
		else if (WOOL_SPOTS.contains(id))
		{
			resources.put(newObject, woolIcon);
		}
		else if (WOOD_SPOTS.contains(id))
		{
			resources.put(newObject, woodIcon);
		}
		else if (ORE_SPOTS.contains(id))
		{
			resources.put(newObject, oreIcon);
		}
		else if (HERB_SPAWNS.contains(id))
		{
			resources.put(newObject, herbIcon);
		}
	}

	private void announceStats()
	{
		if (hunllef == null)
		{
			return;
		}

		final String chatMessage = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Correct attacks: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(Integer.toString(hunllef.getPlayerCorrectAttacks()))
			.append("/")
			.append(Integer.toString(hunllef.getPlayerTotalAttacks()))
			.append(ChatColorType.NORMAL)
			.append(" (")
			.append(ChatColorType.HIGHLIGHT)
			.append(ONE_DECIMAL.format((double) hunllef.getPlayerAttackTicks() / hunllef.getPlayerTotalTicks() * 100))
			.append("%")
			.append(ChatColorType.NORMAL)
			.append(" efficiency)")
			.append(", Damage taken: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(Integer.toString(hunllef.getPlayerDamageTaken()))
			.build();

		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.CONSOLE)
			.runeLiteFormattedMessage(chatMessage)
			.build());
	}
}