package net.runelite.client.plugins.gauntlet;

public class GauntletProjectiles
{
	private static final int HUNLLEF_RANGED = 1712;
	private static final int HUNLLEF_MAGIC = 1708;
	private static final int HUNLLEF_PRAYER_DISABLE = 1714;

	protected static boolean isCountableRanged(final int id)
	{
		return id == HUNLLEF_RANGED || id == HUNLLEF_PRAYER_DISABLE;
	}

	protected static boolean isCountableMagic(final int id)
	{
		return id == HUNLLEF_MAGIC || id == HUNLLEF_PRAYER_DISABLE;
	}
}
