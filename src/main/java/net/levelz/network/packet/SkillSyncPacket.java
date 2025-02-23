package net.levelz.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.levelz.LevelzMain;
import net.levelz.level.SkillAttribute;
import net.levelz.level.SkillBonus;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SkillSyncPacket implements FabricPacket {
    public static final Identifier PACKET_ID = LevelzMain.identifierOf("skill_sync_packet");
    protected final List<Integer> skillIds;
    protected final List<String> skillKeys;
    protected final List<Integer> skillMaxLevels;

    protected final List<SkillAttributesRecord> skillAttributes;
    protected final SkillBonusesRecord skillBonuses;

    public static final PacketType<SkillSyncPacket> TYPE = PacketType.create(
            PACKET_ID, SkillSyncPacket::new
    );

    public SkillSyncPacket(PacketByteBuf buf) {
        this(buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readString), buf.readList(PacketByteBuf::readInt), buf.readList(SkillAttributesRecord::read), SkillBonusesRecord.read(buf));
    }

    public SkillSyncPacket(List<Integer> skillIds, List<String> skillKeys, List<Integer> skillMaxLevels, List<SkillAttributesRecord> skillAttributes, SkillBonusesRecord skillBonuses) {
        this.skillIds = skillIds;
        this.skillKeys = skillKeys;
        this.skillMaxLevels = skillMaxLevels;
        this.skillAttributes = skillAttributes;
        this.skillBonuses = skillBonuses;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.skillIds, PacketByteBuf::writeInt);
        buf.writeCollection(this.skillKeys, PacketByteBuf::writeString);
        buf.writeCollection(this.skillMaxLevels, PacketByteBuf::writeInt);
        buf.writeCollection(this.skillAttributes, (bufx, list) -> new SkillAttributesRecord(list.skillAttributes()).write(bufx));
        this.skillBonuses.write(buf);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public List<String> skillKeys() {
        return skillKeys;
    }

    public List<Integer> skillIds() {
        return skillIds;
    }

    public List<Integer> skillMaxLevels() {
        return skillMaxLevels;
    }

    public List<SkillAttributesRecord> skillAttributes() {
        return skillAttributes;
    }

    public SkillBonusesRecord skillBonuses() {
        return skillBonuses;
    }

    public record SkillAttributesRecord(List<SkillAttribute> skillAttributes) {

        public void write(PacketByteBuf buf) {
            buf.writeInt(skillAttributes().size());
            for (int i = 0; i < skillAttributes().size(); i++) {
                SkillAttribute skillAttribute = skillAttributes().get(i);
                buf.writeInt(skillAttribute.getId());
                buf.writeString(LevelzMain.getEntityAttributeIdAsString(skillAttribute.getAttribute()));
                buf.writeFloat(skillAttribute.getBaseValue());
                buf.writeFloat(skillAttribute.getLevelValue());
                //TODO watch for asString
                buf.writeString(skillAttribute.getOperation().toString());

            }
        }

        public static SkillAttributesRecord read(PacketByteBuf buf) {
            List<SkillAttribute> skillAttributes = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                int id = buf.readInt();

                RegistryEntry<EntityAttribute> attribute = Registries.ATTRIBUTE.getEntry(Registries.ATTRIBUTE.get(Identifier.splitOn(buf.readString(), ':')));
                float baseValue = buf.readFloat();
                float levelValue = buf.readFloat();
                EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.valueOf(buf.readString().toUpperCase());
                skillAttributes.add(new SkillAttribute(id, attribute, baseValue, levelValue, operation));
            }
            return new SkillAttributesRecord(skillAttributes);
        }

    }

    public record SkillBonusesRecord(List<SkillBonus> skillBonuses) {

        public void write(PacketByteBuf buf) {
            buf.writeInt(skillBonuses().size());
            for (int i = 0; i < skillBonuses().size(); i++) {
                SkillBonus skillBonus = skillBonuses().get(i);
                buf.writeString(skillBonus.getKey());
                buf.writeInt(skillBonus.getId());
                buf.writeInt(skillBonus.getLevel());
            }
        }

        public static SkillBonusesRecord read(PacketByteBuf buf) {
            List<SkillBonus> skillBonuses = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                String key = buf.readString();
                int id = buf.readInt();
                int level = buf.readInt();
                skillBonuses.add(new SkillBonus(key, id, level));
            }
            return new SkillBonusesRecord(skillBonuses);
        }

    }

}