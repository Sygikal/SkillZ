package net.skillz.level;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

public class SkillAttribute {

    private final int index;
    private final RegistryEntry<EntityAttribute> attribute;
    private final float baseValue;
    private final float levelValue;
    private final EntityAttributeModifier.Operation operation;
    private final boolean useBaseValue;

    public SkillAttribute(int index, RegistryEntry<EntityAttribute> attribute, float baseValue, boolean useBaseValue, float levelValue, EntityAttributeModifier.Operation operation) {
        this.index = index;
        this.attribute = attribute;
        this.baseValue = baseValue;
        this.levelValue = levelValue;
        this.operation = operation;
        this.useBaseValue = useBaseValue;
    }

    public int getIndex() {
        return index;
    }

    public RegistryEntry<EntityAttribute> getAttribute() {
        return attribute;
    }

    public float getBaseValue() {
        return baseValue;
    }

    public boolean useBaseValue() {
        return useBaseValue;
    }

    public float getLevelValue() {
        return levelValue;
    }

    public EntityAttributeModifier.Operation getOperation() {
        return operation;
    }

}
