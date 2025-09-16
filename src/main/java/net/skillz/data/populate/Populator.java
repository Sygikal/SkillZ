package net.skillz.data.populate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sygii.ultralib.data.util.OptionalObject;
import net.minecraft.util.Identifier;
import net.skillz.level.LevelManager;

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

    public Map<String, Integer> getSkillMap(JsonArray skillArray, int restriction) {
        Map<String, Integer> populatedRestriction = new HashMap<>();

        for (JsonElement elem : skillArray) {
            JsonObject obj = elem.getAsJsonObject();
            String skillKey = obj.get("skill").getAsString();
            int base = obj.get("base").getAsInt();
            int multiply = OptionalObject.get(obj, "multiply", 1).getAsInt();
            int divide = OptionalObject.get(obj, "divide", 1).getAsInt();

            if (LevelManager.SKILLS.containsKey(skillKey)) {
                int max = OptionalObject.get(obj, "max", LevelManager.SKILLS.get(skillKey).maxLevel()).getAsInt();

                int requirement = Math.min(base + ((restriction * multiply) / divide), max);
                if (requirement > 0) populatedRestriction.put(skillKey, requirement);
            }
        }

        return populatedRestriction;
    }

    public Identifier getId() {
        return this.id;
    }
}
