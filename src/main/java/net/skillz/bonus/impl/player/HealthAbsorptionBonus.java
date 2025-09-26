package net.skillz.bonus.impl.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;
import net.skillz.init.ConfigInit;

public class HealthAbsorptionBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("health_absorption");

    public HealthAbsorptionBonus() {
        super(ID);
    }

    @Override
    public void run(PlayerEntity player, float value) {
        player.setAbsorptionAmount(ConfigInit.MAIN.BONUSES.healthAbsorptionBonus);
    }
}
