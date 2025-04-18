package net.skillz.mixin.player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.access.ClientPlayerListAccess;
import net.skillz.init.ConfigInit;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(method = "getPlayerName", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void getPlayerNameMixin(PlayerListEntry entry, CallbackInfoReturnable<Text> info) {
        if (ConfigInit.CLIENT.showLevelList)
            info.setReturnValue(this.applyGameModeFormatting(entry,
                    Team.decorateName(entry.getScoreboardTeam(), Text.translatable("text.skillz.scoreboard", ((ClientPlayerListAccess) entry).getLevel(), entry.getProfile().getName()))));
    }

    @Shadow
    private Text applyGameModeFormatting(PlayerListEntry entry, MutableText name) {
        return null;
    }

}
