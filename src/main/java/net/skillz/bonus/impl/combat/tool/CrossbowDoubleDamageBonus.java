package net.skillz.bonus.impl.combat.tool;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class CrossbowDoubleDamageBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("crossbow_double_damage");

    public CrossbowDoubleDamageBonus() {
        super(ID);
    }
}
