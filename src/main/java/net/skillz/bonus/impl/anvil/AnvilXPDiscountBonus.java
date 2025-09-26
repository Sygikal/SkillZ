package net.skillz.bonus.impl.anvil;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class AnvilXPDiscountBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("anvil_xp_discount");

    public AnvilXPDiscountBonus() {
        super(ID);
    }

    /*public float getValue(PlayerEntity playerEntity, float value, float original) {
        return (original * (1.0f - value * ConfigInit.MAIN.BONUSES.anvilXpDiscountBonus));
    }*/
}
