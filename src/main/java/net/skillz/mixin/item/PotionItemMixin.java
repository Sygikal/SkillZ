package net.skillz.mixin.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.ExtraPotionEffectBonus;
import net.skillz.init.ConfigInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;


import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.world.World;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @ModifyVariable(method = "finishUsing", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/potion/PotionUtil;getPotionEffects(Lnet/minecraft/item/ItemStack;)Ljava/util/List;"), ordinal = 0)
    private List<StatusEffectInstance> finishUsingMixin(List<StatusEffectInstance> original, ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity playerEntity) {
            if (BonusManager.doLinearBooleanBonus(ExtraPotionEffectBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.extraPotionEffectChance)) {
                List<StatusEffectInstance> newEffectList = new ArrayList<>();
                original.forEach(effect -> {
                    newEffectList.add(new StatusEffectInstance(effect.getEffectType(), effect.getDuration(), effect.getAmplifier() + 1, effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()));
                });
                return newEffectList;
            }
        }
        return original;
    }

}
