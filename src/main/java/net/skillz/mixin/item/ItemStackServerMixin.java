package net.skillz.mixin.item;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.skillz.SkillZMain;
import net.skillz.access.ItemStackAccess;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.KeepDurabilityBonus;
import net.skillz.init.ConfigInit;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackServerMixin implements ItemStackAccess {

    private PlayerEntity holdingPlayer = null;

    @Inject(method = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"), cancellable = true)
    private void damageMixin(int amount, Random random, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (BonusManager.doBooleanBonus(KeepDurabilityBonus.ID, player, ConfigInit.MAIN.BONUSES.keepDurabilityChance)) {
            cir.cancel();
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlockMixin(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        PlayerEntity player = context.getPlayer();
        if (player != null && SkillZMain.shouldRestrictItem(player, player.getStackInHand(context.getHand()).getItem() )) {
            info.setReturnValue(ActionResult.PASS);
        }
    }

    @Override
    public PlayerEntity getHoldingPlayer() {
        return holdingPlayer;
    }

    @Override
    public void setHoldingPlayer(PlayerEntity player) {
        this.holdingPlayer = player;
        //return (ItemStack) (Object) this;
    }
}
