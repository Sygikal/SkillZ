package net.skillz.mixin.misc;

import java.util.Optional;

import net.minecraft.util.ActionResult;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Bucketable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@SuppressWarnings("rawtypes")
@Mixin(Bucketable.class)
public interface BucketableMixin {

    @Inject(method = "Lnet/minecraft/entity/Bucketable;tryBucket(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/entity/LivingEntity;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"), cancellable = true)
    private static <T extends LivingEntity> void tryBucketMixin(PlayerEntity player, Hand hand, T entity, CallbackInfoReturnable<Optional> info) {
        if (SkillZMain.shouldRestrictItem(player, player.getStackInHand(hand).getItem())) {
            info.setReturnValue(Optional.empty());
        }
    }
}
