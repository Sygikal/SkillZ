package net.skillz.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.skillz.SkillZMain;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Increase skill packet
 * Used in the skill screen by using a button
 *
 * @param id    skill id
 * @param level amount
 */
public class StatPacket implements FabricPacket {
    public static final Identifier PACKET_ID = SkillZMain.identifierOf("stat_packet");

    protected final Identifier id;
    protected final int level;

    public static final PacketType<StatPacket> TYPE = PacketType.create(
            PACKET_ID, StatPacket::new
    );

    public StatPacket(PacketByteBuf buf) {
        this(buf.readIdentifier(), buf.readInt());
    }

    public StatPacket(Identifier id, int level) {
        this.id = id;
        this.level = level;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.id);
        buf.writeInt(this.level);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public int level() {
        return level;
    }

    public Identifier id() {
        return id;
    }

}

