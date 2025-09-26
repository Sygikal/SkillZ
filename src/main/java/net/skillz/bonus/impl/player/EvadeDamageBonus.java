package net.skillz.bonus.impl.player;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class EvadeDamageBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("evade_damage");

    public EvadeDamageBonus() {
        super(ID);
    }
}
