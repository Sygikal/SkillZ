package net.skillz.bonus;

import net.minecraft.util.Identifier;

public class BonusCondition {
    public final Identifier id;
    public final BonusManager.BonusRunner runner;

    public BonusCondition(Identifier id, BonusManager.BonusRunner runner) {
        this.id = id;
        this.runner = runner;
    }
}
