package net.gpeck.mosswand;

import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.gpeck.mosswand.item.*;
import net.gpeck.mosswand.config.ModConfig;

public class MossWand implements ModInitializer {
	public static final MossWandItem MOSS_WAND = new MossWandItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));
	public static final CrackedWandItem CRACKED_WAND = new CrackedWandItem(new FabricItemSettings().group(ItemGroup.TOOLS).maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("moss_wand", "moss_wand_item"), MOSS_WAND);
		Registry.register(Registry.ITEM, new Identifier("moss_wand", "cracked_wand_item"), CRACKED_WAND);

		AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
	}
}
