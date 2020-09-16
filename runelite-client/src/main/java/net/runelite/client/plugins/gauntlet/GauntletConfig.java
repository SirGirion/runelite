package net.runelite.client.plugins.gauntlet;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("gauntlet")
public interface GauntletConfig extends Config
{
	@ConfigItem(
		position = 1,
		keyName = "showHunllefArea",
		name = "Hunllef area marker",
		description = "Displays tiles underneath the Hunllef"
	)
	default boolean showHunllefArea()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "hunllefAreaColor",
		name = "Hunllef area color",
		description = "Area marker color underneath the Hunllef"
	)
	default Color hunllefAreaColor()
	{
		return Color.WHITE;
	}

	@ConfigItem(
		position = 3,
		keyName = "showHunllefCount",
		name = "Hunllef attack counter",
		description = "Displays boss attacks until style change"
	)
	default boolean showHunllefCount()
	{
		return true;
	}

	@ConfigItem(
		position = 4,
		keyName = "hunllefRangedColor",
		name = "Ranged attack color",
		description = "Counter color when Hunllef will use ranged"
	)
	default Color hunllefRangedColor()
	{
		return new Color(26, 173, 26);
	}

	@ConfigItem(
		position = 5,
		keyName = "hunllefMagicColor",
		name = "Magic attack color",
		description = "Counter color when Hunllef will use magic"
	)
	default Color hunllefMagicColor()
	{
		return new Color(32, 145, 231);
	}

	@ConfigItem(
		position = 6,
		keyName = "hunllefStyleChangeSound",
		name = "Hunllef style change sound",
		description = "Plays a sound effect when the Hunllef's style will change"
	)
	default SoundMode hunllefStyleChangeSound()
	{
		return SoundMode.ZERO;
	}

	@ConfigItem(
		position = 7,
		keyName = "showPlayerCount",
		name = "Player attack counter",
		description = "Displays player attacks until boss prayer will change"
	)
	default boolean showPlayerCount()
	{
		return true;
	}

	@ConfigItem(
		position = 8,
		keyName = "playerCountColor",
		name = "Player count color",
		description = "Player attack counter color"
	)
	default Color playerCountColor()
	{
		return new Color(227, 196, 20);
	}

	@ConfigItem(
		position = 9,
		keyName = "playerStyleChangeSound",
		name = "Player style change sound",
		description = "Plays a sound effect when the players's style must change"
	)
	default SoundMode playerStyleChangeSound()
	{
		return SoundMode.OFF;
	}

	@ConfigItem(
		position = 10,
		keyName = "showFightStats",
		name = "Post-fight stats",
		description = "Adds a game message with attack counts and total damage taken"
	)
	default boolean showFightStats()
	{
		return true;
	}

	@ConfigItem(
		position = 10,
		keyName = "showResources",
		name = "Show resource locations",
		description = "Shows the clickbox and corresponding icon for resource spots"
	)
	default boolean showResources()
	{
		return true;
	}
}
