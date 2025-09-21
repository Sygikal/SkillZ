package net.skillz.mixin.unused;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.EventInit;
import net.skillz.level.LevelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.block.AbstractBlock.AbstractBlockState")
public class ClientPlayerInteractionMixin {

    /*@ModifyVariable(method = "calcBlockBreakingDelta", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getBlockBreakingSpeed(Lnet/minecraft/block/BlockState;)F"), ordinal = 0)
    private int calcBlockBreakingDeltaMixin(int original, BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return (int) (original * ((PlayerBreakBlockAccess) player.getInventory()).getBreakingAbstractBlockDelta());
    }*/

    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    private void useOnBlockMixin(World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(player.getStackInHand(hand).getItem())) {
                player.sendMessage(EventInit.sendRestriction(levelManager.getRequiredItemLevel(player.getStackInHand(hand).getItem()), levelManager), true);
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }
}