package net.skillz.data.populate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.skillz.SkillZMain;
import net.skillz.level.LevelManager;
import net.skillz.util.FileUtil;

import java.util.HashMap;
import java.util.Map;

public class Populator {
    private final Identifier id;

    public Populator(Identifier id) {
        this.id = id;
    }

    public void populate(JsonArray skillObj) {

    }

    public void postPopulate() {

    }

    public Map<String, Integer> getSkillMap(JsonArray skillArray, FormulaRunner formulaRunner) {
        Map<String, Integer> populatedRestriction = new HashMap<>();

        for (JsonElement elem : skillArray) {
            JsonObject obj = elem.getAsJsonObject();
            String skillKey = obj.get("skill").getAsString();
            String formula = obj.get("formula").getAsString();

            if (LevelManager.SKILLS.containsKey(skillKey)) {
                formula = formula.replace("SKILL_MAX", String.valueOf(LevelManager.SKILLS.get(skillKey).maxLevel()));
                formula = formulaRunner.run(formula);

                int requirement = Math.round((float)FileUtil.evaluateFormula(formula));
                if (requirement > 0) populatedRestriction.put(skillKey, requirement);
            }
        }

        return populatedRestriction;
    }

    public interface FormulaRunner {
        String run(String formula);
    }

    public Identifier getId() {
        return this.id;
    }
}
