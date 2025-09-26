package net.skillz.bonus.impl.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;
import net.skillz.init.ConfigInit;

public class ExhaustionReductionBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("exhaustion_reduction");

    public ExhaustionReductionBonus() {
        super(ID);
    }

    public float getValue(PlayerEntity playerEntity, float value, float original) {
        return (1.0f - (value * ConfigInit.MAIN.BONUSES.exhaustionReductionPercent));
    }
}
