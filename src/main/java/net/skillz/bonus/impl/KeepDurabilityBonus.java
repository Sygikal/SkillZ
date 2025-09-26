package net.skillz.bonus.impl;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class KeepDurabilityBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("keep_durability");

    public KeepDurabilityBonus() {
        super(ID);
    }
}
