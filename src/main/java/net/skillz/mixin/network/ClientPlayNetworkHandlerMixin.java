package net.skillz.mixin.network;

import net.skillz.access.OrbAccess;
import net.skillz.content.entity.LevelExperienceOrbEntity;
import net.skillz.network.packet.OrbPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkThreadUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements OrbAccess {

    @Shadow
    @Mutable
    private ClientWorld world;

    @Final
    @Shadow
    @Mutable
    private MinecraftClient client;

    @Override
    public void onLevelExperienceOrbSpawn(OrbPacket packet) {
        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, this.client);
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        Entity entity = new LevelExperienceOrbEntity(this.world, d, e, f, packet.getExperience());
        entity.updateTrackedPosition(d, e, f);
        entity.setYaw(0.0F);
        entity.setPitch(0.0F);
        entity.setId(packet.getEntityId());
        this.world.addEntity(entity.getId(), entity);
    }
}
