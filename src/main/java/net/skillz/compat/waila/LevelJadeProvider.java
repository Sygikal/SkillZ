package net.skillz.compat.waila;

import net.skillz.access.LevelManagerAccess;
import net.skillz.init.RenderInit;
import net.skillz.level.LevelManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.skillz.util.TooltipUtil;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Map;

public enum LevelJadeProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return RenderInit.MINEABLE_INFO;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        LevelManager levelManager = ((LevelManagerAccess) accessor.getPlayer()).getLevelManager();
        if (!levelManager.hasRequiredMiningLevel(accessor.getBlock())) {
            for (Map.Entry<Identifier, Integer> entry : levelManager.getRequiredMiningLevel(accessor.getBlock()).entrySet()) {
                tooltip.add(TooltipUtil.getRestrictionKey(entry.getKey(), entry.getValue()).formatted(levelManager.getSkillLevel(entry.getKey()) < entry.getValue() ? Formatting.RED : Formatting.GREEN));
            }
        }
    }

}
