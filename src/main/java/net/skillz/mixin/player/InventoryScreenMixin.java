package net.skillz.mixin.player;

import net.minecraft.text.Style;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.util.TextUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {

    public InventoryScreenMixin(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    protected void drawBackgroundMixin(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo info) {
        assert this.client != null;
        assert this.client.player != null;
        if (ConfigInit.CLIENT.showInventoryLevel) {
            LevelManager levelManager = (((LevelManagerAccess) this.client.player).getLevelManager());

            Text text = TextUtil.getGui("level_short", levelManager.getOverallLevel());
            float scale = ConfigInit.CLIENT.inventoryLevelScale;
            context.getMatrices().push();
            context.getMatrices().scale(scale, scale, 1F);
            context.getMatrices().translate((ConfigInit.CLIENT.inventoryLevelPosition.x.run(scale, this.textRenderer.fontHeight, this.textRenderer.getWidth(text)) + ConfigInit.CLIENT.inventoryLevelXOffset + this.x) / scale,
                    (ConfigInit.CLIENT.inventoryLevelPosition.y.run(scale, this.textRenderer.fontHeight, this.textRenderer.getWidth(text)) + ConfigInit.CLIENT.inventoryLevelYOffset + this.y) / scale, 70.0D);
            context.drawText(this.textRenderer, text, 0, 0, levelManager.getSkillPointColor(), false);
            context.getMatrices().pop();
        }
    }
}
