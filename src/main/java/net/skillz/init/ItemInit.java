package net.skillz.init;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.skillz.SkillZMain;
import net.skillz.content.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ItemInit {

    public static final Item STRANGE_POTION = register("strange_potion", new StrangePotionItem(new Item.Settings().maxCount(1)), ItemGroups.FOOD_AND_DRINK);
    public static final Item RARE_CANDY = register("rare_candy", new RareCandyItem(new Item.Settings()), ItemGroups.TOOLS);
    public static final Item BOOK_OF_KNOWLEDGE = register("book_of_knowledge", new BookOfKnowledgeItem(new FabricItemSettings().maxCount(1)), ItemGroups.TOOLS);

    private static Item register(String id, Item item, RegistryKey<ItemGroup> itemGroup) {
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.add(item));
        return register(SkillZMain.identifierOf(id), item);
    }

    private static Item register(Identifier id, Item item) {
        return Registry.register(Registries.ITEM, id, item);
    }

    public static void init() {
    }
}
