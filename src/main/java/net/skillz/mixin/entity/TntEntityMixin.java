package net.skillz.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.TNTStrengthBonus;
import net.skillz.init.ConfigInit;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.World.ExplosionSourceType;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {

    @Shadow
    @Nullable
    private LivingEntity causingEntity;

    public TntEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"))
    private Explosion explosionMixin(World instance, Entity entity, double x, double y, double z, float power, ExplosionSourceType explosionSourceType, Operation<Explosion> original) {
        if (causingEntity != null && causingEntity instanceof PlayerEntity playerEntity) {
            power += BonusManager.doBonus(BonusManager.BonusTypes.LINEAR_FLOAT, TNTStrengthBonus.ID, playerEntity, 0.0F, ConfigInit.MAIN.BONUSES.bonusTNTStrength);
        }
        return original.call(instance, entity, x, y, z, power, explosionSourceType);
    }
}
