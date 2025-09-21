package net.skillz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.loader.SimpleDataLoader;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.content.registry.EnchantmentRegistry;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import org.apache.commons.compress.utils.Lists;

import java.util.*;

import static net.skillz.SkillZMain.LOGGER;

public class RestrictionLoader extends SimpleDataLoader {
    public static final Identifier ID = SkillZMain.identifierOf("restriction_loader");

    public final List<RestrictionLoaderEntry> entries = Lists.newArrayList();

    private static final List<Integer> blockList = new ArrayList<>();
    private static final List<Integer> craftingList = new ArrayList<>();
    private static final List<Integer> entityList = new ArrayList<>();
    private static final List<Integer> itemList = new ArrayList<>();
    private static final List<Integer> miningList = new ArrayList<>();
    private static final List<Integer> enchantmentList = new ArrayList<>();

    public RestrictionLoader() {
        super(ID, "restriction");
        entries.add(new RestrictionLoaderEntry("blocks", Registries.BLOCK, LevelManager.BLOCK_RESTRICTIONS, new ArrayList<>()));
        entries.add(new RestrictionLoaderEntry("crafting", Registries.ITEM, LevelManager.CRAFTING_RESTRICTIONS, new ArrayList<>()));
        entries.add(new RestrictionLoaderEntry("entities", Registries.ENTITY_TYPE, LevelManager.ENTITY_RESTRICTIONS, new ArrayList<>()));
        entries.add(new RestrictionLoaderEntry("items", Registries.ITEM, LevelManager.ITEM_RESTRICTIONS, new ArrayList<>()));
        entries.add(new RestrictionLoaderEntry("mining", Registries.BLOCK, LevelManager.MINING_RESTRICTIONS, new ArrayList<>()));


    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return Collections.singleton(SkillLoader.ID);
    }

    @Override
    public void preReload() {
        entries.forEach(entry -> entry.restrictionMap.clear());
        /*LevelManager.BLOCK_RESTRICTIONS.clear();
        LevelManager.CRAFTING_RESTRICTIONS.clear();
        LevelManager.ENTITY_RESTRICTIONS.clear();
        LevelManager.ITEM_RESTRICTIONS.clear();
        LevelManager.MINING_RESTRICTIONS.clear();*/
        LevelManager.ENCHANTMENT_RESTRICTIONS.clear();

        EnchantmentRegistry.updateEnchantments();
    }

    public record RestrictionLoaderEntry(String name, Registry registry, Map<Integer, PlayerRestriction> restrictionMap, List<Integer> idList) {}

    @Override
    public boolean condition() {
        return ConfigInit.MAIN.PROGRESSION.RESTRICTIONS.enableRestrictions;
    }

