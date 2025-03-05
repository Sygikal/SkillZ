package net.levelz.util;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.fabric.impl.tag.convention.TagRegistration;
import net.levelz.access.LevelManagerAccess;
import net.levelz.entity.LevelExperienceOrbEntity;
import net.levelz.init.ConfigInit;
import net.levelz.init.TagInit;
import net.levelz.level.LevelManager;
import net.levelz.level.SkillBonus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BonusHelper {

    public static boolean doBooleanBonus(String bonnusKey, PlayerEntity playerEntity, float bonusChance) {
        if (LevelManager.BONUSES.containsKey(bonnusKey)) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get(bonnusKey);
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * bonusChance) {
                return true;
            }
        }
        return false;
    }

    public interface Running {
        void run(int level);
    }

    public static void doRunnableBonus(String bonnusKey, PlayerEntity playerEntity, Running runner) {
        if (LevelManager.BONUSES.containsKey(bonnusKey)) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get(bonnusKey);
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                runner.run(level);
            }
        }
    }

    public static void bowBonus(PlayerEntity playerEntity, PersistentProjectileEntity persistentProjectileEntity) {
        LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
        if (LevelManager.BONUSES.containsKey("bowDamage")) {
            SkillBonus skillBonus = LevelManager.BONUSES.get("bowDamage");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() + ConfigInit.MAIN.BONUSES.bowDamageBonus * level);
            }
        }
        if (LevelManager.BONUSES.containsKey("bowDoubleDamageChance")) {
            SkillBonus skillBonus = LevelManager.BONUSES.get("bowDoubleDamageChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.bowDoubleDamageChanceBonus) {
                persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() * 2D);
            }
        }
    }

    public static void crossbowBonus(LivingEntity entity, PersistentProjectileEntity persistentProjectileEntity) {
        if (entity instanceof PlayerEntity playerEntity) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            if (LevelManager.BONUSES.containsKey("crossbowDamage")) {
                SkillBonus skillBonus = LevelManager.BONUSES.get("crossbowDamage");
                int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                if (level >= skillBonus.getLevel()) {
                    persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() + ConfigInit.MAIN.BONUSES.crossbowDamageBonus * level);
                }
            }
            if (LevelManager.BONUSES.containsKey("crossbowDoubleDamageChance")) {
                SkillBonus skillBonus = LevelManager.BONUSES.get("crossbowDoubleDamageChance");
                int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.crossbowDoubleDamageChanceBonus) {
                    persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() * 2D);
                }
            }
        }
    }

    public static boolean itemDamageChanceBonus(@Nullable PlayerEntity playerEntity) {
        if (playerEntity != null && LevelManager.BONUSES.containsKey("itemDamageChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("itemDamageChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.itemDamageChanceBonus * level) {
                return true;
            }
        }
        return false;
    }

    /*public static StatusEffectInstance potionEffectChanceBonus(@Nullable PlayerEntity playerEntity, StatusEffectInstance statusEffectInstance) {
        if (playerEntity != null && LevelManager.BONUSES.containsKey("potionEffectChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("potionEffectChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.CONFIG.potionEffectChanceBonus) {
                return new StatusEffectInstance(statusEffectInstance.getEffectType(), statusEffectInstance.getDuration(),
                        statusEffectInstance.getAmplifier() + 1, statusEffectInstance.isAmbient(),
                        statusEffectInstance.shouldShowParticles(), statusEffectInstance.shouldShowIcon());
            }
        }
        return statusEffectInstance;
    }*/

    public static List<StatusEffectInstance> potionEffectChanceBonus(List<StatusEffectInstance> original, ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity playerEntity) {
            if (LevelManager.BONUSES.containsKey("potionEffectChance")) {
                LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
                SkillBonus skillBonus = LevelManager.BONUSES.get("potionEffectChance");
                int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.potionEffectChanceBonus) {
                    List<StatusEffectInstance> newEffectList = new ArrayList<>();
                    original.forEach(effect -> {
                        newEffectList.add(new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier() + 1, effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()));
                    });
                    return newEffectList;
                }
            }
        }
        return original;
    }

    /*public static void breedTwinChanceBonus(ServerWorld world, PlayerEntity playerEntity, PassiveEntity animalEntity, PassiveEntity otherAnimalEntity) {
        if (LevelManager.BONUSES.containsKey("breedTwinChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("breedTwinChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.CONFIG.twinBreedChanceBonus) {
                PassiveEntity extraPassiveEntity = animalEntity.createChild(world, otherAnimalEntity);
                extraPassiveEntity.setBaby(true);
                extraPassiveEntity.refreshPositionAndAngles(animalEntity.getX(), animalEntity.getY(), animalEntity.getZ(), playerEntity.getRandom().nextFloat() * 360F, 0.0F);
                world.spawnEntityAndPassengers(extraPassiveEntity);
            }
        }
    }*/

    public static float fallDamageReductionBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("fallDamageReduction")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("fallDamageReduction");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return level * ConfigInit.MAIN.BONUSES.fallDamageReductionBonus;
            }
        }
        return 0.0f;
    }

    public static boolean deathGraceChanceBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("deathGraceChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("deathGraceChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.deathGraceChanceBonus) {
                playerEntity.setHealth(1.0F);
                playerEntity.clearStatusEffects();
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 0));
                return true;
            }
        }

        return false;
    }

    public static float tntStrengthBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("tntStrength")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("tntStrength");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return ConfigInit.MAIN.BONUSES.tntStrengthBonus;
            }
        }
        return 0.0f;
    }

    public static float priceDiscountBonus(PlayerEntity playerEntity) {
        if (playerEntity.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            return 1.0f;
        }
        if (LevelManager.BONUSES.containsKey("priceDiscount")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("priceDiscount");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return 1.0f - (level * ConfigInit.MAIN.BONUSES.priceDiscountBonus);
            }
        }
        return 1.0f;
    }

    public static void tradeXpBonus(ServerWorld serverWorld, @Nullable PlayerEntity playerEntity, MerchantEntity merchantEntity, int amount) {
        amount = (int) (amount * ConfigInit.MAIN.EXPERIENCE.tradingXPMultiplier);
        if (amount > 0) {
            if (playerEntity != null) {
                if (LevelManager.BONUSES.containsKey("tradeXp")) {
                    LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
                    SkillBonus skillBonus = LevelManager.BONUSES.get("tradeXp");
                    int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                    if (level >= skillBonus.getLevel()) {
                        amount = (int) (amount * level * ConfigInit.MAIN.BONUSES.tradeXpBonus);
                    }
                }
            }
            LevelExperienceOrbEntity.spawn(serverWorld, merchantEntity.getPos().add(0.0D, 0.5D, 0.0D), amount);
            // Todo: HERE
            // ? 1.0F + ConfigInit.CONFIG.basedOnMultiplier * ((PlayerStatsManagerAccess) lastCustomer).getPlayerStatsManager().getOverallLevel()
        }
    }

    public static boolean merchantImmuneBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("merchantImmune")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("merchantImmune");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return true;
            }
        }
        return false;
    }

    public static void miningDropChanceBonus(PlayerEntity playerEntity, BlockState state, BlockPos pos, LootContextParameterSet.Builder builder) {
        if (state.isIn(ConventionalBlockTags.ORES) && EnchantmentHelper.getEquipmentLevel(Enchantments.SILK_TOUCH, playerEntity) <= 0) {
            if (LevelManager.BONUSES.containsKey("miningDropChance")) {
                LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
                SkillBonus skillBonus = LevelManager.BONUSES.get("miningDropChance");
                int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.MAIN.BONUSES.miningDropChanceBonus) {
                    List<ItemStack> list = state.getDroppedStacks(builder);
                    if (!list.isEmpty()) {
                        Block.dropStack(playerEntity.getWorld(), pos, state.getDroppedStacks(builder).get(0).split(1));
                    }
                }
            }
        }
    }

    public static void plantDropChanceBonus(PlayerEntity playerEntity, BlockState state, BlockPos pos) {
        if (EnchantmentHelper.getEquipmentLevel(Enchantments.SILK_TOUCH, playerEntity) <= 0) {
            if (LevelManager.BONUSES.containsKey("plantDropChance")) {
                LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
                SkillBonus skillBonus = LevelManager.BONUSES.get("plantDropChance");
                int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.MAIN.BONUSES.plantDropChanceBonus) {
                    List<ItemStack> list = Block.getDroppedStacks(state, (ServerWorld) playerEntity.getWorld(), pos, null);
                    for (ItemStack itemStack : list) {
                        if (itemStack.isIn(TagInit.FARM_ITEMS)) {
                            Block.dropStack(playerEntity.getWorld(), pos, itemStack);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static boolean anvilXpCapBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("anvilXpCap")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("anvilXpCap");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return true;
            }
        }
        return false;
    }

    public static int anvilXpDiscountBonus(PlayerEntity playerEntity, int levelCost) {
        if (levelCost > ConfigInit.MAIN.BONUSES.anvilXpCap && anvilXpCapBonus(playerEntity)) {
            return ConfigInit.MAIN.BONUSES.anvilXpCap;
        }
        if (LevelManager.BONUSES.containsKey("anvilXpDiscount")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("anvilXpDiscount");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return (int) (levelCost * (1.0f - level * ConfigInit.MAIN.BONUSES.anvilXpDiscountBonus));
            }
        }
        return levelCost;
    }

    public static boolean anvilXpChanceBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("anvilXpChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("anvilXpChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.MAIN.BONUSES.anvilXpChanceBonus) {
                return true;
            }
        }
        return false;
    }

    /*public static void healthRegenBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("healthRegen")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("healthRegen");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                playerEntity.heal(level * ConfigInit.CONFIG.healthRegenBonus);
            }
        }
    }*/

    public static void healthAbsorptionBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("healthAbsorption")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("healthAbsorption");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                playerEntity.setAbsorptionAmount(ConfigInit.MAIN.BONUSES.healthAbsorptionBonus);
            }
        }
    }

    public static float exhaustionReductionBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("exhaustionReduction")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("exhaustionReduction");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return 1.0f - (level * ConfigInit.MAIN.BONUSES.exhaustionReductionBonus);
            }
        }
        return 0.0f;
    }

    public static boolean meleeKnockbackAttackChanceBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("meleeKnockbackAttackChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("meleeKnockbackAttackChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.MAIN.BONUSES.meleeKnockbackAttackChanceBonus) {
                return true;
            }
        }
        return false;
    }

    /*public static boolean meleeCriticalAttackChanceBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("meleeCriticalAttackChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("meleeCriticalAttackChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.CONFIG.meleeCriticalAttackChanceBonus) {
                return true;
            }
        }
        return false;
    }

    public static boolean nonMeleeSweepingAttackChanceBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("nonMeleeSweepingAttackChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("nonMeleeSweepingAttackChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.CONFIG.nonMeleeSweepingAttackChance) {
                return true;
            }
        }
        return false;
    }*/

    public static float meleeCriticalDamageBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("meleeCriticalAttackDamage")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("meleeCriticalAttackDamage");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                return level * ConfigInit.MAIN.BONUSES.meleeCriticalAttackDamageBonus;
            }
        }
        return 0.0f;
    }

    public static boolean meleeDoubleDamageBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("meleeDoubleAttackDamageChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("meleeDoubleAttackDamageChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.meleeDoubleAttackDamageChanceBonus) {
                return true;
            }
        }
        return false;
    }

    public static void foodIncreasionBonus(PlayerEntity playerEntity, ItemStack itemStack) {
        if (LevelManager.BONUSES.containsKey("foodIncreasion") && itemStack.getItem().isFood()) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("foodIncreasion");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel()) {
                net.minecraft.item.FoodComponent foodComponent = itemStack.getItem().getFoodComponent();
                float multiplier = level * ConfigInit.MAIN.BONUSES.foodIncreasionBonus;
                playerEntity.getHungerManager().add((int) (foodComponent.getHunger() * multiplier), foodComponent.getSaturationModifier() * multiplier);
            }
        }
    }

    public static void damageReflectionBonus(PlayerEntity playerEntity, DamageSource source, float amount) {
        if (source.getAttacker() != null && LevelManager.BONUSES.containsKey("damageReflection") && LevelManager.BONUSES.containsKey("damageReflectionChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("damageReflectionChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.MAIN.BONUSES.damageReflectionChanceBonus) {
                skillBonus = LevelManager.BONUSES.get("damageReflection");
                level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
                if (level >= skillBonus.getLevel()) {
                    source.getAttacker().damage(source, amount * level * ConfigInit.MAIN.BONUSES.damageReflectionBonus);
                }
            }
        }
    }

    public static boolean evadingDamageBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("evadingDamageChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("evadingDamageChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.evadingDamageChanceBonus) {
                return true;
            }
        }
        return false;
    }


}
