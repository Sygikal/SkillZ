package net.skillz.bonus.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;
import net.skillz.init.ConfigInit;

public class TNTStrengthBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("tnt_strength");

    public TNTStrengthBonus() {
        super(ID);
    }

}
