package net.skillz.bonus.impl.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.bonus.Bonus;
import net.skillz.bonus.BonusManager;

public class IncreasedNutritionBonus extends Bonus {
    public static final Identifier ID = BonusManager.id("increased_nutrition");

    public IncreasedNutritionBonus() {
        super(ID);
    }

    public float getValue(PlayerEntity playerEntity, float value, float original) {
        return value;
    }
}
