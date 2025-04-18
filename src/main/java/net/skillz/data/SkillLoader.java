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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

        // safety check
        AtomicInteger skillCount = new AtomicInteger();
        List<Integer> attributeIds = new ArrayList<>();

        manager.findResources("skills", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            System.out.println(id);
            System.out.println(id.getPath());
            System.out.println(id.getNamespace());
            try {
                if (!ConfigInit.MAIN.PROGRESSION.defaultSkills && id.getPath().endsWith("/default.json")) {
                    return;
                }
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                for (String mapKey : data.keySet()) {
                    JsonObject skillJsonObject = data.getAsJsonObject(mapKey);

                    // replace check
                    /*if (skillList.contains(skillJsonObject.get("id").getAsString())) {
                        LevelzMain.LOGGER.warn("Skill {} was already loaded.", skillJsonObject.get("id").getAsString());
                        continue;
                    }
                    if (skillJsonObject.has("replace") && skillJsonObject.get("replace").getAsBoolean()) {
                        skillList.add(skillJsonObject.get("id").getAsString());
                    }

                    int identification = skillJsonObject.get("id").getAsInt();
                    // loading check
                    if (LevelManager.SKILLS.containsKey(identification)) {
                        LevelzMain.LOGGER.warn("Id {} in skill {} was already used by another skill.", identification, skillJsonObject.get("id").getAsString());
                        continue;
                    }*/

                    int identification = skillJsonObject.get("id").getAsInt();
                    //Identifier id = Identifier.of();
                    if (skillJsonObject.has("replace") && skillJsonObject.get("replace").getAsBoolean()) {
                        skillList.add(identification);
                        if (LevelManager.SKILLS.containsKey(identification)) {
                            LevelManager.SKILLS.get(identification).attributes().forEach(attribute -> {
                                if (attribute.getId() != -1 && attributeIds.contains(attribute.getId())) {
                                    attributeIds.remove(attribute.getId());
                                }
                            });
                            LevelManager.SKILLS.remove(identification);
                            skillCount.getAndDecrement();
                        }
                    } else if (skillList.contains(identification)) {
                        continue;
                    }

                    // skill creation
                    String key = skillJsonObject.get("key").getAsString();
                    int maxLevel = skillJsonObject.get("level").getAsInt();
                    List<SkillAttribute> attributes = new ArrayList<>();

                    for (JsonElement attributeElement : skillJsonObject.getAsJsonArray("attributes")) {
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
                            float levelValue = attributeJsonObject.get("value").getAsFloat();
                            EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(attributeJsonObject.get("operation").getAsString().toUpperCase());
                            attributes.add(new SkillAttribute(attributeId, attibute, baseValue, levelValue, operation));
                            if (attributeId != -1) {
                                attributeIds.add(attributeId);
                            }
                        } else {
                            SkillZMain.LOGGER.warn("Attribute {} is not a usable attribute in skill {}.", attributeJsonObject.get("type").getAsString(), skillJsonObject.get("id").getAsString());
                            continue;
                        }
                    }

                    if (skillJsonObject.has("bonus")) {
                        for (JsonElement attributeElement : skillJsonObject.getAsJsonArray("bonus")) {
                            JsonObject bonusJsonObject = attributeElement.getAsJsonObject();
                            String bonusKey = bonusJsonObject.get("key").getAsString();
                            int bonusLevel = bonusJsonObject.get("level").getAsInt();

                            if (!SkillBonus.BONUS_KEYS.contains(bonusKey)) {
                                SkillZMain.LOGGER.warn("Bonus type {} is not a valid bonus type.", bonusKey);
                                continue;
                            }

                            LevelManager.BONUSES.put(bonusKey, new SkillBonus(bonusKey, identification, bonusLevel));
                        }
                    }

                    LevelManager.SKILLS.put(identification, new Skill(identification, key, maxLevel, attributes));

                    skillCount.getAndIncrement();
                }
            } catch (Exception e) {
                SkillZMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });

        for (int i = 0; i < skillCount.get(); i++) {
            if (!LevelManager.SKILLS.containsKey(i)) {
                throw new MissingResourceException("Missing skill with id " + i + "! Please add a skill with this id.", this.getClass().getName(), SkillZMain.MOD_ID);
            }
        }
        for (int i = 0; i < attributeIds.size(); i++) {
            if (!attributeIds.contains(i)) {
                throw new MissingResourceException("Missing attribute with id " + i + "! Please add an attribute with this id.", this.getClass().getName(), SkillZMain.MOD_ID);
            }
        }
        Map<Integer, Skill> sortedMap = new TreeMap<>(LevelManager.SKILLS);
        LevelManager.SKILLS.clear();
        LevelManager.SKILLS.putAll(sortedMap);
    }
}
