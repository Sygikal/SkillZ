package net.skillz.compat.waila;

import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.util.Identifier;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.RenderInit;
import net.skillz.level.LevelManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.skillz.util.TooltipUtil;

import java.util.Map;

public class LevelEntityWTHITProvider implements IEntityComponentProvider {

    @Override
    public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
        IEntityComponentProvider.super.appendBody(tooltip, accessor, config);
        if (config.getBoolean(RenderInit.MINEABLE_INFO)) {
            LevelManager levelManager = ((LevelManagerAccess) accessor.getPlayer()).getLevelManager();
            if (!levelManager.hasRequiredEntityLevel(accessor.getEntity().getType())) {
                for (Map.Entry<Identifier, Integer> entry : levelManager.getRequiredEntityLevel(accessor.getEntity().getType()).entrySet()) {
                    tooltip.addLine(TooltipUtil.getRestrictionKey(entry.getKey(), entry.getValue()).formatted(levelManager.getSkillLevel(entry.getKey()) < entry.getValue() ? Formatting.RED : Formatting.GREEN));
                }
            }
        }
    }

}
