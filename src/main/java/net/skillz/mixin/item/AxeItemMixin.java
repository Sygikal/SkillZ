package net.skillz.mixin.item;

import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

@Mixin(AxeItem.class)
public class AxeItemMixin {

    /*@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlockMixin(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        PlayerEntity player = context.getPlayer();
        if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(player.getStackInHand(context.getHand()).getItem())) {
                player.sendMessage(Text.translatable("restriction.skillz.locked.tooltip").formatted(Formatting.RED), true);
                info.setReturnValue(ActionResult.PASS);
            }
        }
    }*/
}
