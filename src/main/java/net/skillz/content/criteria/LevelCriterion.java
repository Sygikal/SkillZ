package net.skillz.content.criteria;

import com.google.gson.JsonObject;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;

public class LevelCriterion extends AbstractCriterion<LevelCriterion.Conditions> {
    private static final Identifier ID = SkillZMain.identifierOf("level");

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    protected Conditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate lootContextPredicate, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
        NumberPredicate numberPredicate = NumberPredicate.fromJson(jsonObject.get("level"));
        return new Conditions(lootContextPredicate, numberPredicate);
    }

    public void trigger(ServerPlayerEntity player, int level) {
        this.trigger(player, conditions -> conditions.matches(player, level));
    }

    class Conditions extends AbstractCriterionConditions {

        private final NumberPredicate numberPredicate;

        public Conditions(LootContextPredicate lootContextPredicate, NumberPredicate numberPredicate) {
            super(ID, lootContextPredicate);
            this.numberPredicate = numberPredicate;
        }

        public boolean matches(ServerPlayerEntity player, int level) {
            return this.numberPredicate.test(level);
        }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject jsonObject = super.toJson(predicateSerializer);
            jsonObject.add("level", this.numberPredicate.toJson());
            return jsonObject;
        }
    }

}
