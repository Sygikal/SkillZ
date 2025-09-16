package net.skillz.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import net.skillz.SkillZMain;

public class MainConfig extends Config {

    public MainConfig() {
        super(SkillZMain.identifierOf("main_config"));
    }

    public LevelSection LEVEL = new LevelSection();
    public static class LevelSection extends ConfigSection {
        @RequiresAction(action = Action.RESTART)
        @Comment("Maximum level: 0 = disabled")
        public int overallMaxLevel = 0;
        @Comment("In combination with overallMaxLevel, only when all skills maxed")
        public boolean allowHigherSkillLevel = false;
        @RequiresAction(action = Action.RESTART)
        public int startPoints = 5;
        public int pointsPerLevel = 3;
        @Comment("If true will reset stats on death")
        public boolean hardMode = false;
        public boolean disableMobFarms = true;
        @Comment("Amount of allowed mob kills in a chunk")
        public int mobKillCount = 6;
        @Comment("Strange potion resets all stats instead of one")
        public boolean opStrangePotion = false;
        @Comment("Restrict hand usage when item not unlocked")
        public boolean lockedHandUsage = true;
        @Comment("Restrict block breaking without required mining level")
        public boolean lockedBlockBreaking = true;
        @Comment("Only for Devs")
        public boolean devMode = false;
    }

    public ProgressionSection PROGRESSION = new ProgressionSection();
    public static class ProgressionSection extends ConfigSection {
        @RequiresAction(action = Action.RESTART)
        public boolean restrictions = true;
        @RequiresAction(action = Action.RELOAD_DATA)
        @Comment("Allow loading of default restrictions")
        public boolean defaultRestrictions = true;

        @RequiresAction(action = Action.RELOAD_DATA)
        @Comment("Allow loading of default skills")
        public boolean defaultSkills = true;

        @RequiresAction(action = Action.RELOAD_DATA)
        @Comment("Allow loading of default populations")
        public boolean defaultPopulations = true;

    }

    public ExperienceSection EXPERIENCE = new ExperienceSection();
    public static class ExperienceSection extends ConfigSection {
        @Comment("XP equation: lvl^exponent * multiplicator + base")
        public float xpCostMultiplicator = 0.1F;
        public int xpExponent = 2;
        public int xpBaseCost = 50;
        @Comment("0 = no experience cap")
        public int xpMaxCost = 0;
        public boolean resetCurrentXp = true;
        public boolean dropXPbasedOnLvl = false;
        @Comment("0.01 = 1% more xp per lvl")
        public float basedOnMultiplier = 0.01F;
        public float breedingXPMultiplier = 1.0F;
        public float bottleXPMultiplier = 1.0F;
        public float dragonXPMultiplier = 0.5F;
        public float fishingXPMultiplier = 0.8F;
        public float furnaceXPMultiplier = 0.1F;
        public float oreXPMultiplier = 1.0F;
        public float tradingXPMultiplier = 0.3F;
        public float mobXPMultiplier = 1.0F;
        public boolean spawnerMobXP = false;
    }

    public BonusSection BONUSES = new BonusSection();
    public static class BonusSection extends ConfigSection {
        @Comment("Bonus id: bowDamage")
        public float bowDamageBonus = 0.5F;

        @Comment("Bonus id: bowDoubleDamageChance")
        public float bowDoubleDamageChanceBonus = 0.1F;

        @Comment("Bonus id: crossbowDamage")
        public float crossbowDamageBonus = 0.5F;

        @Comment("Bonus id: crossbowDoubleDamageChance")
        public float crossbowDoubleDamageChanceBonus = 0.1F;

        @Comment("Bonus id: itemDamageChance")
        public float itemDamageChanceBonus = 0.01F;

        @Comment("Bonus id: potionEffectChance")
        public float potionEffectChanceBonus = 0.2F;

        @Comment("Bonus id: twinBreedChance")
        public float twinBreedChanceBonus = 0.2F;

        @Comment("Bonus id: fallDamageReduction")
        public float fallDamageReductionBonus = 0.2F;

        @Comment("Bonus id: deathGraceChance")
        public float deathGraceChanceBonus = 0.2F;

        @Comment("Bonus id: tntStrength")
        public float tntStrengthBonus = 1F;

        @Comment("Bonus id: priceDiscount")
        public float priceDiscountBonus = 0.01F;

        @Comment("Bonus id: tradeXp")
        public float tradeXpBonus = 0.02F;

        @Comment("Bonus id: miningDropChance")
        public float miningDropChanceBonus = 0.01F;

        @Comment("Bonus id: plantDropChance")
        public float plantDropChanceBonus = 0.01F;

        @Comment("Bonus id: anvilXpCap")
        public int anvilXpCap = 30;
        @Comment("Bonus id: anvilXpDiscount")
        public float anvilXpDiscountBonus = 0.01F;

        @Comment("Bonus id: anvilXpChance")
        public float anvilXpChanceBonus = 0.01F;

        @Comment("Bonus id: healthRegen")
        public float healthRegenBonus = 0.025F;

        @Comment("Bonus id: healthAbsorption")
        public float healthAbsorptionBonus = 4F;

        @Comment("Bonus id: exhaustionReduction")
        public float exhaustionReductionBonus = 0.02F;

        @Comment("Bonus id: knockbackAttackChance")
        public float meleeKnockbackAttackChanceBonus = 0.01F;

        @Comment("Bonus id: meleeCriticalAttackChance")
        public float meleeCriticalAttackChanceBonus = 0.01F;

        @Comment("Bonus id: nonMeleeSweepingAttackChance")
        public float nonMeleeSweepingAttackChance = 0.01F;

        @Comment("Bonus id: meleeCriticalAttackDamage")
        public float meleeCriticalAttackDamageBonus = 0.3F;

        @Comment("Bonus id: meleeDoubleAttackDamageChance")
        public float meleeDoubleAttackDamageChanceBonus = 0.2F;

        @Comment("Bonus id: foodIncreasion")
        public float foodIncreasionBonus = 0.02F;

        @Comment("Bonus id: damageReflection")
        public float damageReflectionBonus = 0.02F;
        @Comment("Bonus id: damageReflectionChance")
        public float damageReflectionChanceBonus = 0.005F;

        @Comment("Bonus id: evadingDamageChance")
        public float evadingDamageChanceBonus = 0.1F;
    }

}
