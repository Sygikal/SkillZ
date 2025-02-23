package net.levelz.init;

import net.levelz.LevelzMain;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class TagInit {

    public static final TagKey<Item> FARM_ITEMS = TagKey.of(RegistryKeys.ITEM, LevelzMain.identifierOf("farm_items"));
    public static final TagKey<Item> RESTRICTED_FURNACE_EXPERIENCE_ITEMS = TagKey.of(RegistryKeys.ITEM, LevelzMain.identifierOf("restricted_furnace_experience_items"));

    public static void init() {
    }
}
