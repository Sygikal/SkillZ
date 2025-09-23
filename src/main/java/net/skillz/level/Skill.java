package net.skillz.level;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.util.TextUtil;

import java.util.List;

public record Skill(Identifier id, Identifier texture, int index, int maxLevel, List<SkillAttribute> attributes) {

    public Text getText() {
        return TextUtil.get("skill", id.toString(), "name");
    }

}
