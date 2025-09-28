package net.skillz.util;

import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.TradeXPBonus;
import net.skillz.bonus.impl.anvil.AnvilXPCapBonus;
import net.skillz.bonus.impl.anvil.AnvilXPDiscountBonus;
import net.skillz.content.entity.LevelExperienceOrbEntity;
import net.skillz.init.ConfigInit;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class BonusHelper {

    public static void tradeXpBonus(ServerWorld serverWorld, @Nullable PlayerEntity playerEntity, MerchantEntity merchantEntity, int amount) {
        amount = (int) (amount * ConfigInit.MAIN.EXPERIENCE.tradingXPMultiplier);
        if (amount > 0) {
            if (playerEntity != null) {
                if (BonusManager.hasBonus(TradeXPBonus.ID, playerEntity)) {
                    amount = (int) (amount * BonusManager.returnBonusValue(TradeXPBonus.ID, playerEntity, 0) * ConfigInit.MAIN.BONUSES.bonusTradeXPPercent);
                }
            }
            LevelExperienceOrbEntity.spawn(serverWorld, merchantEntity.getPos().add(0.0D, 0.5D, 0.0D), amount);
        }
    }

    public static int anvilXpDiscountBonus(PlayerEntity playerEntity, int levelCost) {
        if (levelCost > ConfigInit.MAIN.BONUSES.anvilXPCap && BonusManager.hasBonus(AnvilXPCapBonus.ID, playerEntity)) {
            return ConfigInit.MAIN.BONUSES.anvilXPCap;
        }

        return BonusManager.doBonus(BonusManager.BonusTypes.INVERSE_PERCENTAGE_INT, AnvilXPDiscountBonus.ID, playerEntity, levelCost, ConfigInit.MAIN.BONUSES.anvilXPDiscountPercent);
        //return (int) BonusManager.doInversePercentageFloatBonus(AnvilXPDiscountBonus.ID, playerEntity, levelCost, ConfigInit.MAIN.BONUSES.anvilXPDiscountPercent);
    }

    /*public static boolean nonMeleeSweepingAttackChanceBonus(PlayerEntity playerEntity) {
        if (LevelManager.BONUSES.containsKey("nonMeleeSweepingAttackChance")) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            SkillBonus skillBonus = LevelManager.BONUSES.get("nonMeleeSweepingAttackChance");
            int level = levelManager.getPlayerSkills().get(skillBonus.getId()).getLevel();
            if (level >= skillBonus.getLevel() && playerEntity.getRandom().nextFloat() <= level * ConfigInit.CONFIG.nonMeleeSweepingAttackChance) {
                return true;
            }
        }
        return false;
    }*/


}
