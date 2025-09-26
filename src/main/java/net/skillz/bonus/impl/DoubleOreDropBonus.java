package net.skillz.bonus.impl;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class DoubleOreDropBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("double_ore_drop");

    public DoubleOreDropBonus() {
        super(ID);
    }
}
