package net.skillz.data.populate.impl;

import com.google.gson.JsonArray;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.populate.Populator;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.content.registry.EnchantmentRegistry;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;


public class EnchantmentPopulator extends Populator {
    public static final Identifier ID = SkillZMain.identifierOf("enchantment_populator");

    private final boolean cursed;
    private final boolean treasure;

    public EnchantmentPopulator(boolean cursed, boolean treasure) {
        super(ID);
        this.cursed = cursed;
        this.treasure = treasure;
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
                if (!getIdBlacklist().contains(Registries.ENCHANTMENT.getId(ench))) {
                    int finalI = i;
                    Map<String, Integer> populatedRestriction = getSkillMap(skillArray, Registries.ENCHANTMENT.getId(ench), formula -> {
                        return formula.
                                replace("MIN_POWER", String.valueOf(EnchantAlgorithm.MIN_POWER.runner.run(ench, finalI))).
                                replace("MAX_POWER", String.valueOf(EnchantAlgorithm.MAX_POWER.runner.run(ench, finalI))).
                                replace("WEIGHT", String.valueOf(EnchantAlgorithm.WEIGHT.runner.run(ench, finalI)));
                    });

                    int enchantmentRawId = EnchantmentRegistry.getId(Registries.ENCHANTMENT.getId(ench), i);

                    if (!populatedRestriction.isEmpty()) {
                        if (LevelManager.ENCHANTMENT_RESTRICTIONS.get(enchantmentRawId) == null || ConfigInit.MAIN.PROGRESSION.populatorOverride) {
                            LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentRawId, new PlayerRestriction(enchantmentRawId, populatedRestriction));
                        }
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
