package net.skillz.mixin.item;

import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(SwordItem.class)
public class SwordItemMixin {

    @Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
    private void postHitMixin(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> info) {
        if (attacker instanceof PlayerEntity playerEntity) {
            if (SkillZMain.shouldRestrictItem(playerEntity, stack.getItem())) {
                System.out.print("SwordItemMixin called?");
                info.setReturnValue(false);
            }
        }

    }

    @Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
    private void postMineMixin(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity attacker, CallbackInfoReturnable<Boolean> info) {
        if (attacker instanceof PlayerEntity playerEntity) {
            if (SkillZMain.shouldRestrictItem(playerEntity, stack.getItem())) {
                System.out.print("SwordItemMixin2 called?");
                info.setReturnValue(false);
            }
        }
    }

}
