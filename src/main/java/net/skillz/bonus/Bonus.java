package net.skillz.bonus;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bonus {
    private final Identifier id;

    public final Map<Identifier, BonusCondition> conditions = new HashMap<>();
    public final Map<Identifier, BonusProvider> providers = new HashMap<>();

    public final Map<Identifier, Pair<BonusCondition, BonusProvider>> pairMap = new HashMap<>();


    public Bonus(Identifier id) {
        this.id = id;
    }

    public void run(PlayerEntity playerEntity, float value) {

    }

    public float getValue(PlayerEntity playerEntity, float value, float original) {
        return original;
    }

    public boolean checkConditions(PlayerEntity player) {
        for (BonusCondition condition : conditions.values()) {
            if (!condition.runner.run(player)) {
                return false;
            }
        }
        return true;
    }

    public void registerProvisions(Identifier id, BonusCondition condition, BonusProvider provider, boolean reload) {
        if (conditions.get(condition.id) == null || reload) {
            conditions.put(condition.id, condition);
        }

        if (providers.get(provider.id) == null || reload) {
            providers.put(provider.id, provider);
        }

        if (pairMap.get(id) == null || reload) {
            pairMap.put(id, Pair.of(condition, provider));
        }
    }

    /*public void registerProvider(BonusProvider provider, boolean reload) {
        if (providers.get(provider.id) != null || reload) {
            providers.put(provider.id, provider);
        }
    }*/

    public List<BonusProvider> providsionList() {
        return (List<BonusProvider>) this.providers.values();
    }

    public Identifier getId() {
        return this.id;
    }
}
