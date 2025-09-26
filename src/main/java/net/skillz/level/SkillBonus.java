package net.skillz.level;

import net.minecraft.util.Identifier;

import java.util.List;

public class SkillBonus {

    private final Identifier bonusId;
    private final Identifier skillId;
    private final int level;

    public SkillBonus(Identifier bonusId, Identifier skillId, int level) {
        this.bonusId = bonusId;
        this.skillId = skillId;
        this.level = level;
    }

    public Identifier getBonusId() {
        return bonusId;
    }

    public Identifier getSkillId() {
        return skillId;
    }

    public int getLevel() {
        return level;
    }

}
