package net.skillz.mixin.player;

import net.skillz.util.BonusHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.skillz.init.ConfigInit;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    /*@Shadow
    private int foodLevel;
    @Shadow
    private float saturationLevel;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 1))
    private void updateStaminaMixin(PlayerEntity player, CallbackInfo info) {
        PlayerStatsManager playerStatsManager = ((PlayerStatsManagerAccess) player).getPlayerStatsManager();
        player.heal((float) playerStatsManager.getSkillLevel(Skill.STAMINA) * ConfigInit.CONFIG.staminaHealthBonus);

    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V", shift = Shift.AFTER, ordinal = 0))
    private void updateAbsorptionMixin(PlayerEntity player, CallbackInfo info) {
        PlayerStatsManager playerStatsManager = ((PlayerStatsManagerAccess) player).getPlayerStatsManager();
        if (player.getMaxHealth() <= player.getHealth() && player.getAbsorptionAmount() <= 0.0F && playerStatsManager.getSkillLevel(Skill.HEALTH) >= ConfigInit.CONFIG.maxLevel)
            player.setAbsorptionAmount(ConfigInit.CONFIG.healthAbsorptionBonus);
    }*/

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;heal(F)V", ordinal = 1))
    private void updateStaminaMixin(PlayerEntity player, CallbackInfo info) {
        BonusHelper.doRunnableBonus("healthRegen", player, (level) -> {
            player.heal(level * ConfigInit.MAIN.BONUSES.healthRegenBonus);
        });
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V", shift = Shift.AFTER, ordinal = 0))
    private void updateAbsorptionMixin(PlayerEntity player, CallbackInfo info) {
        if(BonusHelper.hasBonus("healthAbsorption", player)) {
            player.setAbsorptionAmount(ConfigInit.MAIN.BONUSES.healthAbsorptionBonus);
        }
        //BonusHelper.healthAbsorptionBonus(player);
    }
}