package net.skillz.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.skillz.SkillZMain;
import net.skillz.level.restriction.PlayerRestriction;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
/*import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestrictionPacket implements FabricPacket {
    public static final Identifier PACKET_ID = SkillZMain.identifierOf("restriction_packet");

    public RestrictionRecord blockRestrictions() {
        return blockRestrictions;
    }

    public RestrictionRecord craftingRestrictions() {
        return craftingRestrictions;
    }

    public RestrictionRecord entityRestrictions() {
        return entityRestrictions;
    }

    public RestrictionRecord itemRestrictions() {
        return itemRestrictions;
    }

    public RestrictionRecord miningRestrictions() {
        return miningRestrictions;
    }

    public RestrictionRecord enchantmentRestrictions() {
        return enchantmentRestrictions;
    }

    protected final RestrictionRecord blockRestrictions;
    protected final RestrictionRecord craftingRestrictions;
    protected final RestrictionRecord entityRestrictions;
    protected final RestrictionRecord itemRestrictions;
    protected final RestrictionRecord miningRestrictions;
    protected final RestrictionRecord enchantmentRestrictions;

    public static final PacketType<RestrictionPacket> TYPE = PacketType.create(
            PACKET_ID, RestrictionPacket::new
    );

    public RestrictionPacket(PacketByteBuf buf) {
        this(RestrictionRecord.read(buf), RestrictionRecord.read(buf), RestrictionRecord.read(buf), RestrictionRecord.read(buf), RestrictionRecord.read(buf), RestrictionRecord.read(buf));
    }

    public RestrictionPacket(RestrictionRecord blockRestrictions, RestrictionRecord craftingRestrictions, RestrictionRecord entityRestrictions,
                             RestrictionRecord itemRestrictions, RestrictionRecord miningRestrictions, RestrictionRecord enchantmentRestrictions) {
        this.blockRestrictions = blockRestrictions;
        this.craftingRestrictions = craftingRestrictions;
        this.entityRestrictions = entityRestrictions;
        this.itemRestrictions = itemRestrictions;
        this.miningRestrictions = miningRestrictions;
        this.enchantmentRestrictions = enchantmentRestrictions;
    }

    @Override
    public void write(PacketByteBuf buf) {
        this.blockRestrictions.write(buf);
        this.craftingRestrictions.write(buf);
        this.entityRestrictions.write(buf);
        this.itemRestrictions.write(buf);
        this.miningRestrictions.write(buf);
        this.enchantmentRestrictions.write(buf);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public record RestrictionRecord(List<Integer> ids, List<PlayerRestriction> restrictions) {

        public void write(PacketByteBuf buf) {
            buf.writeInt(ids().size());
            for (Integer id : ids) {
                buf.writeInt(id);
            }
            buf.writeInt(restrictions().size());
            for (int i = 0; i < restrictions().size(); i++) {
                PlayerRestriction playerRestriction = restrictions().get(i);
                buf.writeInt(playerRestriction.getId());
                buf.writeBoolean(playerRestriction.isHidden());
                buf.writeInt(playerRestriction.getSkillLevelRestrictions().size());
                for (Map.Entry<String, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                    buf.writeString(entry.getKey());
                    buf.writeInt(entry.getValue());
                }
            }
        }

        public static RestrictionRecord read(PacketByteBuf buf) {
            List<Integer> ids = new ArrayList<>();
            int idSize = buf.readInt();
            for (int i = 0; i < idSize; i++) {
                ids.add(buf.readInt());
            }
            List<PlayerRestriction> playerRestrictions = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                int id = buf.readInt();
                boolean hidden = buf.readBoolean();
                int skillLevelSize = buf.readInt();
                Map<String, Integer> skillLevelRestrictions = new HashMap<>();
                for (int u = 0; u < skillLevelSize; u++) {
                    String skillId = buf.readString();
                    int skillLevel = buf.readInt();
                    skillLevelRestrictions.put(skillId, skillLevel);
                }
                PlayerRestriction res = new PlayerRestriction(id, skillLevelRestrictions);
                res.setHidden(hidden);
                playerRestrictions.add(res);
            }
            return new RestrictionRecord(ids, playerRestrictions);
        }

    }

}

