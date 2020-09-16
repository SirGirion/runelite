package net.runelite.client.plugins.gauntlet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;

public class GauntletDynamicOverlay extends Overlay
{
	private final Client client;
	private final GauntletPlugin plugin;
	private final GauntletConfig config;

	@Inject
	private GauntletDynamicOverlay(Client client, GauntletPlugin plugin, GauntletConfig config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	private Point locate(Actor actor)
	{
		if (actor == null)
		{
			return null;
		}

		final LocalPoint local = actor.getLocalLocation();
		if (local == null)
		{
			return null;
		}

		return Perspective.localToCanvas(client, local, client.getPlane(), 0);
	}

	private static void drawPie(Graphics2D graphics, Point location, Color fill, int num, int denom)
	{
		if (num < 0)
		{
			num = 0;
		}
		ProgressPieComponent pie = new ProgressPieComponent();
		pie.setFill(fill);
		pie.setBorderColor(num <= 1 ? Color.RED : Color.BLACK);
		pie.setPosition(location);
		pie.setProgress((double) num / denom);
		pie.render(graphics);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Hunllef hunllef = plugin.getHunllef();
		if (hunllef == null)
		{
			return null;
		}

		final NPC hunllefNpc = hunllef.getNpc();

		if (config.showHunllefArea())
		{
			final LocalPoint location = hunllefNpc.getLocalLocation();
			if (location != null)
			{
				final Polygon area = Perspective.getCanvasTileAreaPoly(client, location, Hunllef.TILE_SIZE);
				if (area != null)
				{
					OverlayUtil.renderPolygon(graphics, area, config.hunllefAreaColor());
				}
			}
		}

		if (config.showHunllefCount())
		{
			final int hunllefAttackCount = hunllef.getAttackCount();

			final Color hunllefStyle = hunllef.isMagicNext() ? config.hunllefMagicColor() : config.hunllefRangedColor();

			final Point location = locate(hunllefNpc);
			if (location != null)
			{
				drawPie(graphics, location, hunllefStyle,
					(Hunllef.ATTACK_SWITCH_INTERVAL - hunllefAttackCount), Hunllef.ATTACK_SWITCH_INTERVAL);
			}
		}

		if (config.showPlayerCount() && plugin.inBossFight())
		{
			final int playerAttackCount = hunllef.getPlayerAttackCount();

			final Point location = locate(client.getLocalPlayer());
			if (location != null)
			{
				drawPie(graphics, location, config.playerCountColor(),
					(Hunllef.PRAYER_SWITCH_INTERVAL - playerAttackCount), Hunllef.PRAYER_SWITCH_INTERVAL);
			}
		}

		return null;
	}
}
