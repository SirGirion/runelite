package net.runelite.client.plugins.gauntlet;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.NpcID;

class Hunllef
{
	static final Set<Integer> NPC_IDS = ImmutableSet.of(
		NpcID.CRYSTALLINE_HUNLLEF, NpcID.CRYSTALLINE_HUNLLEF_9022,
		NpcID.CRYSTALLINE_HUNLLEF_9023, NpcID.CRYSTALLINE_HUNLLEF_9024,
		NpcID.CORRUPTED_HUNLLEF, NpcID.CORRUPTED_HUNLLEF_9036,
		NpcID.CORRUPTED_HUNLLEF_9037, NpcID.CORRUPTED_HUNLLEF_9038
	);

	static final int TILE_SIZE = 5;

	static final int ATTACK_SWITCH_INTERVAL = 4;
	static final int PRAYER_SWITCH_INTERVAL = 6;

	@Getter
	private final NPC npc;
	@Getter
	private boolean magicNext = false;
	@Getter
	private int attackCount = 0;

	@Getter
	private AttackStyle lastTickOverhead = null;
	@Getter
	private AttackStyle overhead = null;
	@Getter
	private int playerAttackCount = 0;

	@Getter
	private int playerCorrectAttacks = 0;
	@Getter
	private int playerTotalAttacks = 0;
	@Getter
	private int playerDamageTaken = 0;
	@Getter
	private int playerAttackTicks = 0;
	@Getter
	private int playerTotalTicks = 0;

	Hunllef(final NPC npc)
	{
		this.npc = npc;
	}

	private AttackStyle getCurrentOverhead()
	{
		final NPCComposition comp = npc.getComposition();
		if (comp == null)
		{
			return null;
		}

		// TODO: Can this be fixed
		final HeadIcon icon = null;
		if (icon == null)
		{
			return null;
		}

		switch (icon)
		{
			case MELEE:
				return AttackStyle.MELEE;
			case RANGED:
				return AttackStyle.RANGED;
			case MAGIC:
				return AttackStyle.MAGIC;
			default:
				throw new RuntimeException("Unknown head icon " + icon);
		}
	}

	void updateOverheads()
	{
		lastTickOverhead = overhead;
		overhead = getCurrentOverhead();

		// Reset player attack count when the overhead changes
		if (overhead != null && overhead != lastTickOverhead)
		{
			playerAttackCount = 0;
		}
	}

	void countHunllefAttack()
	{
		attackCount++;

		// Check for style switch next attack
		if (attackCount >= ATTACK_SWITCH_INTERVAL)
		{
			magicNext = !magicNext;
			attackCount = 0;
		}
	}

	void countCorrectAttack(final int id)
	{
		playerAttackCount++;
		playerCorrectAttacks++;
		playerAttackTicks += GauntletAnimations.getPlayerAttackSpeed(id);
	}

	void countPlayerAttack()
	{
		playerTotalAttacks++;
	}

	void countPlayerDamage(final int damage)
	{
		playerDamageTaken += damage;
	}

	void countTick()
	{
		playerTotalTicks++;
	}
}
