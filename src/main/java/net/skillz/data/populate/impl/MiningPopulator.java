package net.skillz.data.populate.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
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


public class MiningPopulator extends Populator {
    public static final Identifier ID = SkillZMain.identifierOf("mining_populator");

    private final List<String> tagWhitelist = Lists.newArrayList();
    private final List<String> tagBlacklist = Lists.newArrayList();
    private final List<Identifier> blockBlacklist = Lists.newArrayList();
    private final int minCutoff;
    private final int maxCutoff;

    public MiningPopulator(JsonArray tagWhitelist, JsonArray tagBlacklist, JsonArray blockBlacklist, Integer minCutoff, Integer maxCutoff) {
        super(ID);
        tagWhitelist.forEach((elem) -> {this.tagWhitelist.add(elem.getAsString());});
        tagBlacklist.forEach((elem) -> {this.tagBlacklist.add(elem.getAsString());});
        this.minCutoff = minCutoff;
        this.maxCutoff = maxCutoff;
        blockBlacklist.forEach((elem) -> {this.blockBlacklist.add(Identifier.tryParse(elem.getAsString()));});
    }

    @Override
    public void populate(JsonArray skillArray) {

        for (Block block : Registries.BLOCK) {
            if (!blockBlacklist.contains(Registries.BLOCK.getId(block)) && block.getHardness() > minCutoff && block.getHardness() < maxCutoff) {

                //LoaderInit.blockForRePopulation.computeIfAbsent(block, k -> new ArrayList<>()).add();

                Map<String, Integer> populatedRestriction = getSkillMap(skillArray, formula -> {
                    return formula.
                            replace("RESISTANCE", String.valueOf(BlockAlgorithm.RESISTANCE.runner.run(block))).
                            replace("HARDNESS", String.valueOf(BlockAlgorithm.HARDNESS.runner.run(block)));
                });

                for (Map.Entry<String, Integer> entry : populatedRestriction.entrySet()) {
                    LoaderInit.blockForRePopulation.computeIfAbsent(block, k -> new ArrayList<>()).add(Pair.of(entry.getKey(), entry.getValue()));
                }



                //LevelManager.MINING_RESTRICTIONS.put(Registries.BLOCK.getRawId(block), new PlayerRestriction(Registries.BLOCK.getRawId(block), populatedRestriction));
            }
        }
    }

    @Override
    public void postPopulate() {
        for (Map.Entry<Block, List<Pair<String, Integer>>> entry : LoaderInit.blockForRePopulation.entrySet()) {
            boolean skipped = false;
            for (String s : tagBlacklist) {
                TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(s));
                if (entry.getKey().getDefaultState().isIn(tag)) {
                    skipped = true;
                }
            }
            if (!skipped) {
                for (String s : tagWhitelist) {
                    TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(s));

                    if (entry.getKey().getDefaultState().isIn(tag)) {
                        Map<String, Integer> populatedRestriction = new HashMap<>();

                        for (Pair<String, Integer> pair : entry.getValue()) {
                            populatedRestriction.put(pair.getLeft(), pair.getRight());
                        }
                        LevelManager.MINING_RESTRICTIONS.put(Registries.BLOCK.getRawId(entry.getKey()), new PlayerRestriction(Registries.BLOCK.getRawId(entry.getKey()), populatedRestriction));
                    }
                }
            }
        }
    }

    public enum BlockAlgorithm {
        RESISTANCE(Block::getBlastResistance),
        HARDNESS(Block::getHardness);

        public final BlockRunner runner;

        BlockAlgorithm(BlockRunner runner) {
            this.runner = runner;
        }
    }

    public interface BlockRunner {
        float run(Block material);
    }
}
