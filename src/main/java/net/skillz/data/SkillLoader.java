package net.skillz.data;

import com.google.gson.JsonArray;
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
import net.skillz.access.LevelManagerAccess;
import net.skillz.bonus.BonusCondition;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.BonusProvider;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.level.SkillAttribute;
import net.skillz.level.SkillBonus;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;

public class SkillLoader extends SimpleDataLoader {
    public static final Identifier ID = SkillZMain.identifierOf("skill_loader");

    public SkillLoader() {
        super(ID, "skill");
    }

    @Override
    public void preReload() {
        LevelManager.SKILLS.clear();
        BonusManager.clear();
    }

    @Override
    public void reloadResource(JsonObject data, Identifier id, String fileName) {
        if ((OptionalObject.get(data, "default", false).getAsBoolean() && !ConfigInit.MAIN.PROGRESSION.SKILLS.defaultSkills) || ConfigInit.MAIN.PROGRESSION.SKILLS.disabledSkills.contains(id.toString())) {
            return;
        }

        int maxLevel = OptionalObject.get(data, "max_level", ConfigInit.MAIN.PROGRESSION.SKILLS.defaultMaxLevel).getAsInt();
        int index = OptionalObject.get(data, "index", 999).getAsInt();
        Identifier texture = Identifier.tryParse(data.get("texture").getAsString());
        List<SkillAttribute> attributes = new ArrayList<>();


        for (JsonElement attrElem : OptionalObject.get(data, "attributes", new JsonArray()).getAsJsonArray()) {
            JsonObject attrObj = attrElem.getAsJsonObject();

            //TODO EntityAttribute registry keys
            String type = attrObj.get("type").getAsString();
            Identifier iden = Identifier.tryParse(type);

            if (attrObj.has("primary")) {
                if (FabricLoader.getInstance().isModLoaded(attrObj.get("primary").getAsJsonObject().get("mod").getAsString())) {
                    SkillZMain.LOGGER.info("Switch attribute {} to primary {}", type, attrObj.get("primary").getAsJsonObject().get("type").getAsString());
                    iden = Identifier.tryParse(attrObj.get("primary").getAsJsonObject().get("type").getAsString());
                }
            }
            RegistryKey<EntityAttribute> asd = RegistryKey.of(RegistryKeys.ATTRIBUTE, iden);
            Optional<RegistryEntry.Reference<EntityAttribute>> entityAttribute = Registries.ATTRIBUTE.getEntry(asd);

            if (entityAttribute.isPresent()) {
                int attributeIndex = OptionalObject.get(attrObj, "index", 999).getAsInt();

                RegistryEntry<EntityAttribute> attribute = entityAttribute.get();
                float baseValue = OptionalObject.get(attrObj, "base", -10000.0f).getAsFloat();
                boolean useBaseValue = OptionalObject.get(attrObj, "set_base_value", false).getAsBoolean();

                float levelValue = attrObj.get("value").getAsFloat();
                EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(attrObj.get("operation").getAsString().toUpperCase());
                attributes.add(new SkillAttribute(attributeIndex, attribute, baseValue, useBaseValue, levelValue, operation));
            } else {
                SkillZMain.LOGGER.warn("Attribute {} is not a usable attribute in skill {}.", iden, id);
                continue;
            }
        }

        for (JsonElement attributeElement : OptionalObject.get(data, "bonus", new JsonArray()).getAsJsonArray()) {
            JsonObject bonusObj = attributeElement.getAsJsonObject();
            Identifier bonusKey = Identifier.tryParse(bonusObj.get("key").getAsString());
            int bonusLevel = bonusObj.get("level").getAsInt();

            if (!BonusManager.VALID_BONUSES.contains(bonusKey)) {
                SkillZMain.LOGGER.warn("Bonus type {} is not a valid bonus type.", bonusObj.get("key").getAsString());
                continue;
            }

            BonusManager.SKILL_BONUSES.put(bonusKey, new SkillBonus(bonusKey, id, bonusLevel));
            BonusManager.BONUSES.get(bonusKey).registerProvisions(id,
                new BonusCondition(id, (player)-> {
                    LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                    int level = levelManager.getPlayerSkills().get(id).getLevel();
                    return level >= bonusLevel;
                }),
                new BonusProvider(id, (player)-> {
                    LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                    return levelManager.getPlayerSkills().get(id).getLevel();
                }),
            true);
        }
        LevelManager.SKILLS.put(id, new Skill(id, texture, index, maxLevel, attributes));
    }
}
