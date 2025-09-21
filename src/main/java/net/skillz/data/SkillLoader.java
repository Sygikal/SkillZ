package net.skillz.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.loader.SimpleDataLoader;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.level.SkillAttribute;
import net.skillz.level.SkillBonus;
import net.skillz.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

public class SkillLoader extends SimpleDataLoader {
    public static final Identifier ID = SkillZMain.identifierOf("skill_loader");

    List<Integer> attributeIds = new ArrayList<>();

    public SkillLoader() {
        super(ID, "skill");
    }

    @Override
    public void preReload() {
        LevelManager.SKILLS.clear();
        LevelManager.BONUSES.clear();
        attributeIds = new ArrayList<>();
    }

    @Override
    public void reloadResource(JsonObject data, Identifier id, String fileName) {
        if (OptionalObject.get(data, "default", false).getAsBoolean() && !ConfigInit.MAIN.PROGRESSION.defaultSkills) {
            return;
        }

        String skillId = FileUtil.getBaseName(id.getPath());

        int maxLevel = data.get("maxlevel").getAsInt();
        int index = 999;
        if (data.has("index")) {
            index = data.get("index").getAsInt();
        }
        List<SkillAttribute> attributes = new ArrayList<>();

        for (JsonElement attributeElement : data.getAsJsonArray("attributes")) {
            JsonObject attributeJsonObject = attributeElement.getAsJsonObject();

            //TODO EntityAttribute registry keys
            Identifier iden;
            if (attributeJsonObject.get("type").getAsString().contains("attribute-backport:player.block_interaction_range") && FabricLoader.getInstance().isModLoaded("reach-entity-attributes")) {
                iden = Identifier.splitOn("reach-entity-attributes:reach", ':');
            }else {
                iden = Identifier.splitOn(attributeJsonObject.get("type").getAsString(), ':');
            }
            RegistryKey<EntityAttribute> asd = RegistryKey.of(RegistryKeys.ATTRIBUTE, iden);
            Optional<RegistryEntry.Reference<EntityAttribute>> entityAttribute = Registries.ATTRIBUTE.getEntry(asd);

            if (entityAttribute.isPresent()) {
                int attributeId = -1;
                if (attributeJsonObject.has("id")) {
                    attributeId = attributeJsonObject.get("id").getAsInt();
                }
                RegistryEntry<EntityAttribute> attibute = entityAttribute.get();
                float baseValue = -10000.0f;
                if (attributeJsonObject.has("base")) {
                    baseValue = attributeJsonObject.get("base").getAsFloat();
                }
                boolean useBaseValue = false;
                if (attributeJsonObject.has("set_base_value")) {
                    useBaseValue = attributeJsonObject.get("set_base_value").getAsBoolean();
                }
                float levelValue = attributeJsonObject.get("value").getAsFloat();
                EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(attributeJsonObject.get("operation").getAsString().toUpperCase());
                attributes.add(new SkillAttribute(attributeId, attibute, baseValue, useBaseValue, levelValue, operation));
                if (attributeId != -1) {
                    attributeIds.add(attributeId);
                }
            } else {
                SkillZMain.LOGGER.warn("Attribute {} is not a usable attribute in skill {}.", attributeJsonObject.get("type").getAsString(), data.get("id").getAsString());
                continue;
            }
        }

        if (data.has("bonus")) {
            for (JsonElement attributeElement : data.getAsJsonArray("bonus")) {
                JsonObject bonusJsonObject = attributeElement.getAsJsonObject();
                String bonusKey = bonusJsonObject.get("key").getAsString();
                int bonusLevel = bonusJsonObject.get("level").getAsInt();

                if (!SkillBonus.BONUS_KEYS.contains(bonusKey)) {
                    SkillZMain.LOGGER.warn("Bonus type {} is not a valid bonus type.", bonusKey);
                    continue;
                }

                LevelManager.BONUSES.put(bonusKey, new SkillBonus(bonusKey, skillId, bonusLevel));
            }
        }
        LevelManager.SKILLS.put(skillId, new Skill(skillId, index, maxLevel, attributes));
    }

    @Override
    public void postReload() {
        for (int i = 0; i < attributeIds.size(); i++) {
            if (!attributeIds.contains(i)) {
                throw new MissingResourceException("Missing attribute with id " + i + "! Please add an attribute with this id.", this.getClass().getName(), SkillZMain.MOD_ID);
            }
        }
    }
}
