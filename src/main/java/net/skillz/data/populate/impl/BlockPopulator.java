package net.skillz.data.populate.impl;

import com.google.gson.JsonArray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.data.populate.Populator;
import net.skillz.init.ConfigInit;
import net.skillz.init.LoaderInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BlockPopulator extends Populator {
    private final List<String> hiddenItems = Lists.newArrayList();

    private final List<String> tagWhitelist = Lists.newArrayList();
    private final List<String> tagBlacklist = Lists.newArrayList();
    private final float minCutoff;
    private final float maxCutoff;
    private final List<BlockFilter> filters = Lists.newArrayList();
    private final Map<Block, List<Pair<String, Integer>>> list;
    private final Map<Integer, PlayerRestriction> restrictionMap;

    private final List<Identifier> additional = Lists.newArrayList();

    private JsonArray skillArray;


    public BlockPopulator(Identifier id, Map<Integer, PlayerRestriction> resmap, Map<Block, List<Pair<String, Integer>>> list, JsonArray hiddenTags, JsonArray additional, JsonArray blockFilters, JsonArray tagWhitelist, JsonArray tagBlacklist, float minCutoff, float maxCutoff) {
        super(id);
        this.list = list;
        this.restrictionMap = resmap;
        tagWhitelist.forEach((elem) -> {this.tagWhitelist.add(elem.getAsString());});
        tagBlacklist.forEach((elem) -> {this.tagBlacklist.add(elem.getAsString());});
        this.minCutoff = minCutoff;
        this.maxCutoff = maxCutoff;
        blockFilters.forEach((elem) -> {this.filters.add(BlockFilter.valueOf(elem.getAsString().toUpperCase()));});

        additional.forEach((elem) -> {this.additional.add(Identifier.tryParse(elem.getAsString()));});
        hiddenTags.forEach((elem) -> {this.hiddenItems.add(elem.getAsString());});

    }

    @Override
    public void populate(JsonArray skillArray) {
        this.skillArray = skillArray;
    }

    @Override
    public void postPopulate() {
        for (Block block : Registries.BLOCK) {
            Identifier id = Registries.BLOCK.getId(block);
            if ((!getIdBlacklist().contains(id) && block.getHardness() > minCutoff && block.getHardness() < maxCutoff) || (additional.contains(id))) {
                boolean skipped = false;
                if (!filters.isEmpty()) {
                    for (BlockFilter filter : filters) {
                        if (!filter.runner.run(block)) {
                            skipped = true;
                        }
                    }
                }
                for (String s : tagBlacklist) {
                    TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(s));
                    if (block.getDefaultState().isIn(tag)) {
                        skipped = true;
                    }
                }

                if (!skipped || (additional.contains(id))) {
                    Map<String, Integer> populatedRestriction = getSkillMap(skillArray, id, formula -> {
                        return formula.
                                replace("RESISTANCE", String.valueOf(BlockAlgorithm.RESISTANCE.runner.run(block))).
                                replace("HARDNESS", String.valueOf(BlockAlgorithm.HARDNESS.runner.run(block)));
                    });

                    /*for (Map.Entry<String, Integer> entry : populatedRestriction.entrySet()) {
                        list.computeIfAbsent(block, k -> new ArrayList<>()).add(Pair.of(entry.getKey(), entry.getValue()));
                    }*/

                    boolean hidden = block.asItem().equals(Items.AIR);
                    for (String s : hiddenItems) {
                        if (s.contains("#")) {
                            s = s.replace("#", "");
                            TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, Identifier.tryParse(s));
                            if (block.asItem().getDefaultStack().isIn(tag)) {
                                hidden = true;
                            }
                        }else {
                            if (Registries.ITEM.getId(block.asItem()).equals(Identifier.tryParse(s))) {
                                hidden = true;
                            }
                        }
                    }

                    if (!populatedRestriction.isEmpty()) {
                        int rawId = Registries.BLOCK.getRawId(block);
                        PlayerRestriction restriction = new PlayerRestriction(rawId, populatedRestriction);
                        restriction.setHidden(hidden);
                        if (!tagWhitelist.isEmpty()) {
                            for (String s : tagWhitelist) {
                                TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, Identifier.tryParse(s));

                                if (block.getDefaultState().isIn(tag)) {
                                    if (restrictionMap.get(rawId) == null || ConfigInit.MAIN.PROGRESSION.populatorOverride) {
                                        restrictionMap.put(rawId, restriction);
                                    }
                                }
                            }
                        } else {
                            if (restrictionMap.get(rawId) == null || ConfigInit.MAIN.PROGRESSION.populatorOverride) {
                                restrictionMap.put(rawId, restriction);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean handleInTag(Identifier currId, Identifier tagId) {
        TagKey<Block> tag = TagKey.of(RegistryKeys.BLOCK, tagId);
        if (Registries.BLOCK.get(currId).getDefaultState().isIn(tag)) {
            return true;
        }
        return false;
    }

    public enum BlockFilter {
        IS_OPAQUE((block) -> {
            return block.getDefaultState().isOpaque();
        }),
        REQUIRES_TOOL((block) -> {
            return block.getDefaultState().isToolRequired();
        }),
        //Mycelium, copper weathering, dripstone, amethyst
        HAS_RANDOM_TICKS((block) -> {
            return block.getDefaultState().hasRandomTicks();
        }),
        EMITS_POWER((block) -> {
            return block.getDefaultState().emitsRedstonePower();
        }),
        //Fences and such
        EXCEEDS_CUBE((block) -> {
            return block.getDefaultState().exceedsCube();
        }),
        SOLID((block) -> {
            return block.getDefaultState().isSolid();
        }),
        BURNABLE((block) -> {
            return block.getDefaultState().isBurnable();
        }),
        HAS_BE((block) -> {
            return block.getDefaultState().hasBlockEntity();
        }),
        REPLACEABLE((block) -> {
            return block.getDefaultState().isReplaceable();
        }),
        NON_INVISIBLE((block) -> {
            return block.getDefaultState().getRenderType() != BlockRenderType.INVISIBLE;
        }),
        NON_AIR_ITEM((block) -> {
            return !block.asItem().equals(Items.AIR);
        });


        public final BlockBoolRunner runner;

        BlockFilter(BlockBoolRunner runner) {
            this.runner = runner;
        }
    }

    public enum BlockAlgorithm {
        RESISTANCE(Block::getBlastResistance),
        HARDNESS(Block::getHardness);

        public final BlockFloatRunner runner;

        BlockAlgorithm(BlockFloatRunner runner) {
            this.runner = runner;
        }
    }

    public interface BlockFloatRunner {
        float run(Block material);
    }

    public interface BlockBoolRunner {
        boolean run(Block material);
    }
}
