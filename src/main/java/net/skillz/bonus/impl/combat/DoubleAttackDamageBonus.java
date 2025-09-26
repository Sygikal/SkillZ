package net.skillz.bonus.impl.combat;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class DoubleAttackDamageBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("double_attack_damage");

    public DoubleAttackDamageBonus() {
        super(ID);
    }
}
