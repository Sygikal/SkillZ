package net.skillz.network.packet;

import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.access.OrbAccess;
import net.skillz.content.entity.LevelExperienceOrbEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

public class OrbPacket implements Packet<ClientPlayPacketListener> {
    public static final Identifier PACKET_ID = SkillZMain.identifierOf("orb");
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final int experience;

    public OrbPacket(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.experience = buf.readShort();
    }

    public OrbPacket(LevelExperienceOrbEntity orb) {
        this.entityId = orb.getId();
        this.x = orb.getX();
        this.y = orb.getY();
        this.z = orb.getZ();
        this.experience = orb.getExperienceAmount();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeShort(this.experience);
    }

    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        ((OrbAccess) clientPlayPacketListener).onLevelExperienceOrbSpawn(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public int getExperience() {
        return this.experience;
    }
}