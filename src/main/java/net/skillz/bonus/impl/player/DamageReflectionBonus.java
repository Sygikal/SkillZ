package net.skillz.bonus.impl.player;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class DamageReflectionBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("damage_reflection");

    public DamageReflectionBonus() {
        super(ID);
    }
}
