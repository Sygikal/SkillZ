package net.skillz.mixin.misc;

import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.anvil.AnvilXPRecoveryBonus;
import net.skillz.init.ConfigInit;
import net.skillz.util.BonusHelper;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    @Shadow
    @Final
    private Property levelCost;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    //TODO: canTakeOutput
    // &&
    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    protected void canTakeOutputMixin(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (levelCost.get() <= 0 || BonusHelper.anvilXpDiscountBonus(this.player, this.levelCost.get()) <= 0) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "updateResult()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 4))
    private void updateResultMixin(CallbackInfo info) {
        //if (this.levelCost.get() > 1) {
            this.levelCost.set(BonusHelper.anvilXpDiscountBonus(this.player, this.levelCost.get()));
        //}
    }

    @Inject(method = "onTakeOutput", at = @At(value = "HEAD"))
    private void onTakeOutputMixin(PlayerEntity playerEntity, ItemStack stack, CallbackInfo ci) {
        if (BonusManager.doBooleanBonus(AnvilXPRecoveryBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.recoverAnvilXPChance)) {
            this.levelCost.set(0);
        }
    }
}