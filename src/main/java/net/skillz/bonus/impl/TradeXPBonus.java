package net.skillz.bonus.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class TradeXPBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("trade_xp");

    public TradeXPBonus() {
        super(ID);
    }

    public float getValue(PlayerEntity playerEntity, float value, float original) {
        return value;
    }
}
