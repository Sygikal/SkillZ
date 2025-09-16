package net.skillz.mixin.entity;

import net.skillz.access.LevelManagerAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.skillz.content.entity.LevelExperienceOrbEntity;
import net.skillz.init.ConfigInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", ordinal = 0))
    private void useMixin(ItemStack usedItem, CallbackInfoReturnable<Integer> info) {
        if (ConfigInit.MAIN.EXPERIENCE.fishingXPMultiplier > 0.0F) {
            LevelExperienceOrbEntity.spawn((ServerWorld) getPlayerOwner().getWorld(), getPlayerOwner().getPos().add(0.0D, 0.5D, 0.0D),
                    (int) ((getPlayerOwner().getWorld().getRandom().nextInt(6) + 1) * ConfigInit.MAIN.EXPERIENCE.fishingXPMultiplier
                            * (ConfigInit.MAIN.EXPERIENCE.dropXPbasedOnLvl && getPlayerOwner() != null
                            ? 1.0F + ConfigInit.MAIN.EXPERIENCE.basedOnMultiplier * ((LevelManagerAccess) getPlayerOwner()).getLevelManager().getOverallLevel()
                            : 1.0F)));
        }
    }

    @Shadow
    @Nullable
    public PlayerEntity getPlayerOwner() {
        return null;
    }
}
