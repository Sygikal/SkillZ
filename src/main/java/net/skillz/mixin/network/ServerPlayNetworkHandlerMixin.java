package net.skillz.mixin.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.EventInit;
import net.skillz.level.LevelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @WrapOperation(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;interactBlock(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult useOnBlockMixin(ServerPlayerInteractionManager instance, ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, Operation<ActionResult> original) {
        /*if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(player.getStackInHand(hand).getItem())) {
                player.sendMessage(EventInit.sendRestriction(levelManager.getRequiredItemLevel(player.getStackInHand(hand).getItem()), levelManager), true);
                return ActionResult.PASS;
            }
        }*/
        if (SkillZMain.shouldRestrictItem(player, player.getStackInHand(hand).getItem())) {
            return ActionResult.PASS;
        }
        return original.call(instance, player, world, stack, hand, hitResult);
    }

}
