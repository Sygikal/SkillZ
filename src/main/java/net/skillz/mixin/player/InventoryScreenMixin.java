package net.skillz.mixin.player;

import net.minecraft.text.Style;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
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
        if (ConfigInit.CLIENT.inventorySkillLevel) {
            LevelManager levelManager = (((LevelManagerAccess) this.client.player).getLevelManager());
            // 0xAARRGGBB Format
            int color = 0xFFFFFF;
            if (levelManager.getSkillPoints() > 0) {
                color = 1507303;
            }

            context.getMatrices().push();
            context.getMatrices().scale(0.5F, 0.5F, 1F);
            context.getMatrices().translate((28 + ConfigInit.CLIENT.inventorySkillLevelPosX + this.x) / 0.5F,
                    (8 + ConfigInit.CLIENT.inventorySkillLevelPosY + this.y + textRenderer.fontHeight / 2F) / 0.5F, 70.0D);
            context.drawText(this.textRenderer, Text.translatable("text.skillz.gui.short_level", levelManager.getOverallLevel()), 0, -textRenderer.fontHeight / 2, color, false);
            context.getMatrices().pop();
        }
    }
}
