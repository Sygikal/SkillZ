package net.skillz.bonus.impl.player;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class DeathGraceBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("death_grace");

    public DeathGraceBonus() {
        super(ID);
    }
}
