package net.skillz.level;

import net.minecraft.text.Text;

import java.util.List;

public record Skill(String id, int index, int maxLevel, List<SkillAttribute> attributes) {

    public Text getText() {
        return Text.translatable("skill.skillz." + id);
    }

}
