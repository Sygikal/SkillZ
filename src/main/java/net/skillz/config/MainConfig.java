package net.skillz.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import net.skillz.SkillZMain;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

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
        @Comment("Reset all levels on death")
        public boolean hardMode = false;
        public boolean disableMobFarms = true;
        @Comment("Amount of allowed mob kills in a chunk")
        public int mobKillCount = 6;
        @Comment("Strange potion resets all levels instead of one")
        public boolean opStrangePotion = false;
        @Comment("Restrict hand usage when item not unlocked")
        public boolean lockedHandUsage = true;
        @Comment("Restrict block breaking without required mining level")
        public boolean lockedBlockBreaking = true;
        public boolean devMode = false;
    }

    public ProgressionSection PROGRESSION = new ProgressionSection();
    public static class ProgressionSection extends ConfigSection {

        public PopulationSection POPULATION = new PopulationSection();
        public static class PopulationSection extends ConfigSection {
            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("Allow loading of default populations")
            public boolean defaultPopulations = true;

            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("Populators will override restriction data")
            public boolean populatorOverride = true;

            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("List of populator ids to disable")
            public List<String> disabledPopulators = Lists.newArrayList();
        }

        public SkillSection SKILLS = new SkillSection();
        public static class SkillSection extends ConfigSection {
            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("Allow loading of default skills")
            public boolean defaultSkills = true;

            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("Default max level for skills")
            public int defaultMaxLevel = 20;

            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("List of skill ids to disable")
            public List<String> disabledSkills = Lists.newArrayList();
        }

        public RestrictionSection RESTRICTIONS = new RestrictionSection();
        public static class RestrictionSection extends ConfigSection {
            @RequiresAction(action = Action.RELOAD_DATA)
            public boolean enableRestrictions = true;

            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("Allow loading of default restrictions")
            public boolean defaultRestrictions = true;

            @RequiresAction(action = Action.RELOAD_DATA)
            @Comment("List of restriction ids to disable")
            public List<String> disabledRestrictions = Lists.newArrayList();
        }

    }

    public ExperienceSection EXPERIENCE = new ExperienceSection();
    public static class ExperienceSection extends ConfigSection {
        public String xpFormula = "(LVL * LVL) * 0.1 + 50";

        @Comment("0 to disable")
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
        public float bonusBowDamage = 0.1F;
        public float bowDoubleDamageChance = 0.1F;
        public float bonusCrossbowDamage = 0.1F;
        public float crossbowDoubleDamageChance = 0.1F;

        public float keepDurabilityChance = 0.01F;

        public float extraPotionEffectChance = 0.2F;

        public float breedTwinChance = 0.2F;

        public float bonusTNTStrength = 1F;

        public float priceDiscountPercent = 0.01F;
        public float bonusTradeXPPercent = 0.02F;

        public float doubleOreDropChance = 0.01F;
        public float doubleCropDropChance = 0.01F;

        public int anvilXPCap = 30;
        public float anvilXPDiscountPercent = 0.01F;
        public float recoverAnvilXPChance = 0.01F;

        public float healthRegenBonus = 0.025F;
        public float healthAbsorptionBonus = 4F;
        public float exhaustionReductionPercent = 0.02F;
        public float bonusFoodNutrition = 0.02F;
        //public float damageReflectionBonus = 0.02F;
        public float damageReflectionChance = 0.005F;
        public float evadeDamageChance = 0.1F;
        public float fallDamageReductionPercent = 0.2F;
        public float deathGraceChance = 0.2F;

        public float knockbackAttackChance = 0.01F;
        public float criticalAttackChance = 0.01F;
        //public float nonMeleeSweepingAttackChance = 0.01F;
        public float bonusCriticalAttackDamage = 0.3F;
        public float doubleAttackDamageChance = 0.2F;
    }

}
