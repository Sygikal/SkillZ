package net.skillz.mixin.misc;

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

    //private int smithingLevel = ((PlayerStatsManagerAccess) player).getPlayerStatsManager().getSkillLevel(Skill.SMITHING);

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    /*@Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    protected void canTakeOutputMixin(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (levelCost.get() <= 0 && (smithingLevel >= ConfigInit.CONFIG.maxLevel || (int) (1F - smithingLevel * ConfigInit.CONFIG.smithingCostBonus) <= 0))
            info.setReturnValue(true);
    }*/

    //TODO: canTakeOutput
    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    protected void canTakeOutputMixin(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> info) {
        if (/*BonusHelper.anvilXpCapBonus(player)*/ /*BonusHelper.doBooleanBonus("anvilXpCap", player)*/ (levelCost.get() <= 0 || BonusHelper.anvilXpDiscountBonus(this.player, this.levelCost.get()) <= 0)) {
            info.setReturnValue(true);
        }
    }

    /*@Inject(method = "Lnet/minecraft/screen/AnvilScreenHandler;updateResult()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 4))
    private void updateResultMixin(CallbackInfo info) {
        if (this.levelCost.get() > 1) {
            int levelCost = (int) (this.levelCost.get() * (1F - smithingLevel * ConfigInit.CONFIG.smithingCostBonus));
            if (levelCost > 30 && smithingLevel >= ConfigInit.CONFIG.maxLevel) {
                this.levelCost.set(30);
            } else
                this.levelCost.set(levelCost < 0 ? 0 : levelCost);
        }
    }*/

    @Inject(method = "updateResult()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 4))
    private void updateResultMixin(CallbackInfo info) {
        //if (this.levelCost.get() > 1) {
            this.levelCost.set(BonusHelper.anvilXpDiscountBonus(this.player, this.levelCost.get()));
        //}
    }

    /*@Inject(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;get()I"), require = 0)
    private void onTakeOutputMixin(PlayerEntity playerEntity, ItemStack stack, CallbackInfo ci) {
        if ((smithingLevel >= ConfigInit.CONFIG.maxLevel) && (ConfigInit.CONFIG.smithingAnvilChance > playerEntity.getWorld().getRandom().nextFloat()))
            levelCost.set(0);
    }*/

    @Inject(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;get()I"), require = 0)
    private void onTakeOutputMixin(PlayerEntity playerEntity, ItemStack stack, CallbackInfo ci) {
        if (/*BonusHelper.anvilXpChanceBonus(playerEntity)*/BonusHelper.doBooleanBonus("anvilXpChance", playerEntity, ConfigInit.MAIN.BONUSES.anvilXpChanceBonus)) {
            this.levelCost.set(0);
        }
    }

    //This discount is rendered in the UI
    @Inject(method = "getLevelCost", at = @At(value = "HEAD"), cancellable = true)
    public void getLevelCostMixin(CallbackInfoReturnable<Integer> info) {
        int levelCost = BonusHelper.anvilXpDiscountBonus(this.player, this.levelCost.get());
        if (levelCost != this.levelCost.get()) {
            info.setReturnValue(levelCost);
        }
    }
}