package net.skillz.init;

import net.skillz.SkillZMain;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TagInit {

    public static final TagKey<Item> FARM_ITEMS = TagKey.of(RegistryKeys.ITEM, SkillZMain.identifierOf("farm_items"));
    public static final TagKey<Item> RESTRICTED_FURNACE_EXPERIENCE_ITEMS = TagKey.of(RegistryKeys.ITEM, SkillZMain.identifierOf("restricted_furnace_experience_items"));
    public static final TagKey<Item> HIDDEN_RESTRICTION_ITEMS = TagKey.of(RegistryKeys.ITEM, SkillZMain.identifierOf("hidden_restriction_items"));


    public static void init() {
    }
}
