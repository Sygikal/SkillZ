package net.skillz.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.screen.LevelScreen;
import net.skillz.screen.SkillInfoScreen;
import net.skillz.screen.SkillRestrictionScreen;
import dev.sygii.tabapi.api.InventoryTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class SkillTab extends InventoryTab {

    public SkillTab(Identifier id, Text title, Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(id, title, texture, preferedPos, screenClasses);
    }

    @Override
    public boolean canClick(Screen screen, MinecraftClient client) {
        if (screen.getClass().equals(SkillInfoScreen.class) || screen.getClass().equals(SkillRestrictionScreen.class)) {
            return true;
        }
        return super.canClick(screen, client);
    }

    @Override
    public void onClick(MinecraftClient client) {
        client.setScreen(new LevelScreen());
    }

}
