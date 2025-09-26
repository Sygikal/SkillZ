package net.skillz.bonus.impl.combat.tool;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class BowDoubleDamageBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("bow_double_damage");

    public BowDoubleDamageBonus() {
        super(ID);
    }
}
