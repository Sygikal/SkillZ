package net.skillz.data.populate.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.populate.Populator;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.registry.EnchantmentRegistry;
import net.skillz.registry.EnchantmentZ;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnchantmentPopulator extends Populator {
    public static final Identifier ID = SkillZMain.identifierOf("enchantment_populator");

    private final List<Identifier> enchantBlacklist = Lists.newArrayList();
    //private final ToolSubType subtype;
    private final EnchantAlgorithm algo;
    private final boolean cursed;
    private final boolean treasure;



    public EnchantmentPopulator(EnchantAlgorithm algo, boolean cursed, boolean treasure, JsonArray enchantBlacklist) {
        super(ID);
        this.algo = algo;
        this.cursed = cursed;
        this.treasure = treasure;
        //this.subtype = subtype;
        enchantBlacklist.forEach((elem) -> {this.enchantBlacklist.add(Identifier.tryParse(elem.getAsString()));});
    }

    @Override
    public void populate(JsonArray skillArray) {
        for (Enchantment ench : Registries.ENCHANTMENT) {
            if (ench.isCursed() && !cursed) {
                continue;
            }
            if (ench.isTreasure() && !treasure) {
                continue;
            }
            for (int i = 1; i <= ench.getMaxLevel(); i++) {
                int restriction = Math.round(this.algo.runner.run(ench, i));

                if (!enchantBlacklist.contains(Registries.ENCHANTMENT.getId(ench))) {
                    Map<String, Integer> populatedRestriction = getSkillMap(skillArray, restriction);

                    int enchantmentRawId = EnchantmentRegistry.getId(Registries.ENCHANTMENT.getId(ench), i);

                    if (!populatedRestriction.isEmpty()) {
                        LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentRawId, new PlayerRestriction(enchantmentRawId, populatedRestriction));
                    }
                }
            }
        }
    }

    public enum EnchantAlgorithm {
        MIN_POWER(((ench, level) -> {return ench.getMinPower(level);})),
        MAX_POWER(((ench, level) -> {return ench.getMaxPower(level);})),
        WEIGHT(((ench, level) -> {return ench.getRarity().getWeight();}));

        public final EnchantRunner runner;

        EnchantAlgorithm(EnchantRunner runner) {
            this.runner = runner;
        }
    }

    public interface EnchantRunner {
        float run(Enchantment ench, int level);
    }
}
