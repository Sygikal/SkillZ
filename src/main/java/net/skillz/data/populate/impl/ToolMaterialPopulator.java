package net.skillz.data.populate.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.populate.Populator;
import net.skillz.init.ConfigInit;
import net.skillz.init.LoaderInit;
import net.skillz.init.TagInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ToolMaterialPopulator extends Populator {
    public static final Identifier ID = SkillZMain.identifierOf("tool_material");

    private final ToolSubType subtype;

    public ToolMaterialPopulator(ToolSubType subtype) {
        super(ID);
        this.subtype = subtype;
    }

    @Override
    public void populate(JsonArray skillArray) {
        for (Item item : Registries.ITEM) {
            if (item instanceof ToolItem tool) {
                if (subtype.equals(ToolSubType.ALL) ||
                        ((tool instanceof PickaxeItem && subtype.equals(ToolSubType.PICKAXE)) ||
                        (tool instanceof SwordItem && subtype.equals(ToolSubType.SWORD)) ||
                        (tool instanceof AxeItem && subtype.equals(ToolSubType.AXE)) ||
                        (tool instanceof HoeItem && subtype.equals(ToolSubType.HOE)) ||
                        (tool instanceof ShovelItem && subtype.equals(ToolSubType.SHOVEL)))) {
                    if (!getIdBlacklist().contains(Registries.ITEM.getId(item))) {
                        Map<Identifier, Integer> populatedRestriction = getSkillMap(skillArray, Registries.ITEM.getId(item), formula -> {
                            return formula.
                                    replace("MINING_LEVEL", String.valueOf(ToolAlgorithm.MINING_LEVEL.runner.run(tool.getMaterial()))).
                                    replace("ATTACK_DAMAGE", String.valueOf(ToolAlgorithm.ATTACK_DAMAGE.runner.run(tool.getMaterial()))).
                                    replace("ENCHANTABILITY", String.valueOf(ToolAlgorithm.ENCHANTABILITY.runner.run(tool.getMaterial()))).
                                    replace("MINING_SPEED", String.valueOf(ToolAlgorithm.MINING_SPEED.runner.run(tool.getMaterial())));

                        });

                        if (!populatedRestriction.isEmpty()) {
                            int rawId = Registries.ITEM.getRawId(item);
                            boolean hidden = item.getDefaultStack().isIn(TagInit.HIDDEN_RESTRICTION_ITEMS);

                            PlayerRestriction restriction = new PlayerRestriction(rawId, populatedRestriction);
                            restriction.setHidden(hidden);
                            if (LevelManager.ITEM_RESTRICTIONS.get(rawId) == null || ConfigInit.MAIN.PROGRESSION.POPULATION.populatorOverride) {
                                LevelManager.ITEM_RESTRICTIONS.put(rawId, restriction);
                            }
                        }
                    }
                }
            }
        }
    }

    public enum ToolSubType {
        ALL,
        PICKAXE,
        AXE,
        SWORD,
        SHOVEL,
        HOE;
    }

    public enum ToolAlgorithm {
        MINING_LEVEL(ToolMaterial::getMiningLevel),
        ATTACK_DAMAGE(ToolMaterial::getAttackDamage),
        ENCHANTABILITY((mat) -> {return (float) mat.getEnchantability() / 2;}),
        MINING_SPEED(ToolMaterial::getMiningSpeedMultiplier);

        public final ToolRunner runner;

        ToolAlgorithm(ToolRunner runner) {
            this.runner = runner;
        }
    }

    public interface ToolRunner {
        float run(ToolMaterial material);
    }
}
