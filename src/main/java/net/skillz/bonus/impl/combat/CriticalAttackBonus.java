package net.skillz.bonus.impl.combat;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class CriticalAttackBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("critical_attack");

    public CriticalAttackBonus() {
        super(ID);
    }
}
