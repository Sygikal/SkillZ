package net.skillz.data.populate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.util.Identifier;
import net.skillz.level.LevelManager;
import net.skillz.util.TextUtil;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Populator {
    private final Identifier id;
    private List<Identifier> idBlacklist = Lists.newArrayList();

    public Populator(Identifier id) {
        this.id = id;
    }

    public void populate(JsonArray skillObj) {

    }

    public void postPopulate() {

    }

    public Map<Identifier, Integer> getSkillMap(JsonArray skillArray, Identifier currId, FormulaRunner formulaRunner) {
        Map<Identifier, Integer> populatedRestriction = new HashMap<>();

        for (JsonElement elem : skillArray) {
            JsonObject obj = elem.getAsJsonObject();
            Identifier skillKey = Identifier.tryParse(obj.get("skill").getAsString());
            String formula = obj.get("formula").getAsString();

            if (LevelManager.SKILLS.containsKey(skillKey)) {

                for (JsonElement varElem : OptionalObject.get(obj, "variables", new JsonArray()).getAsJsonArray()) {
                    JsonObject varObj = varElem.getAsJsonObject();
                    String varName = varObj.get("name").getAsString();
                    String varType = varObj.get("type").getAsString();
                    float defaultReturn = OptionalObject.get(varObj, "default_return", 0).getAsFloat();
                    float matchedReturn = OptionalObject.get(varObj, "return", 1).getAsFloat();
                    //variableRunner.run(varName);

                    AtomicBoolean skipped = new AtomicBoolean(false);
                    boolean used = false;
                    switch (varType) {
                        case "pattern_match":
                            varObj.get("contains").getAsJsonArray().forEach(contElem -> {
                                if (!currId.toString().contains(contElem.getAsString())) {
                                    skipped.set(true);
                                }
                            });
                            used = true;
                            break;
                        case "in_tag":
                            varObj.get("tags").getAsJsonArray().forEach(contElem -> {
                                if (!handleInTag(currId, Identifier.tryParse(contElem.getAsString()))) {
                                    skipped.set(true);
                                }
                            });
                            used = true;
                            break;
                    }
                    if (used) {
                        formula = formula.replace(varName, String.valueOf(!skipped.get() ?
                                matchedReturn : defaultReturn));
                    }
                }

                formula = formula.replace("SKILL_MAX", String.valueOf(LevelManager.SKILLS.get(skillKey).maxLevel()));
                formula = formulaRunner.run(formula);

                int requirement = Math.round((float) TextUtil.evaluateFormula(formula));
                if (requirement > 0) populatedRestriction.put(skillKey, requirement);
            }
        }

        return populatedRestriction;
    }

    public boolean handleInTag(Identifier currId, Identifier tagId) {
        return false;
    }

    public interface VariableRunner {
        float run(JsonObject varObj, String varName);
    }

    public interface FormulaRunner {
        String run(String formula);
    }

    public Identifier getId() {
        return this.id;
    }

    public void addToIdBlacklist(Identifier id) {
        this.idBlacklist.add(id);
    }

    public List<Identifier> getIdBlacklist() {
        return this.idBlacklist;
    }

    public void setIdBlacklist(List<Identifier> id) {
        this.idBlacklist = id;
    }
}
