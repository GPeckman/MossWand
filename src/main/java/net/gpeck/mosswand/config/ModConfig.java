package net.gpeck.mosswand.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "mosswand")
public class ModConfig implements ConfigData {
	
	@Comment("Does the Moss Wand consume bone meal [default = true]")
	public boolean useBoneMeal = true;
	
	public static ModConfig get() {
		return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
	}
}
