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


public class ToolMaterialPopulator extends Populator {
    public static final Identifier ID = SkillZMain.identifierOf("tool_material");

    private final List<Identifier> itemBlacklist = Lists.newArrayList();
    private final ToolSubType subtype;
    private final ToolAlgorithm algo;


    public ToolMaterialPopulator(ToolAlgorithm algo, ToolSubType subtype, JsonArray itemBlacklist) {
        super(ID);
        this.algo = algo;
        this.subtype = subtype;
        itemBlacklist.forEach((elem) -> {this.itemBlacklist.add(Identifier.tryParse(elem.getAsString()));});
    }

    @Override
    public void populate(JsonArray skillArray) {
        for (Item item : Registries.ITEM) {
            if (item instanceof ToolItem tool) {
                int restriction = Math.round(this.algo.runner.run(tool.getMaterial()));

                if (subtype.equals(ToolSubType.ALL) ||
                        ((tool instanceof PickaxeItem && subtype.equals(ToolSubType.PICKAXE)) ||
                        (tool instanceof SwordItem && subtype.equals(ToolSubType.SWORD)) ||
                        (tool instanceof AxeItem && subtype.equals(ToolSubType.AXE)) ||
                        (tool instanceof HoeItem && subtype.equals(ToolSubType.HOE)) ||
                        (tool instanceof ShovelItem && subtype.equals(ToolSubType.SHOVEL)))) {
                    if (!itemBlacklist.contains(Registries.ITEM.getId(item))) {
                        Map<String, Integer> populatedRestriction = getSkillMap(skillArray, restriction);

                        if (!populatedRestriction.isEmpty()) {
                            LevelManager.ITEM_RESTRICTIONS.put(Registries.ITEM.getRawId(item), new PlayerRestriction(Registries.ITEM.getRawId(item), populatedRestriction));
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
