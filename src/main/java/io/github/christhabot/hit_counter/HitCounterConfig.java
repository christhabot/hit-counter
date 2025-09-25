package io.github.christhabot.hit_counter;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "hit_counter")
public class HitCounterConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	public boolean onlyPlayers = true;

	@ConfigEntry.Gui.Tooltip
	public boolean showText = true;

	@ConfigEntry.Gui.Tooltip
	public boolean ColorText = true;

	@ConfigEntry.Gui.Tooltip
	public boolean ColorOnBothText = false;

	@ConfigEntry.Gui.Tooltip
	public long disappearAfter = 30L;

	@ConfigEntry.Gui.Tooltip
	public int guiX = 10;

	@ConfigEntry.Gui.Tooltip
	public int guiY = 10;

	@ConfigEntry.Gui.Tooltip
	public String defaultHex = "#AAAAAA";

	@ConfigEntry.Gui.Tooltip
	public String gotHitHex = "#FF4500";

	@ConfigEntry.Gui.Tooltip
	public String didHitHex = "#00BFFF";

	public enum Option {
		HITSTAKEN, HITSDEALT, DAMAGETAKEN, DAMAGEDEALT, NONE
	}

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
	public Option configA = Option.HITSDEALT;

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
	public Option configB = Option.DAMAGEDEALT;

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
	public Option configC = Option.HITSTAKEN;

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
	public Option configD = Option.DAMAGETAKEN;
}
