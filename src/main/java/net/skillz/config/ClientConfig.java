package net.skillz.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedColor;
import net.minecraft.client.font.TextRenderer;
import net.skillz.SkillZMain;

public class ClientConfig extends Config {

    public ClientConfig() {
        super(SkillZMain.identifierOf("client_config"));
    }

    @Comment("Hide reached levels in tooltips")
    public boolean hideReachedLevels = true;

    @Comment("How locked blocks should appear highlighted")
    public BlockHighlightOption highlightOption = BlockHighlightOption.NORMAL;

    @Comment("The color used for the level display when there are available skill points")
    public ValidatedColor skillPointColor = new ValidatedColor(22, 255, 231, 255);

    public boolean showInventoryLevel = true;

    @Comment("Position of the level display in the inventory")
    public LevelPositionOption inventoryLevelPosition = LevelPositionOption.TOP_LEFT;

    public float inventoryLevelScale = 0.5f;

    public int inventoryLevelXOffset = 0;

    public int inventoryLevelYOffset = 0;

    @RequiresAction(action = Action.RESTART)
    public boolean showLevelList = true;

    public boolean showLevel = true;

    @Comment("Inventory key goes back to main screen rather than closing the inventory")
    public boolean switchScreen = true;

    @Comment("Show tooltip on the hud for restrictions (should only use if you dont have jade)")
    public boolean showRestrictionOnHUD = false;

    public int hudInfoX = 0;

    public int hudInfoY = 0;

    public enum LevelPositionOption {
        TOP_LEFT((s, h, w)-> {return 28;}, (s, h, w)-> {return 10;}), // 28, 10
        BOTTOM_LEFT((s, g, w)-> {return 28;}, (s, h, w)-> {return 77 - h * s;}), // 28, 77 - (this.textRenderer.fontHeight * scale)
        TOP_RIGHT((s, h, w)-> {return 73 - (w);}, (s, h, w) -> {return 10;}), // 73 - (this.textRenderer.getWidth(text) * scale), 10
        BOTTOM_RIGHT((s, h, w) -> {return 73 - (w * s);}, (s, h, w) -> {return 77 - (h * s);}); // 73 - (this.textRenderer.getWidth(text) * scale), 77 - (this.textRenderer.fontHeight * scale)

        public final PositionRunner x;
        public final PositionRunner y;

        LevelPositionOption(PositionRunner x, PositionRunner y) {
            this.x = x;
            this.y = y;
        }
    }

    public interface PositionRunner {
        float run(float scale, float height, float width);
    }

    public enum BlockHighlightOption {
        NORMAL,
        RED,
        NONE;
    }
}
