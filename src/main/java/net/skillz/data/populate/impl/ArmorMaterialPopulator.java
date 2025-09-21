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
import net.skillz.init.ConfigInit;
import net.skillz.init.LoaderInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.util.FileUtil;
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

    public ArmorMaterialPopulator(JsonArray whitelist, JsonArray blacklist) {
        super(ID);
        whitelist.forEach((elem) -> {this.whitelist.add(elem.getAsString());});
        blacklist.forEach((elem) -> {this.blacklist.add(elem.getAsString());});
    }

    @Override
    public void populate(JsonArray skillArray) {
        for (Item item : Registries.ITEM) {
            if (item instanceof ArmorItem armor) {
                String name = armor.getMaterial().getName();
                if (!getIdBlacklist().contains(Registries.ITEM.getId(item))) {
                    if ((!whitelist.isEmpty() && whitelist.contains(name)) || (!blacklist.isEmpty() && !blacklist.contains(name)) || (blacklist.isEmpty() && whitelist.isEmpty())) {
                        LoaderInit.itemsForRePopulation.computeIfAbsent(armor.getMaterial().getName(), k -> Pair.of(new ArrayList<>(), new ArrayList<>())).getValue().add(armor);

                        for (JsonElement elem : skillArray) {
                            JsonObject obj = elem.getAsJsonObject();
                            String skillKey = obj.get("skill").getAsString();
                            String formula = obj.get("formula").getAsString();

                            if (LevelManager.SKILLS.containsKey(skillKey)) {
                                LoaderInit.itemsForRePopulation.computeIfAbsent(armor.getMaterial().getName(), k -> Pair.of(new ArrayList<>(), new ArrayList<>())).getKey().add(Pair.of(formula, skillKey));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postPopulate() {
        for (Map.Entry<String, Pair<List<Pair<String, String>>, List<Item>>> stringListEntry : LoaderInit.itemsForRePopulation.entrySet()) {
            Map<String, Integer> populatedRestriction = new HashMap<>();

            int totalProtection = 0;
            float totalToughness = 0;
            float totalKnockbackRes = 0;
            int setPieces = 0;
            for (Item item : stringListEntry.getValue().getValue()) {
                if (item instanceof ArmorItem armor) {
                    totalProtection += armor.getProtection();
                    totalToughness += armor.getToughness();
                    totalKnockbackRes += armor.getMaterial().getKnockbackResistance();
                    setPieces += 1;
                }
            }

            for (Pair<String, String> pair : stringListEntry.getValue().getKey()) {
                if (LevelManager.SKILLS.containsKey(pair.getRight())) {
                    String formula = pair.getLeft();
                    formula = formula.replace("SKILL_MAX", String.valueOf(LevelManager.SKILLS.get(pair.getRight()).maxLevel()));
                    formula = formula.replace("TOTAL_PROT", String.valueOf(totalProtection));
                    formula = formula.replace("TOTAL_TOUGH", String.valueOf(totalToughness));
                    formula = formula.replace("TOTAL_KB_RES", String.valueOf(totalKnockbackRes));
                    formula = formula.replace("PIECES", String.valueOf(setPieces));

                    int requirement = Math.round((float) FileUtil.evaluateFormula(formula));
                    if (requirement > 0) {
                        populatedRestriction.put(pair.getRight(), requirement);
                    }
                }
            }


            if (!populatedRestriction.isEmpty()) {
                for (Item item : stringListEntry.getValue().getValue()) {
                    if (item instanceof ArmorItem) {
                        if (LevelManager.ITEM_RESTRICTIONS.get(Registries.ITEM.getRawId(item)) == null || ConfigInit.MAIN.PROGRESSION.populatorOverride) {
                            LevelManager.ITEM_RESTRICTIONS.put(Registries.ITEM.getRawId(item), new PlayerRestriction(Registries.ITEM.getRawId(item), populatedRestriction));
                        }
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
}
