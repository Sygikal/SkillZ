package net.skillz.level;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.Identifier;
import net.skillz.init.ConfigInit;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.content.registry.EnchantmentRegistry;
import net.skillz.util.LevelHelper;
import net.skillz.util.PacketHelper;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.skillz.util.TextUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LevelManager {

    public static final Map<Identifier, Skill> SKILLS = new LinkedHashMap<>();

    public static final Map<Integer, PlayerRestriction> BLOCK_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> CRAFTING_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> ENTITY_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> ITEM_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> MINING_RESTRICTIONS = new HashMap<>();
    public static final Map<Integer, PlayerRestriction> ENCHANTMENT_RESTRICTIONS = new HashMap<>();

    private final PlayerEntity playerEntity;
    private Map<Identifier, PlayerSkill> playerSkills = new HashMap<>();

    // Level
    private int overallLevel;
    private int totalLevelExperience;
    private float levelProgress;
    private int skillPoints;

    public LevelManager(PlayerEntity playerEntity) {
        this.playerEntity = playerEntity;

        for (Skill skill : SKILLS.values()) {
            if (!this.playerSkills.containsKey(skill.id())) {
                this.playerSkills.put(skill.id(), new PlayerSkill(skill.id(), 0));
            } else if (this.playerSkills.get(skill.id()).getLevel() > skill.maxLevel()) {
                this.playerSkills.get(skill.id()).setLevel(skill.maxLevel());
            }
        }
    }

    public PlayerEntity getPlayerEntity() {
        return playerEntity;
    }

    public void readNbt(NbtCompound nbt) {
        this.overallLevel = nbt.getInt("Level");
        this.levelProgress = nbt.getFloat("LevelProgress");
        this.totalLevelExperience = nbt.getInt("TotalLevelExperience");
        this.skillPoints = nbt.getInt("SkillPoints");

        NbtList skills = nbt.getList("Skills", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < skills.size(); i++) {
            PlayerSkill skill = new PlayerSkill(skills.getCompound(i));
            if (!SKILLS.containsKey(skill.getId())) {
                continue;
            }
            playerSkills.put(skill.getId(), skill);
        }

    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putInt("Level", this.overallLevel);
        nbt.putFloat("LevelProgress", this.levelProgress);
        nbt.putInt("TotalLevelExperience", this.totalLevelExperience);
        nbt.putInt("SkillPoints", this.skillPoints);

        NbtList skills = new NbtList();
        for (PlayerSkill skill : this.playerSkills.values()) {
            skills.add(skill.writeDataToNbt());
        }
        nbt.put("Skills", skills);
    }

    public Map<Identifier, PlayerSkill> getPlayerSkills() {
        return playerSkills;
    }

    public void setPlayerSkills(Map<Identifier, PlayerSkill> playerSkills) {
        this.playerSkills = playerSkills;
    }

    public void setOverallLevel(int overallLevel) {
        this.overallLevel = overallLevel;
    }

    public int getOverallLevel() {
        return overallLevel;
    }

    public void setTotalLevelExperience(int totalLevelExperience) {
        this.totalLevelExperience = totalLevelExperience;
    }

    public int getTotalLevelExperience() {
        return totalLevelExperience;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public int getSkillPointColor() {
        return hasSkillPoints() ? ConfigInit.CLIENT.skillPointColor.toInt() : 0xFFFFFF;
    }

    public void setLevelProgress(float levelProgress) {
        this.levelProgress = levelProgress;
    }

    public float getLevelProgress() {
        return levelProgress;
    }

    public void setSkillLevel(Identifier skillId, int level) {
        this.playerSkills.get(skillId).setLevel(level);
    }

    public int getSkillLevel(Identifier skillId) {
        // Maybe add a containsKey check here
        return this.playerSkills.get(skillId).getLevel();
    }

    public void addExperienceLevels(int levels) {
        this.overallLevel += levels;
        this.skillPoints += ConfigInit.MAIN.LEVEL.pointsPerLevel;
        if (this.overallLevel < 0) {
            this.overallLevel = 0;
            this.levelProgress = 0.0F;
            this.totalLevelExperience = 0;
        }
    }

    public boolean isMaxLevel() {
        if (ConfigInit.MAIN.LEVEL.overallMaxLevel > 0) {
            return this.overallLevel >= ConfigInit.MAIN.LEVEL.overallMaxLevel;
        } else {
            int maxLevel = 0;
            for (Skill skill : SKILLS.values()) {
                maxLevel += skill.maxLevel();
            }
            return this.overallLevel >= maxLevel;
        }
    }

    public boolean hasSkillPoints() {
        return this.skillPoints > 0;
    }

    public int getNextLevelExperience() {
        if (isMaxLevel()) {
            return 0;
        }

        int cost = Math.round((float) TextUtil.evaluateFormula(ConfigInit.MAIN.EXPERIENCE.xpFormula.replace("LVL", String.valueOf(this.overallLevel))));

        if (ConfigInit.MAIN.EXPERIENCE.xpMaxCost != 0) {
            return cost >= ConfigInit.MAIN.EXPERIENCE.xpMaxCost ? ConfigInit.MAIN.EXPERIENCE.xpMaxCost : cost;
        } else {
            return cost;
        }
    }

    // block
    public boolean hasRequiredBlockLevel(Block block) {
        int itemId = Registries.BLOCK.getRawId(block);
        if (BLOCK_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = BLOCK_RESTRICTIONS.get(itemId);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (this.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Identifier, Integer> getRequiredBlockLevel(Block block) {
        int itemId = Registries.BLOCK.getRawId(block);
        if (BLOCK_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = BLOCK_RESTRICTIONS.get(itemId);
            return playerRestriction.getSkillLevelRestrictions();
        }
        return null;
    }

    // crafting
    public boolean hasRequiredCraftingLevel(Item item) {
        int itemId = Registries.ITEM.getRawId(item);
        if (CRAFTING_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = CRAFTING_RESTRICTIONS.get(itemId);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (this.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Identifier, Integer> getRequiredCraftingLevel(Item item) {
        int itemId = Registries.ITEM.getRawId(item);
        if (CRAFTING_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = CRAFTING_RESTRICTIONS.get(itemId);
            return playerRestriction.getSkillLevelRestrictions();
        }
        return null;
    }

    // entity
    public boolean hasRequiredEntityLevel(EntityType<?> entityType) {
        int entityId = Registries.ENTITY_TYPE.getRawId(entityType);
        if (ENTITY_RESTRICTIONS.containsKey(entityId)) {
            PlayerRestriction playerRestriction = ENTITY_RESTRICTIONS.get(entityId);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (this.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Identifier, Integer> getRequiredEntityLevel(EntityType<?> entityType) {
        int entityId = Registries.ENTITY_TYPE.getRawId(entityType);
        if (ENTITY_RESTRICTIONS.containsKey(entityId)) {
            PlayerRestriction playerRestriction = ENTITY_RESTRICTIONS.get(entityId);
            return playerRestriction.getSkillLevelRestrictions();
        }
        return null;
    }

    // item
    public boolean hasRequiredItemLevel(Item item) {
        int itemId = Registries.ITEM.getRawId(item);
        if (ITEM_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = ITEM_RESTRICTIONS.get(itemId);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (this.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Identifier, Integer> getRequiredItemLevel(Item item) {
        int itemId = Registries.ITEM.getRawId(item);
        if (ITEM_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = ITEM_RESTRICTIONS.get(itemId);
            return playerRestriction.getSkillLevelRestrictions();
        }
        return null;
    }

    // mining
    public boolean hasRequiredMiningLevel(Block block) {
        int itemId = Registries.BLOCK.getRawId(block);
        if (MINING_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = MINING_RESTRICTIONS.get(itemId);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (this.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Identifier, Integer> getRequiredMiningLevel(Block block) {
        int itemId = Registries.BLOCK.getRawId(block);
        if (MINING_RESTRICTIONS.containsKey(itemId)) {
            PlayerRestriction playerRestriction = MINING_RESTRICTIONS.get(itemId);
            return playerRestriction.getSkillLevelRestrictions();
        }
        return null;
    }

    // enchantment
    public boolean hasRequiredEnchantmentLevel(RegistryEntry<Enchantment> enchantment, int level) {
        int enchantmentId = EnchantmentRegistry.getId(enchantment, level);
        if (ENCHANTMENT_RESTRICTIONS.containsKey(enchantmentId)) {
            PlayerRestriction playerRestriction = ENCHANTMENT_RESTRICTIONS.get(enchantmentId);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (this.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Identifier, Integer> getRequiredEnchantmentLevel(RegistryEntry<Enchantment> enchantment, int level) {
        int enchantmentId = EnchantmentRegistry.getId(enchantment, level);
        if (ENCHANTMENT_RESTRICTIONS.containsKey(enchantmentId)) {
            PlayerRestriction playerRestriction = ENCHANTMENT_RESTRICTIONS.get(enchantmentId);
            return playerRestriction.getSkillLevelRestrictions();
        }
        return null;
    }

    public boolean resetSkill(Identifier skillId) {
        int level = this.getSkillLevel(skillId);
        if (level > 0) {
            this.setSkillPoints(this.getSkillPoints() + level);
            this.setSkillLevel(skillId, 0);
            PacketHelper.updatePlayerSkills((ServerPlayerEntity) this.playerEntity, null);
            LevelHelper.updateSkill((ServerPlayerEntity) this.playerEntity, SKILLS.get(skillId));
            return true;
        } else {
            return false;
        }
    }

}
