package net.skillz.mixin.entity;

import net.skillz.util.BonusHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

    @Shadow
    @Nullable
    private PlayerEntity lastCustomer;

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    /*@Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;beginTradeWith(Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void interactMobMixin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ArrayList<Object> levelList = LevelLists.villagerList;
        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
            this.sayNo();
            player.sendMessage(Text.translatable("item.levelz." + levelList.get(0) + ".tooltip", levelList.get(1)).formatted(Formatting.RED), true);
            info.setReturnValue(ActionResult.FAIL);
        }
    }*/

    /*@Inject(method = "afterUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    protected void afterUsingMixin(TradeOffer offer, CallbackInfo info, int i) {
        if (ConfigInit.CONFIG.tradingXPMultiplier > 0.0F)
            LevelExperienceOrbEntity.spawn((ServerWorld) this.getWorld(), this.getPos().add(0.0D, 0.5D, 0.0D),
                    (int) (i * ConfigInit.CONFIG.tradingXPMultiplier
                            * (lastCustomer != null ? 1.0F + ((PlayerStatsManagerAccess) lastCustomer).getPlayerStatsManager().getSkillLevel(Skill.TRADE) * ConfigInit.CONFIG.tradeXPBonus : 1.0F)
                            * (ConfigInit.CONFIG.dropXPbasedOnLvl && lastCustomer != null
                                    ? 1.0F + ConfigInit.CONFIG.basedOnMultiplier * ((PlayerStatsManagerAccess) lastCustomer).getPlayerStatsManager().getOverallLevel()
                                    : 1.0F)));
    }*/

    //TODO tradeXpBonus
    @Inject(method = "afterUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    protected void afterUsingMixin(TradeOffer offer, CallbackInfo info, int i) {
        BonusHelper.tradeXpBonus((ServerWorld) this.getWorld(), this.lastCustomer, this, i);
    }

    /*@Inject(method = "prepareOffersFor", at = @At(value = "TAIL"))
    private void prepareOffersForMixin(PlayerEntity player, CallbackInfo info) {
        if (!player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            Iterator<TradeOffer> var5 = this.getOffers().iterator();
            while (var5.hasNext()) {
                TradeOffer tradeOffer2 = (TradeOffer) var5.next();
                tradeOffer2.increaseSpecialPrice(-(int) (((PlayerStatsManagerAccess) player).getPlayerStatsManager().getSkillLevel(Skill.TRADE) * ConfigInit.CONFIG.tradeBonus / 100.0D
                        * tradeOffer2.getOriginalFirstBuyItem().getCount()));
            }

        }
    }*/

    @Inject(method = "prepareOffersFor", at = @At(value = "TAIL"))
    private void prepareOffersForMixin(PlayerEntity player, CallbackInfo info) {
        if (!player.hasStatusEffect(StatusEffects.HERO_OF_THE_VILLAGE)) {
            for (TradeOffer tradeOffer : this.getOffers()) {
                int originalPrice = tradeOffer.getOriginalFirstBuyItem().getCount();
                tradeOffer.increaseSpecialPrice(-(int) (originalPrice - originalPrice * BonusHelper.priceDiscountBonus(player)));
            }
        }
    }

    /*@Inject(method = "setAttacker", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;handleInteraction(Lnet/minecraft/entity/EntityInteraction;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/InteractionObserver;)V"), cancellable = true)
    private void setAttackerMixin(@Nullable LivingEntity attacker, CallbackInfo info) {
        if (attacker != null && attacker instanceof PlayerEntity && ConfigInit.CONFIG.tradeReputation
                && ((PlayerStatsManagerAccess) (PlayerEntity) attacker).getPlayerStatsManager().getSkillLevel(Skill.TRADE) >= ConfigInit.CONFIG.maxLevel) {
            super.setAttacker(attacker);
            info.cancel();
        }
    }*/

    //TODO merchantImmuneBonus
    @Inject(method = "setAttacker", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;handleInteraction(Lnet/minecraft/entity/EntityInteraction;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/InteractionObserver;)V"), cancellable = true)
    private void setAttackerMixin(@Nullable LivingEntity attacker, CallbackInfo info) {
        if (attacker instanceof PlayerEntity playerEntity && BonusHelper.hasBonus("merchantImmune", playerEntity)) {
            super.setAttacker(attacker);
            info.cancel();
        }
    }
}