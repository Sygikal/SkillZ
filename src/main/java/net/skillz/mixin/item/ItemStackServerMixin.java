package net.skillz.mixin.item;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.skillz.SkillZMain;
import net.skillz.access.ItemStackAccess;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.EventInit;
import net.skillz.level.LevelManager;
import net.skillz.util.BonusHelper;
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

    /*@ModifyVariable(method = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"), ordinal = 1)
    private int damageMixin(int original, int amount, Random random, @Nullable ServerPlayerEntity player) {
        if (player != null) {
            if ((float) ((PlayerStatsManagerAccess) player).getPlayerStatsManager().getSkillLevel(Skill.SMITHING) * ConfigInit.CONFIG.smithingToolChance > random.nextFloat()) {
                return original + 1;
            }
        }
        return original;
    }

    @Inject(method = "damage(ILnet/minecraft/server/world/ServerWorld;Lnet/minecraft/server/network/ServerPlayerEntity;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getItemDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;I)I"), cancellable = true)
    private void damageMixin(int amount, ServerWorld world, @Nullable ServerPlayerEntity player, Consumer<Item> breakCallback, CallbackInfo info) {
        if (BonusHelper.itemDamageChanceBonus(player)) {
            info.cancel();
        }
    }*/

    //TODO damageMixin
    @Inject(method = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/util/math/random/Random;Lnet/minecraft/server/network/ServerPlayerEntity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"), cancellable = true)
    private void damageMixin(int amount, Random random, ServerPlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (BonusHelper.itemDamageChanceBonus(player)) {
            System.out.println("Damage has been nulled");
            cir.cancel();
        }
    }

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void useOnBlockMixin(ItemUsageContext context, CallbackInfoReturnable<ActionResult> info) {
        PlayerEntity player = context.getPlayer();
        /*if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(player.getStackInHand(context.getHand()).getItem())) {
                player.sendMessage(EventInit.sendRestriction(levelManager.getRequiredItemLevel(player.getStackInHand(context.getHand()).getItem()), levelManager), true);
                info.setReturnValue(ActionResult.PASS);
            }
        }*/
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
