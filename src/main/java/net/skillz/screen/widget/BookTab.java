package net.skillz.screen.widget;

import dev.sygii.tabapi.api.InventoryTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.screen.SkillRestrictionScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class BookTab extends InventoryTab {
    public static final Identifier BOOK_TEXTURE = SkillZMain.identifierOf("textures/gui/book_template.png");
    private final Color color;
    private final RestrictionBook book;

    public BookTab(Text title, RestrictionBook book, Color color, int preferedPos, Class<?>... screenClasses) {
        super(null, title, null, preferedPos, true, screenClasses);
        this.color = color;
        this.book = book;
    }

    /*@Override
    public boolean canClick(Class<?> screenClass, MinecraftClient client) {
        if (screenClass.equals(SkillInfoScreen.class) || screenClass.equals(SkillRestrictionScreen.class)) {
            return true;
        }
        return super.canClick(screenClass, client);
    }*/

    @Override
    public void customRender(DrawContext context, int x, int y, int mouseX, int mouseY) {

        setColor(context, color.darker().darker().darker().getRGB());
        context.drawTexture(BOOK_TEXTURE, x, y, 15, 0, 15, 13, 45, 26);
        setColor(context, -1);

        setColor(context, color.getRGB());
        context.drawTexture(BOOK_TEXTURE, x, y, 0, 0, 15, 13, 45, 26);
        setColor(context, -1);

        context.drawTexture(BOOK_TEXTURE, x, y, 30, 13, 15, 13, 45, 26);
        setColor(context, -1);
    }

    @Override
    public boolean isSelected(Screen screen) {
        if (screen instanceof SkillRestrictionScreen screen1) {
            return screen1.getRestriction().equals(book.restriction);
        }
        return false;
    }

    @Override
    public boolean shouldShow(MinecraftClient client) {
        return true;
    }

    public void setColor(DrawContext context, int hex) {
        context.setShaderColor(ColorHelper.Argb.getRed(hex) / 255.0F, ColorHelper.Argb.getGreen(hex) / 255.0F, ColorHelper.Argb.getBlue(hex) / 255.0F, ColorHelper.Argb.getAlpha(hex) / 255.0F);
    }

    @Override
    public void onClick(MinecraftClient client) {
        client.setScreen(new SkillRestrictionScreen(((LevelManagerAccess) client.player).getLevelManager(), book.restriction, book.title, book.code));
    }

    @Override
    public boolean canClick(Screen screen, MinecraftClient client) {
        return !this.isSelected(screen);
    }

}
