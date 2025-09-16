package net.skillz.data.populate.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.populate.Populator;
import net.skillz.init.LoaderInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ArmorMaterialPopulator extends Populator {
    public static final Identifier ID = SkillZMain.identifierOf("armor_material");

    private final List<String> whitelist = Lists.newArrayList();
    private final List<String> blacklist = Lists.newArrayList();
    private final List<Identifier> itemBlacklist = Lists.newArrayList();


    public ArmorMaterialPopulator(JsonArray whitelist, JsonArray blacklist, JsonArray itemBlacklist) {
        super(ID);
        whitelist.forEach((elem) -> {this.whitelist.add(elem.getAsString());});
        blacklist.forEach((elem) -> {this.blacklist.add(elem.getAsString());});
        itemBlacklist.forEach((elem) -> {this.itemBlacklist.add(Identifier.tryParse(elem.getAsString()));});
    }

    @Override
    public void populate(JsonArray skillArray) {
        for (Item item : Registries.ITEM) {
            if (item instanceof ArmorItem armor) {
                String name = armor.getMaterial().getName();
                if (!itemBlacklist.contains(Registries.ITEM.getId(item))) {
                    if ((!whitelist.isEmpty() && whitelist.contains(name)) || (!blacklist.isEmpty() && !blacklist.contains(name)) || (blacklist.isEmpty() && whitelist.isEmpty())) {
                        LoaderInit.itemsForRePopulation.computeIfAbsent(armor.getMaterial().getName(), k -> Pair.of(new ArrayList<>(), new ArrayList<>())).getValue().add(armor);

                        for (JsonElement elem : skillArray) {
                            JsonObject obj = elem.getAsJsonObject();
                            String skillKey = obj.get("skill").getAsString();
                            int base = obj.get("base").getAsInt();
                            int multiply = OptionalObject.get(obj, "multiply", 1).getAsInt();
                            if (LevelManager.SKILLS.containsKey(skillKey)) {
                                LoaderInit.itemsForRePopulation.computeIfAbsent(armor.getMaterial().getName(), k -> Pair.of(new ArrayList<>(), new ArrayList<>())).getKey().add(Pair.of(Pair.of(base, multiply), skillKey));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postPopulate() {
        for (Map.Entry<String, Pair<List<Pair<Pair<Integer, Integer>, String>>, List<Item>>> stringListEntry : LoaderInit.itemsForRePopulation.entrySet()) {
            Map<String, Integer> populatedRestriction = new HashMap<>();

            float averageProtection = 0;
            for (Item item : stringListEntry.getValue().getValue()) {
                if (item instanceof ArmorItem armor) {
                    averageProtection += armor.getProtection() + armor.getToughness();
                }
            }

            int restrict = Math.round(averageProtection / 4);
            for (Pair<Pair<Integer, Integer>, String> pair : stringListEntry.getValue().getKey()) {
                if (LevelManager.SKILLS.containsKey(pair.getValue())) {
                    populatedRestriction.put(pair.getValue(), pair.getKey().getKey() + (restrict * pair.getKey().getValue()));
                }
            }

            for (Item item : stringListEntry.getValue().getValue()) {
                if (item instanceof ArmorItem) {
                    LevelManager.ITEM_RESTRICTIONS.put(Registries.ITEM.getRawId(item), new PlayerRestriction(Registries.ITEM.getRawId(item), populatedRestriction));
                }
            }

            String keys = "[" +
                    populatedRestriction.entrySet()
                            .stream()
                            .map(e -> e.getKey() + " " + e.getValue())
                            .collect(Collectors.joining(", ")) + "]";

            String items = "[" +
                    stringListEntry.getValue().getValue()
                            .stream()
                            .map(Item::toString)
                            .collect(Collectors.joining(", ")) + "]";
            SkillZMain.LOGGER.info("Populating item {} to {}", items, keys);
        }
    }
}