    @Override
    public void reloadResource(JsonObject data, Identifier id, String fileName) {
        if ((OptionalObject.get(data, "default", false).getAsBoolean() && !ConfigInit.MAIN.PROGRESSION.RESTRICTIONS.defaultRestrictions) || ConfigInit.MAIN.PROGRESSION.RESTRICTIONS.disabledRestrictions.contains(id.toString())) {
            return;
        }

        for (JsonElement element : data.get("restrictions").getAsJsonArray()) {
            JsonObject restrictionJsonObject = element.getAsJsonObject();
            Map<String, Integer> skillLevelRestrictions = new HashMap<>();
            boolean replace = OptionalObject.get(restrictionJsonObject, "replace", false).getAsBoolean();

            JsonObject skillRestrictions = restrictionJsonObject.getAsJsonObject("skills");
            for (String skillKey : skillRestrictions.keySet()) {
                if (LevelManager.SKILLS.containsKey(skillKey)) {
                    skillLevelRestrictions.put(skillKey, skillRestrictions.get(skillKey).getAsInt());
                } else {
                    LOGGER.warn("Restriction {} contains an unrecognized skill called {}.", fileName, skillKey);
                }
            }

            if (!skillLevelRestrictions.isEmpty()) {
                for (RestrictionLoaderEntry entry : entries) {
                    for (JsonElement elem : OptionalObject.get(restrictionJsonObject, entry.name(), new JsonArray()).getAsJsonArray()) {
                        Identifier elemId = Identifier.tryParse(elem.getAsString());
                        if (entry.registry().containsId(elemId)) {
                            int rawId = entry.registry().getRawId(entry.registry().get(elemId));

                            if (entry.idList().contains(rawId)) {
                                continue;
                            }
                            if (replace) {
                                entry.idList().add(rawId);
                            }
                            entry.restrictionMap.put(rawId, new PlayerRestriction(rawId, skillLevelRestrictions));
                        } else {
                            LOGGER.warn("Restriction {} contains an unrecognized id {}.", fileName, elemId);
                        }
                    }
                }
                // blocks
                /*for (JsonElement elem : OptionalObject.get(restrictionJsonObject, "blocks", new JsonArray()).getAsJsonArray()) {
                    Identifier elemId = Identifier.tryParse(elem.getAsString());
                    if (Registries.BLOCK.containsId(elemId)) {
                        int blockRawId = Registries.BLOCK.getRawId(Registries.BLOCK.get(elemId));

                        if (blockList.contains(blockRawId)) {
                            continue;
                        }
                        if (replace) {
                            blockList.add(blockRawId);
                        }
                        LevelManager.BLOCK_RESTRICTIONS.put(blockRawId, new PlayerRestriction(blockRawId, skillLevelRestrictions));
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized block id called {}.", fileName, elemId);
                    }
                }*/
                // crafting
                /*for (JsonElement elem : OptionalObject.get(restrictionJsonObject, "crafting", new JsonArray()).getAsJsonArray()) {
                    Identifier elemId = Identifier.tryParse(elem.getAsString());
                    if (Registries.ITEM.containsId(elemId)) {
                        int craftingRawId = Registries.ITEM.getRawId(Registries.ITEM.get(elemId));

                        if (craftingList.contains(craftingRawId)) {
                            continue;
                        }
                        if (replace) {
                            craftingList.add(craftingRawId);
                        }
                        LevelManager.CRAFTING_RESTRICTIONS.put(craftingRawId, new PlayerRestriction(craftingRawId, skillLevelRestrictions));
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized crafting id called {}.", fileName, elemId);
                    }
                }*/
                // entities
                /*for (JsonElement elem : OptionalObject.get(restrictionJsonObject, "entities", new JsonArray()).getAsJsonArray()) {
                    Identifier elemId = Identifier.tryParse(elem.getAsString());
                    if (Registries.ENTITY_TYPE.containsId(elemId)) {
                        int entityRawId = Registries.ENTITY_TYPE.getRawId(Registries.ENTITY_TYPE.get(elemId));

                        if (entityList.contains(entityRawId)) {
                            continue;
                        }
                        if (replace) {
                            entityList.add(entityRawId);
                        }
                        LevelManager.ENTITY_RESTRICTIONS.put(entityRawId, new PlayerRestriction(entityRawId, skillLevelRestrictions));
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized entity id called {}.", fileName, elemId);
                    }
                }*/
                // items
                /*for (JsonElement elem : OptionalObject.get(restrictionJsonObject, "items", new JsonArray()).getAsJsonArray()) {
                    Identifier elemId = Identifier.tryParse(elem.getAsString());
                    if (Registries.ITEM.containsId(elemId)) {
                        int itemRawId = Registries.ITEM.getRawId(Registries.ITEM.get(elemId));

                        if (itemList.contains(itemRawId)) {
                            continue;
                        }
                        if (replace) {
                            itemList.add(itemRawId);
                        }
                        LevelManager.ITEM_RESTRICTIONS.put(itemRawId, new PlayerRestriction(itemRawId, skillLevelRestrictions));
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized item id called {}.", fileName, elemId);
                    }
                }*/
                // mining
                /*for (JsonElement elem : OptionalObject.get(restrictionJsonObject, "mining", new JsonArray()).getAsJsonArray()) {
                    Identifier elemId = Identifier.tryParse(elem.getAsString());
                    if (Registries.BLOCK.containsId(elemId)) {
                        int miningRawId = Registries.BLOCK.getRawId(Registries.BLOCK.get(elemId));

                        if (miningList.contains(miningRawId)) {
                            continue;
                        }
                        if (replace) {
                            miningList.add(miningRawId);
                        }
                        LevelManager.MINING_RESTRICTIONS.put(miningRawId, new PlayerRestriction(miningRawId, skillLevelRestrictions));
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized mining id called {}.", fileName, elemId);
                    }
                }*/
                // enchantments
                JsonObject enchantmentObject = OptionalObject.get(restrictionJsonObject, "enchantments", new JsonObject()).getAsJsonObject();
                for (String enchantment : enchantmentObject.keySet()) {
                    Identifier enchantmentIdentifier = Identifier.splitOn(enchantment, ':');
                    int level = enchantmentObject.get(enchantment).getAsInt();
                    if (EnchantmentRegistry.containsId(enchantmentIdentifier, level)) {
                        int enchantmentRawId = EnchantmentRegistry.getId(enchantmentIdentifier, level);
                        if (enchantmentList.contains(enchantmentRawId)) {
                            continue;
                        }
                        if (replace) {
                            enchantmentList.add(enchantmentRawId);
                        }
                        LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentRawId, new PlayerRestriction(enchantmentRawId, skillLevelRestrictions));
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized enchantment id called {}.", fileName, enchantmentIdentifier);
                    }
                }
            } else {
                LOGGER.warn("Restriction {} does not contain any valid skills.", fileName);
            }
        }
    }

    @Override
    public void postReload() {

    }
}
