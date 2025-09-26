package net.skillz.bonus.impl.player;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class MerchantImmunityBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("merchant_immunity");

    public MerchantImmunityBonus() {
        super(ID);
    }
}
