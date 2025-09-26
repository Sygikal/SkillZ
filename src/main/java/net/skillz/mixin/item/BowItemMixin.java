package net.skillz.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.combat.tool.BowDamageBonus;
import net.skillz.bonus.impl.combat.tool.BowDoubleDamageBonus;
import net.skillz.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getLevel(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onStoppedUsingMixin(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo info, @Local PersistentProjectileEntity persistentProjectileEntity, @Local PlayerEntity playerEntity) {
        if (BonusManager.hasBonus(BowDamageBonus.ID, playerEntity)) {
            double damage = persistentProjectileEntity.getDamage() + (ConfigInit.MAIN.BONUSES.bonusBowDamage * BonusManager.returnBonusValue(BowDamageBonus.ID, playerEntity, 0));
            persistentProjectileEntity.setDamage(damage);
        }

        if (BonusManager.doLinearBooleanBonus(BowDoubleDamageBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.bowDoubleDamageChance)) {
            persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() * 2D);
        }
    }
}