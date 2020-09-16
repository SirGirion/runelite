package net.runelite.client.plugins.gauntlet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SoundMode
{
	ZERO("0"),
	ONE("1"),
	OFF("Off");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
