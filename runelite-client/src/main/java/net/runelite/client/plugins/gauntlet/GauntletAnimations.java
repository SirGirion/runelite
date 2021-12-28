package net.runelite.client.plugins.gauntlet;

public class GauntletAnimations
{
	private static final int PLAYER_MELEE_PUNCH = 422;
	private static final int PLAYER_MELEE_KICK = 423;
	private static final int PLAYER_MELEE_SCEPTRE = 401;
	private static final int PLAYER_MELEE_HALBERD = 440;
	private static final int PLAYER_RANGED = 426;
	private static final int PLAYER_MAGIC = 1167;

	protected static boolean isPlayerAttack(final int id)
	{
		switch (id)
		{
			case PLAYER_MELEE_PUNCH:
			case PLAYER_MELEE_KICK:
			case PLAYER_MELEE_SCEPTRE:
			case PLAYER_MELEE_HALBERD:
			case PLAYER_RANGED:
			case PLAYER_MAGIC:
				return true;
			default:
				return false;
		}
	}

	protected static AttackStyle getPlayerAttackStyle(final int id)
	{
		switch (id)
		{
			case PLAYER_MELEE_PUNCH:
			case PLAYER_MELEE_KICK:
			case PLAYER_MELEE_SCEPTRE:
			case PLAYER_MELEE_HALBERD:
				return AttackStyle.MELEE;
			case PLAYER_RANGED:
				return AttackStyle.RANGED;
			case PLAYER_MAGIC:
				return AttackStyle.MAGIC;
			default:
				throw new RuntimeException("Unknown playerAttackStyle " + id);
		}
	}

	protected static int getPlayerAttackSpeed(final int id)
	{
		switch (id)
		{
			case PLAYER_MELEE_PUNCH:
			case PLAYER_MELEE_KICK:
			case PLAYER_MELEE_HALBERD:
			case PLAYER_RANGED:
			case PLAYER_MAGIC:
				return 4;
			case PLAYER_MELEE_SCEPTRE:
				return 5;
			default:
				throw new RuntimeException("Unknown playerAttackSpeed " + id);
		}
	}
}
