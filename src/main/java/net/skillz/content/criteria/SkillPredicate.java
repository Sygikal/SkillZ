package net.skillz.content.criteria;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class SkillPredicate {
    private final Identifier jobName;

    public SkillPredicate(Identifier jobName) {
        this.jobName = jobName;
    }

    public boolean test(Identifier jobName) {
        if (this.jobName.equals(jobName)) {
            return true;
        } else {
            return false;
        }
    }

    public static SkillPredicate fromJson(JsonElement json) {
        Identifier jobName = Identifier.tryParse(JsonHelper.asString(json, "skill_id"));
        return new SkillPredicate(jobName);
    }

    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("skill_id", this.jobName.toString());
        return jsonObject;
    }

}
