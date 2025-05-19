package net.skillz.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import dev.sygii.tabapi.api.InventoryTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class VanillaInventoryTab extends InventoryTab {

    public VanillaInventoryTab(Identifier id, Text title, Identifier texture, int preferedPos, Class<?>... screenClasses) {
        super(id, title, texture, preferedPos, screenClasses);
    }

    @Override
    public void onClick(MinecraftClient client) {
        client.setScreen(new InventoryScreen(client.player));
    }

}
