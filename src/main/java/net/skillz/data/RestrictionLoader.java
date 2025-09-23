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
            JsonObject restrictionObj = element.getAsJsonObject();
            Map<Identifier, Integer> skillLevelRestrictions = new HashMap<>();
            boolean override = OptionalObject.get(restrictionObj, "override", false).getAsBoolean();

            for (JsonElement elem : restrictionObj.getAsJsonArray("skills")) {
                JsonObject skillObj = elem.getAsJsonObject();
                Identifier skillId = Identifier.tryParse(skillObj.get("skill").getAsString());
                int level = skillObj.get("level").getAsInt();
                if (LevelManager.SKILLS.containsKey(skillId)) {
                    skillLevelRestrictions.put(skillId, level);
                } else {
                    LOGGER.warn("Restriction {} contains an unrecognized skill called {}.", fileName, skillId);
                }
            }

            if (!skillLevelRestrictions.isEmpty()) {
                for (RestrictionLoaderEntry entry : entries) {
                    for (JsonElement elem : OptionalObject.get(restrictionObj, entry.name(), new JsonArray()).getAsJsonArray()) {
                        Identifier elemId = Identifier.tryParse(elem.getAsString());
                        if (entry.registry().containsId(elemId)) {
                            int rawId = entry.registry().getRawId(entry.registry().get(elemId));

                            if (entry.restrictionMap.get(rawId) == null || override) {
                                entry.restrictionMap.put(rawId, new PlayerRestriction(rawId, skillLevelRestrictions));
                            }else {
                                LOGGER.warn("Object {} is already restricted", elemId);
                            }
                        } else {
                            LOGGER.warn("Restriction {} contains an unrecognized id {}.", fileName, elemId);
                        }
                    }
                }
                // enchantments
                for (JsonElement elem : OptionalObject.get(restrictionObj, "enchantments", new JsonArray()).getAsJsonArray()) {
                    JsonObject enchantObj = elem.getAsJsonObject();
                    Identifier enchantId = Identifier.tryParse(enchantObj.get("name").getAsString());
                    int enchantLevel = enchantObj.get("level").getAsInt();

                    if (enchantId != null && EnchantmentRegistry.containsId(enchantId, enchantLevel)) {
                        int enchantmentRawId = EnchantmentRegistry.getId(enchantId, enchantLevel);

                        if (LevelManager.ENCHANTMENT_RESTRICTIONS.get(enchantmentRawId) == null || override) {
                            LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentRawId, new PlayerRestriction(enchantmentRawId, skillLevelRestrictions));
                        }else {
                            LOGGER.warn("Enchantment {} is already restricted", enchantId);
                        }
                    } else {
                        LOGGER.warn("Restriction {} contains an unrecognized enchantment id called {}.", fileName, enchantId);
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
