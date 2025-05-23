package net.skillz.mixin.misc;

import java.util.List;

import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {

    /*@Inject(method = "Lnet/minecraft/entity/mob/PiglinBrain;dropBarteredItem(Lnet/minecraft/entity/mob/PiglinEntity;Lnet/minecraft/entity/player/PlayerEntity;Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
    private static void dropBarteredItemMixin(PiglinEntity piglin, PlayerEntity player, List<ItemStack> items, CallbackInfo info) {
        ArrayList<Object> levelList = LevelLists.piglinList;
        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
            player.sendMessage(Text.translatable("item.skillz." + levelList.get(0) + ".tooltip", levelList.get(1)).formatted(Formatting.RED), true);
            if (!items.isEmpty()) {
                piglin.swingHand(Hand.OFF_HAND);
                LookTargetUtil.give(piglin, new ItemStack(Items.GOLD_INGOT), player.getPos().add(0.0, 1.0, 0.0));
            }
            info.cancel();
        }
    }*/

    /*@Inject(method = "playerInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;", shift = Shift.BEFORE), cancellable = true)
    private static void playerInteractMixin(PiglinEntity piglin, PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ArrayList<Object> levelList = LevelLists.piglinList;
        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
            player.sendMessage(Text.translatable("item.skillz." + levelList.get(0) + ".tooltip", levelList.get(1)).formatted(Formatting.RED), true);
            info.setReturnValue(ActionResult.FAIL);
        }
    }*/

    @Inject(method = "dropBarteredItem(Lnet/minecraft/entity/mob/PiglinEntity;Lnet/minecraft/entity/player/PlayerEntity;Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
    private static void dropBarteredItemMixin(PiglinEntity piglin, PlayerEntity player, List<ItemStack> items, CallbackInfo info) {
        if (player.isCreative()) {
            return;
        }
        LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
        if (!levelManager.hasRequiredEntityLevel(piglin.getType())) {
            player.sendMessage(Text.translatable("restriction.skillz.locked.tooltip").formatted(Formatting.RED), true);
            if (!items.isEmpty()) {
                piglin.swingHand(Hand.OFF_HAND);
                LookTargetUtil.give(piglin, new ItemStack(Items.GOLD_INGOT), player.getPos().add(0.0, 1.0, 0.0));
            }
            info.cancel();
        }
    }
}
