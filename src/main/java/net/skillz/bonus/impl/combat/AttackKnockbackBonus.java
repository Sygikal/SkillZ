package net.skillz.bonus.impl.combat;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class AttackKnockbackBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("attack_knockback");

    public AttackKnockbackBonus() {
        super(ID);
    }
}
