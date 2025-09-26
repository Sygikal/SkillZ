package net.skillz.bonus.impl.combat.tool;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class CrossbowDamageBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("crossbow_damage");

    public CrossbowDamageBonus() {
        super(ID);
    }

    public float getValue(PlayerEntity playerEntity, float value, float original) {
        return value;
    }
}
