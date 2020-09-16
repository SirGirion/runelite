package net.runelite.client.plugins.gauntlet;

public class GauntletSounds
{
	private static final int HUNLLEF_RANGED = 1965;
	private static final int HUNLLEF_MAGIC_STANDARD = 222;
	private static final int HUNLLEF_MAGIC_SPECIAL = 134;
	private static final int HUNLLEF_TORNADOES = 975;

	protected static boolean isCountableRanged(final int id)
	{
		switch (id)
		{
			case HUNLLEF_RANGED:
			case HUNLLEF_TORNADOES:
				return true;
			default:
				return false;
		}
	}

	protected static boolean isCountableMagic(final int id)
	{
		switch (id)
		{
			case HUNLLEF_MAGIC_STANDARD:
			case HUNLLEF_MAGIC_SPECIAL:
			case HUNLLEF_TORNADOES:
				return true;
			default:
				return false;
		}
	}
}
