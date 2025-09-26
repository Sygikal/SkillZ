package net.skillz.bonus.impl;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class TradePriceDiscount extends Bonus {
    public static final Identifier ID = BonusManager.id("trade_price_discount");

    public TradePriceDiscount() {
        super(ID);
    }
}
