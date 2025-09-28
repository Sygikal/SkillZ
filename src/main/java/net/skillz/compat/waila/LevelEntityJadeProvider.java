package net.skillz.compat.waila;

import net.minecraft.registry.Registries;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.init.RenderInit;
import net.skillz.level.LevelManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.skillz.util.TooltipUtil;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.Map;

public enum LevelEntityJadeProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return RenderInit.USABLE_INFO;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
        LevelManager levelManager = ((LevelManagerAccess) entityAccessor.getPlayer()).getLevelManager();

        boolean showLines = entityAccessor.getPlayer().isCreative() || !ConfigInit.CLIENT.hideReachedLevels;
        TooltipUtil.addJadeLines(tooltip, TooltipUtil.USABLE, Registries.ENTITY_TYPE.getRawId(entityAccessor.getEntity().getType()), levelManager, showLines, LevelManager.ENTITY_RESTRICTIONS);
    }
}
