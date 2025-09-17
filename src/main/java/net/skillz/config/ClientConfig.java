package net.skillz.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.skillz.SkillZMain;

public class ClientConfig extends Config {

    public ClientConfig() {
        super(SkillZMain.identifierOf("client_config"));
    }

    @Comment("Hide reached levels in tooltips")
    public boolean hideReachedLevels = true;

    @Comment("How locked blocks should appear highlighted")
    public BlockHighlightOption highlightOption = BlockHighlightOption.NORMAL;

    public boolean inventorySkillLevel = true;

    public int inventorySkillLevelPosX = 0;

    public int inventorySkillLevelPosY = 0;

    @RequiresAction(action = Action.RESTART)
    public boolean showLevelList = true;

    public boolean showLevel = true;

    @Comment("Inventory key goes back to main screen rather than closing the inventory")
    public boolean switchScreen = true;

    @Comment("Show tooltip on the hud for restrictions (should only use if you dont have jade)")
    public boolean showRestrictionOnHUD = false;

    public int hudInfoX = 0;

    public int hudInfoY = 0;

    public enum BlockHighlightOption {
        NORMAL,
        RED,
        NONE;
    }
}
