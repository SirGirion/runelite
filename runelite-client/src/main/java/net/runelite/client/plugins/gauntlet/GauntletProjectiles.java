package net.runelite.client.plugins.gauntlet;

public class GauntletProjectiles
{
	private static final int HUNLLEF_RANGE = 1711;
	private static final int CORRUPTED_HUNLLEF_RANGED = 1712;
	private static final int HUNLLEF_MAGIC = 1707;
	private static final int CORRUPTED_HUNLLEF_MAGIC = 1708;
	private static final int HUNLLEF_PRAYER_DISABLE = 1713;
	private static final int CORRUPTED_HUNLLEF_PRAYER_DISABLE = 1714;

	protected static boolean isCountableRanged(final int id)
	{
		return id == HUNLLEF_RANGE || id == CORRUPTED_HUNLLEF_RANGED;
	}

	protected static boolean isCountableMagic(final int id)
	{
		return id == HUNLLEF_MAGIC || id == HUNLLEF_PRAYER_DISABLE || id == CORRUPTED_HUNLLEF_MAGIC || id == CORRUPTED_HUNLLEF_PRAYER_DISABLE;
	}
}
