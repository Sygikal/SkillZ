package net.skillz.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.skillz.SkillZMain;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.level.SkillAttribute;
import net.skillz.level.SkillBonus;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.skillz.util.FileUtil;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class SkillLoader implements SimpleSynchronousResourceReloadListener {

    //private static List<String> skillList = new ArrayList<>();
    private static final List<Integer> skillList = new ArrayList<>();

    @Override
    public Identifier getFabricId() {
        return SkillZMain.identifierOf("skills");
    }

    @Override
    public void reload(ResourceManager manager) {
        // clear skills
        LevelManager.SKILLS.clear();
        // clear bonuses
        LevelManager.BONUSES.clear();

        List<Integer> attributeIds = new ArrayList<>();

        manager.findResources("skill", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            System.out.println(id);
            //System.out.println(id.getPath());
            //System.out.println(id.getNamespace());
            try {
                if (!ConfigInit.MAIN.PROGRESSION.defaultSkills && id.getNamespace().equals("skillz")) {
                    return;
                }
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

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
                        System.out.println("ASEX");
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
            } catch (Exception e) {
                SkillZMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
        /*for (int i = 0; i < skillCount.get(); i++) {
            if (!LevelManager.SKILLS.containsKey(i)) {
                throw new MissingResourceException("Missing skill with id " + i + "! Please add a skill with this id.", this.getClass().getName(), SkillZMain.MOD_ID);
            }
        }*/
        for (int i = 0; i < attributeIds.size(); i++) {
            if (!attributeIds.contains(i)) {
                throw new MissingResourceException("Missing attribute with id " + i + "! Please add an attribute with this id.", this.getClass().getName(), SkillZMain.MOD_ID);
            }
        }
    }
}
