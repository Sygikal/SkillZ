package net.skillz.util;

import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.level.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class LevelHelper {

    public static void updateSkill(ServerPlayerEntity serverPlayerEntity, Skill skill) {
        LevelManager levelManager = ((LevelManagerAccess) serverPlayerEntity).getLevelManager();
        for (SkillAttribute skillAttribute : skill.attributes()) {
            EntityAttributeInstance attr = serverPlayerEntity.getAttributeInstance(skillAttribute.getAttribute().value());
            if (attr != null) {
                Identifier identifier = skill.id();
                UUID uid = UUID.nameUUIDFromBytes(identifier.toString().getBytes());
                if (attr.getModifier(uid) != null && attr.hasModifier(attr.getModifier(uid))) {
                    attr.removeModifier(uid);
                }
                attr.addPersistentModifier(new EntityAttributeModifier(uid, identifier.toString(), skillAttribute.getLevelValue() * levelManager.getSkillLevel(skill.id()), skillAttribute.getOperation()));
                if (skillAttribute.useBaseValue()) {
                    attr.setBaseValue(skillAttribute.getBaseValue());
                }
            }
        }
    }
}
