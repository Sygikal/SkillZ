package net.skillz.bonus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.impl.*;
import net.skillz.bonus.impl.anvil.*;
import net.skillz.bonus.impl.combat.*;
import net.skillz.bonus.impl.combat.tool.BowDamageBonus;
import net.skillz.bonus.impl.combat.tool.BowDoubleDamageBonus;
import net.skillz.bonus.impl.combat.tool.CrossbowDamageBonus;
import net.skillz.bonus.impl.combat.tool.CrossbowDoubleDamageBonus;
import net.skillz.bonus.impl.player.*;
import net.skillz.level.SkillBonus;
import net.skillz.util.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BonusManager {
    public static final Map<Identifier, Bonus> BONUSES = new HashMap<>();
    public static final Map<Identifier, SkillBonus> SKILL_BONUSES = new HashMap<>();

    public static final List<Identifier> VALID_BONUSES = Lists.newArrayList();

    // bonusapi:health_absorption Grant absorption when regenerating with full saturation
    // bonusapi:health_regen Grants `level * config` extra healing when healing from hunger
    // bonusapi:merchant_immunity Grants immunity to reputation decrease and attack call on damaging merchant
    // bonusapi:anvil_xp_cap Caps Anvil XP Cost to `config`
    // bonusapi:anvil_xp_discount Grants inverse `level * config` discount on anvil xp cost
    // bonusapi:anvil_xp_recovery `config` chance to not use XP in anvil
    // bonusapi:double_ore_drop Grants `level * config` chance to drop double ore
    // bonusapi:attack_knockback Grants `level * config` chance to deal knockback
    // bonusapi:critical_attack Each level grants `level * config` chance to critical hit
    // bonusapi:double_attack_damage Grants `level * config` chance to double melee damage
    // bonusapi:death_grace Grants `config` chance to grace death after critical damage
    // bonusapi:evade_damage `config` chance to evade damage
    // bonusapi:trade_price_discount Grants inverse `level * config` discount on  trading
    // bonusapi:tnt_strength Grants `+config` tnt strength
    // bonusapi:fall_damage_reduction Grants `+level * config` fall damage reduction
    // bonusapi:critical_attack_damage Grants `+level * config` damage on critical hit
    // bonusapi:breed_twin Grants `config` chance to breed twins
    // bonusapi:increased_nutrition Grants `level * config` more % nutrition when eating food
    // bonusapi:exhaustion_reduction Grants inverse `level * config` % exhaustion reduction
    // bonusapi:bow_double_damage Grants `config` chance for double bow damage
    // bonusapi:bow_damage: Grants `+level * config` to arrow damage
    // bonusapi:crossbow_damage Grants `+level * config` on damage from crossbow
    // bonusapi:double_crossbow_damage Grants `config` chance for double damage with crossbow
    // bonusapi:keep_durability Grants `level * config` chance to skip durability loss
    // bonusapi:extra_potion_effect Grants `config` chance to increase effect amplifier by one
    // bonusapi:trade_xp Grants `level * config` more Level xp from trading
    // bonusapi:tnt_strength Grants `+config` tnt strength
    // bonusapi:double_crop_drop Grants `level * config` chance to double plant drop
    // bonusapi:damage_reflection Grants `level * config` chance to reflect incoming damage

    public static void init() {
        registerBonus(new BowDamageBonus());
        registerBonus(new HealthAbsorptionBonus());
        registerBonus(new HealthRegenBonus());
        registerBonus(new MerchantImmunityBonus());
        registerBonus(new AnvilXPCapBonus());
        registerBonus(new AnvilXPDiscountBonus());
        registerBonus(new AnvilXPRecoveryBonus());
        registerBonus(new DoubleOreDropBonus());
        registerBonus(new AttackKnockbackBonus());
        registerBonus(new CriticalAttackBonus());
        registerBonus(new DoubleAttackDamageBonus());
        registerBonus(new DeathGraceBonus());
        registerBonus(new EvadeDamageBonus());
        registerBonus(new TradePriceDiscount());
        registerBonus(new TNTStrengthBonus());
        registerBonus(new FallDamageReductionBonus());
        registerBonus(new CriticalAttackDamageBonus());
        registerBonus(new BreedTwinBonus());
        registerBonus(new IncreasedNutritionBonus());
        registerBonus(new ExhaustionReductionBonus());
        registerBonus(new BowDoubleDamageBonus());
        registerBonus(new CrossbowDamageBonus());
        registerBonus(new CrossbowDoubleDamageBonus());
        registerBonus(new KeepDurabilityBonus());
        registerBonus(new ExtraPotionEffectBonus());
        registerBonus(new TradeXPBonus());
        registerBonus(new DoubleCropDropBonus());
        registerBonus(new DamageReflectionBonus());

    }

    public static void clear() {
        SKILL_BONUSES.clear();
        for (Bonus bonus : BONUSES.values()) {
            bonus.conditions.clear();
            bonus.providers.clear();
            bonus.pairMap.clear();
        }
    }

    public static void registerBonus(Bonus bonus) {
        VALID_BONUSES.add(bonus.getId());
        BONUSES.put(bonus.getId(), bonus);
    }

    public static boolean hasBonus(Identifier bonusId, PlayerEntity playerEntity) {
        if (playerEntity != null && VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);
            return bonus.checkConditions(playerEntity);
        }
        return false;
    }

    public static void runBonus(Identifier bonusId, PlayerEntity playerEntity) {
        if (playerEntity != null && VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    bonus.run(playerEntity, p.getRight().runner.run(playerEntity));
                }
            }
            /*if (bonus.checkConditions(playerEntity)) {
                bonus.providsionList().forEach((pro) -> {
                    bonus.run(playerEntity, pro.runner.run(playerEntity));
                });
                //bonus.run(playerEntity, bonus.providsionList());
            }*/
        }
    }

    public static float returnBonusValue(Identifier bonusId, PlayerEntity playerEntity, float original) {
        if (playerEntity != null && VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return bonus.getValue(playerEntity, p.getRight().runner.run(playerEntity), original);
                }
            }
        }
        return original;
    }

    public static float doInversePercentageFloatBonus(Identifier bonusId, PlayerEntity playerEntity, float original, float probability) {
        return BonusManager.doBonus(BonusTypes.INVERSE_PERCENTAGE, bonusId, playerEntity, original, probability);
        /*if (VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return original * (1.0f - p.getRight().runner.run(playerEntity) * probability);
                }
            }
        }
        return original;*/
    }

    public static boolean doBooleanBonus(Identifier bonusId, PlayerEntity playerEntity, float bonusChance) {
        return BonusManager.doBonus(BonusManager.BonusTypes.LINEAR_BOOLEAN, bonusId, playerEntity, false, bonusChance);
        /*if (VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return playerEntity.getRandom().nextFloat() <= p.getRight().runner.run(playerEntity) * bonusChance;
                }
            }
        }
        return false;*/
    }

    /*public static boolean doLinearBooleanBonus(Identifier bonusId, PlayerEntity playerEntity, float bonusChance) {
        if (VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return playerEntity.getRandom().nextFloat() <= bonusChance;
                }
            }
        }
        return false;
    }

    public static float doLinearFloatBonus(Identifier bonusId, PlayerEntity playerEntity, float original, float linearValue) {
        if (VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return linearValue;
                }
            }
        }
        return original;
    }

    public static float doScalingFloatBonus(Identifier bonusId, PlayerEntity playerEntity, float original, float additional) {
        if (VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return p.getRight().runner.run(playerEntity) * additional;
                }
            }
        }
        return original;
    }

    public static float doFloatBonus(Identifier bonusId, PlayerEntity playerEntity, float original, RunningFloat runner) {
        if (VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return runner.run(p.getRight().runner.run(playerEntity));
                }
            }
        }
        return original;
    }*/

    public static <T> T doBonus(BonusType<T> type, Identifier bonusId, PlayerEntity playerEntity, T original, Object... args) {
        if (playerEntity != null && VALID_BONUSES.contains(bonusId)) {
            Bonus bonus = BONUSES.get(bonusId);

            for (Pair<BonusCondition, BonusProvider> p : bonus.pairMap.values()) {
                if (p.getLeft().runner.run(playerEntity)) {
                    return (T) type.runner.run(playerEntity, p.getRight(), original, args);
                }
            }
        }
        return original;
    }

    public static class BonusTypes {
        public static BonusType<Boolean> BOOLEAN = new BonusType<Boolean>((playerEntity, provider, original, args) -> {
            return playerEntity.getRandom().nextFloat() <= provider.runner.run(playerEntity) * (float)args[0];
        });

        public static BonusType<Boolean> LINEAR_BOOLEAN = new BonusType<Boolean>((playerEntity, provider, original, args) -> {
            return playerEntity.getRandom().nextFloat() <= (float)args[0];
        });

        public static BonusType<Float> LINEAR_FLOAT = new BonusType<Float>((playerEntity, provider, original,args) -> {
            return (float)args[0];
        });

        public static BonusType<Float> SCALING_FLOAT = new BonusType<Float>((playerEntity, provider, original,args) -> {
            return provider.runner.run(playerEntity) * (float)args[0];
        });

        public static BonusType<Float> INVERSE_PERCENTAGE = new BonusType<Float>((playerEntity, provider, original, args) -> {
            return original * (1.0f - provider.runner.run(playerEntity) * (float)args[0]);
        });

        public static BonusType<Integer> INVERSE_PERCENTAGE_INT = new BonusManager.BonusType<Integer>((playerEntity, provider, original, args) -> {
            return (int) (original * (1.0f - provider.runner.run(playerEntity) * (float)args[0]));
        });

    }

    public static class BonusType<T> {
        public final BonusTypeRunner<T> runner;

        public BonusType(BonusTypeRunner<T> runner) {
            this.runner = runner;
        }
    }

    public interface BonusTypeRunner<T> {
        T run(PlayerEntity player, BonusProvider provider, T original, Object... args);
    }

    public interface RunningFloat {
        float run(float level);
    }

    public interface BonusRunner {
        boolean run(PlayerEntity player);
    }

    public interface BonusProviderRunner {
        float run(PlayerEntity player);
    }

    public static Identifier id(String key) {
        return Identifier.of("bonusapi", key);
    }
}
