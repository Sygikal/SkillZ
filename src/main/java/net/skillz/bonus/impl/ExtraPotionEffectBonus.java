package net.skillz.bonus.impl;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class ExtraPotionEffectBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("extra_potion_effect");

    public ExtraPotionEffectBonus() {
        super(ID);
    }
}
