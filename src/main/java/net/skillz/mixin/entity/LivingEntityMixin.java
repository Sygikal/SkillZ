package net.skillz.mixin.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.player.DeathGraceBonus;
import net.skillz.bonus.impl.player.FallDamageReductionBonus;
import net.minecraft.entity.damage.DamageTypes;
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
import net.skillz.content.entity.LevelExperienceOrbEntity;
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

    @ModifyReturnValue(method = "modifyAppliedDamage", at = @At("RETURN"))
    private float fallDamageReduction(float original, @Local(argsOnly = true) DamageSource source) {
        if (source.isOf(DamageTypes.FALL) && entity instanceof PlayerEntity playerEntity) {
            return Math.max(original - BonusManager.doScalingFloatBonus(FallDamageReductionBonus.ID, playerEntity, 0.0F, ConfigInit.MAIN.BONUSES.fallDamageReductionPercent), 0);
        }
        return original;
    }

    @ModifyVariable(method = "tryUseTotem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/LivingEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;", ordinal = 0))
    private ItemStack tryUseTotemMixin(ItemStack original) {
        if (entity instanceof PlayerEntity playerEntity && SkillZMain.shouldRestrictItem(playerEntity, original.getItem())) {
            return ItemStack.EMPTY;
        }
        return original;
    }

    @Inject(method = "tryUseTotem", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"), cancellable = true)
    private void tryUseTotemMixin(DamageSource source, CallbackInfoReturnable<Boolean> info) {
        if (entity instanceof PlayerEntity playerEntity && BonusManager.doLinearBooleanBonus(DeathGraceBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.deathGraceChance)) {
            playerEntity.getWorld().playSound(null, playerEntity.getBlockPos(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.PLAYERS, 0.8F, 1F);

            playerEntity.setHealth(1.0F);
            playerEntity.clearStatusEffects();
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 100, 1));
            playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 0));
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