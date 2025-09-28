package net.skillz.compat.waila;

import net.minecraft.registry.Registries;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.init.RenderInit;
import net.skillz.level.LevelManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.screen.SkillRestrictionScreen;
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

        int blockId = Registries.BLOCK.getRawId(accessor.getBlock());
        boolean showLines = accessor.getPlayer().isCreative() || !ConfigInit.CLIENT.hideReachedLevels;

        TooltipUtil.addJadeLines(tooltip, TooltipUtil.USABLE, blockId, levelManager, showLines, LevelManager.BLOCK_RESTRICTIONS);
        TooltipUtil.addJadeLines(tooltip, TooltipUtil.MINEABLE, blockId, levelManager, showLines, LevelManager.MINING_RESTRICTIONS);
    }

}
