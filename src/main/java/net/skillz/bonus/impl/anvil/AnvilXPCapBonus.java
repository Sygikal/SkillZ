package net.skillz.bonus.impl.anvil;

import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class AnvilXPCapBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("anvil_xp_cap");

    public AnvilXPCapBonus() {
        super(ID);
    }
}
