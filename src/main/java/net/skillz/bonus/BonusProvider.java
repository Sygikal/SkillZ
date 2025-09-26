package net.skillz.bonus;

import net.minecraft.util.Identifier;

public class BonusProvider {
    public final Identifier id;
    public final BonusManager.BonusProviderRunner runner;

    public BonusProvider(Identifier id, BonusManager.BonusProviderRunner runner) {
        this.id = id;
        this.runner = runner;
    }
}
