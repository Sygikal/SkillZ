package net.levelz.mixin.entity;

import net.levelz.util.BonusHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    /*@Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/WanderingTraderEntity;setCustomer(Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void interactMobMixin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ArrayList<Object> levelList = LevelLists.wanderingTraderList;
        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
            player.sendMessage(Text.translatable("item.levelz." + levelList.get(0) + ".tooltip", levelList.get(1)).formatted(Formatting.RED), true);
            info.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "afterUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    protected void afterUsingMixin(TradeOffer offer, CallbackInfo info, int i) {
        if (ConfigInit.CONFIG.tradingXPMultiplier > 0.0F)
            LevelExperienceOrbEntity.spawn((ServerWorld) this.getWorld(), this.getPos().add(0.0D, 0.5D, 0.0D), (int) (i * ConfigInit.CONFIG.tradingXPMultiplier
                    * (this.getCustomer() != null ? 1.0F + ((PlayerStatsManagerAccess) this.getCustomer()).getPlayerStatsManager().getSkillLevel(Skill.TRADE) * ConfigInit.CONFIG.tradeXPBonus : 1.0F)
                    * (ConfigInit.CONFIG.dropXPbasedOnLvl && this.getCustomer() != null
                            ? 1.0F + ConfigInit.CONFIG.basedOnMultiplier * ((PlayerStatsManagerAccess) this.getCustomer()).getPlayerStatsManager().getOverallLevel()
                            : 1.0F)));
    }*/

    //TODO tradeXpBonus
    @Inject(method = "afterUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    protected void afterUsingMixin(TradeOffer offer, CallbackInfo info, int i) {
        BonusHelper.tradeXpBonus((ServerWorld) this.getWorld(), this.getCustomer(), this, i);
    }
}
