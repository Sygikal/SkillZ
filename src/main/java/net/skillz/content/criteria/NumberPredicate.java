package net.skillz.content.criteria;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

public class NumberPredicate {
    private final int level;

    public NumberPredicate(int level) {
        this.level = level;
    }

    public boolean test(int level) {
        if (this.level == 0 || this.level == level) {
            return true;
        } else {
            return false;
        }
    }

    public static NumberPredicate fromJson(JsonElement json) {
        int level = JsonHelper.asInt(json, "level");
        return new NumberPredicate(level);
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("level", (Number) this.level);
        return jsonObject;
    }

}
