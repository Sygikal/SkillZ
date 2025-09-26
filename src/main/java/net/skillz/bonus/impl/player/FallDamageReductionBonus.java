package net.skillz.bonus.impl.player;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class FallDamageReductionBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("fall_damage_reduction");

    public FallDamageReductionBonus() {
        super(ID);
    }

}
