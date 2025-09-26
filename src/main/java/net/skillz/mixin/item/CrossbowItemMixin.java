package net.skillz.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.combat.tool.CrossbowDamageBonus;
import net.skillz.bonus.impl.combat.tool.CrossbowDoubleDamageBonus;
import net.skillz.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(method = "createArrow", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void createArrowMixin(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow, CallbackInfoReturnable<PersistentProjectileEntity> info, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        if (entity instanceof PlayerEntity playerEntity) {
            if (BonusManager.hasBonus(CrossbowDamageBonus.ID, playerEntity)) {
                double damage = persistentProjectileEntity.getDamage() + (ConfigInit.MAIN.BONUSES.bonusCrossbowDamage * BonusManager.returnBonusValue(CrossbowDamageBonus.ID, playerEntity, 0));
                persistentProjectileEntity.setDamage(damage);
            }

            if (BonusManager.doLinearBooleanBonus(CrossbowDoubleDamageBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.crossbowDoubleDamageChance)) {
                persistentProjectileEntity.setDamage(persistentProjectileEntity.getDamage() * 2D);
            }
        }
    }
}
