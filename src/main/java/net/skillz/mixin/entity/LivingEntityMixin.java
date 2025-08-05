package net.skillz.mixin.entity;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import net.skillz.util.BonusHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.skillz.access.MobEntityAccess;
import net.skillz.access.PlayerDropAccess;
import net.skillz.entity.LevelExperienceOrbEntity;
import net.skillz.init.ConfigInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Debug(export=true)
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    protected int playerHitTimer;

    @Shadow
    @Nullable
    protected PlayerEntity attackingPlayer;

    @Unique
    LivingEntity entity = ((LivingEntity)(Object)this);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "modifyAppliedDamage", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getProtectionAmount(Ljava/lang/Iterable;Lnet/minecraft/entity/damage/DamageSource;)I"), ordinal = 0)
    private int modifyAppliedDamageMixin(int original, DamageSource source, float amount) {
        return 1;
    }

    @ModifyArg(method = "modifyAppliedDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/DamageUtil;getInflictedDamage(FF)F"), index = 1)
    private float modifyAppliedDamageMixin2(float damageDealt, @Local DamageSource source) {
        /*if (source == this.getDamageSources().fall() && (Object) this instanceof PlayerEntity player) {
            return (int) (original + ((PlayerStatsManagerAccess) player).getPlayerStatsManager().getSkillLevel(Skill.AGILITY) * ConfigInit.CONFIG.movementFallBonus);
        } else {
            return original;
        }*/
        //TODO fallDamageReductionBonus
        if (source.isOf(DamageTypes.FALL) && entity instanceof PlayerEntity playerEntity) {
            return EnchantmentHelper.getProtectionAmount(this.getArmorItems(), source) + /*BonusHelper.fallDamageReductionBonus(playerEntity)*/
                    BonusHelper.doScalingFloatBonus("fallDamageReduction", playerEntity, 0.0F, ConfigInit.MAIN.BONUSES.fallDamageReductionBonus);
        } else {
            return EnchantmentHelper.getProtectionAmount(this.getArmorItems(), source);
        }
    }

    @ModifyVariable(method = "tryUseTotem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack tryUseTotemMixin(ItemStack original) {
        /*if ((Object) this instanceof PlayerEntity player) {
            ArrayList<Object> levelList = LevelLists.totemList;
            if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
                return ItemStack.EMPTY;
            }
        }
        return original;*/
        //TODO totem 1
        if (entity instanceof PlayerEntity playerEntity && !playerEntity.isCreative() && original.isOf(Items.TOTEM_OF_UNDYING)) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(original.getItem())) {
                return ItemStack.EMPTY;
            }
        }
        return original;
    }

    @Inject(method = "tryUseTotem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"), cancellable = true)
    private void tryUseTotemMixin(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        /*if ((Object) this instanceof PlayerEntity player) {
            if (((PlayerStatsManagerAccess) player).getPlayerStatsManager().getSkillLevel(Skill.LUCK) >= ConfigInit.CONFIG.maxLevel
                    && player.getWorld().getRandom().nextFloat() < ConfigInit.CONFIG.luckSurviveChance) {
                player.setHealth(1.0F);
                player.clearStatusEffects();
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 0));
                info.setReturnValue(true);
            }
        }*/
        //TODO totem 2
        if (entity instanceof PlayerEntity playerEntity && /*BonusHelper.deathGraceChanceBonus(playerEntity)*/
                BonusHelper.doLinearBooleanBonus("deathGraceChance", playerEntity, ConfigInit.MAIN.BONUSES.deathGraceChanceBonus)) {
            playerEntity.setHealth(1.0F);
            playerEntity.clearStatusEffects();
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 600, 0));
            info.setReturnValue(true);
        }
    }

    @Inject(method = "drop", at = @At(value = "HEAD"), cancellable = true)
    protected void dropMixin(DamageSource source, CallbackInfo info) {
        if (!(entity instanceof PlayerEntity) && attackingPlayer != null && this.playerHitTimer > 0 && ConfigInit.MAIN.LEVEL.disableMobFarms
                && !((PlayerDropAccess) attackingPlayer).allowMobDrop()) {
            info.cancel();
        }
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void onDeathMixin(DamageSource source, CallbackInfo info) {
        if (attackingPlayer != null && this.playerHitTimer > 0 && ConfigInit.MAIN.LEVEL.disableMobFarms) {
            ((PlayerDropAccess) attackingPlayer).increaseKilledMobStat(this.getWorld().getChunk(this.getBlockPos()));
        }
    }

    @Inject(method = "dropXp", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"))
    protected void dropXpMixin(CallbackInfo info) {
        if (ConfigInit.MAIN.EXPERIENCE.mobXPMultiplier > 0.0F) {
            if (!ConfigInit.MAIN.EXPERIENCE.spawnerMobXP && (Object) entity instanceof MobEntity mobEntity && ((MobEntityAccess) mobEntity).isSpawnerMob()) {
            } else {
                LevelExperienceOrbEntity.spawn((ServerWorld) this.getWorld(), this.getPos(),
                        (int) (this.getXpToDrop() * ConfigInit.MAIN.EXPERIENCE.mobXPMultiplier
                                * (ConfigInit.MAIN.EXPERIENCE.dropXPbasedOnLvl && this.attackingPlayer != null
                                        ? 1.0F + ConfigInit.MAIN.EXPERIENCE.basedOnMultiplier * ((LevelManagerAccess) this.attackingPlayer).getLevelManager().getOverallLevel()
                                        : 1.0F)));
            }
        }
    }

    @Shadow
    protected int getXpToDrop() {
        return 0;
    }

}