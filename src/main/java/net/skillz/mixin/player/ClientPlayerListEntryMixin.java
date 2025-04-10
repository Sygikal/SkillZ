package net.skillz.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.access.ClientPlayerListAccess;
import net.minecraft.client.network.PlayerListEntry;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListEntry.class)
public abstract class ClientPlayerListEntryMixin implements ClientPlayerListAccess {

    @Unique
    private int level;

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }
}
