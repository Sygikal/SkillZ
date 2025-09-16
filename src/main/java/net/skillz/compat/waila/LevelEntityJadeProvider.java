package net.skillz.compat.waila;

import net.skillz.access.LevelManagerAccess;
import net.skillz.init.RenderInit;
import net.skillz.level.LevelManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Map;

public enum LevelEntityJadeProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return RenderInit.MINEABLE_INFO;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        LevelManager levelManager = ((LevelManagerAccess) entityAccessor.getPlayer()).getLevelManager();
        if (!levelManager.hasRequiredEntityLevel(entityAccessor.getEntity().getType())) {
            for (Map.Entry<String, Integer> entry : levelManager.getRequiredEntityLevel(entityAccessor.getEntity().getType()).entrySet()) {
                Formatting formatting =
                        levelManager.getSkillLevel(entry.getKey()) < entry.getValue() ? Formatting.RED : Formatting.GREEN;
                tooltip.add(Text.translatable("restriction.skillz." + LevelManager.SKILLS.get(entry.getKey()).id() + ".tooltip", entry.getValue()).formatted(formatting));
            }
        }
    }
}
