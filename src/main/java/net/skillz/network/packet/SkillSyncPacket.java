package net.skillz.network.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.skillz.SkillZMain;
import net.skillz.level.SkillAttribute;
import net.skillz.level.SkillBonus;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class SkillSyncPacket implements FabricPacket {
    public static final Identifier PACKET_ID = SkillZMain.identifierOf("skill_sync_packet");
    protected final List<String> skillIds;
    protected final List<Integer> skillIndexes;
    protected final List<Integer> skillMaxLevels;

    protected final List<SkillAttributesRecord> skillAttributes;
    protected final SkillBonusesRecord skillBonuses;

    public static final PacketType<SkillSyncPacket> TYPE = PacketType.create(
            PACKET_ID, SkillSyncPacket::new
    );

    public SkillSyncPacket(PacketByteBuf buf) {
        this(buf.readList(PacketByteBuf::readString), buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readInt), buf.readList(SkillAttributesRecord::read), SkillBonusesRecord.read(buf));
    }

    public SkillSyncPacket(List<String> skillIds, List<Integer> skillIndexes, List<Integer> skillMaxLevels, List<SkillAttributesRecord> skillAttributes, SkillBonusesRecord skillBonuses) {
        this.skillIds = skillIds;
        this.skillIndexes = skillIndexes;
        this.skillMaxLevels = skillMaxLevels;
        this.skillAttributes = skillAttributes;
        this.skillBonuses = skillBonuses;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeCollection(this.skillIds, PacketByteBuf::writeString);
        buf.writeCollection(this.skillIndexes, PacketByteBuf::writeInt);
        buf.writeCollection(this.skillMaxLevels, PacketByteBuf::writeInt);
        buf.writeCollection(this.skillAttributes, (bufx, list) -> new SkillAttributesRecord(list.skillAttributes()).write(bufx));
        this.skillBonuses.write(buf);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public List<String> skillIds() {
        return skillIds;
    }

    public List<Integer> skillIndexes() {
        return skillIndexes;
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
                buf.writeString(SkillZMain.getEntityAttributeIdAsString(skillAttribute.getAttribute()));
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
                buf.writeString(skillBonus.getId());
                buf.writeInt(skillBonus.getLevel());
            }
        }

        public static SkillBonusesRecord read(PacketByteBuf buf) {
            List<SkillBonus> skillBonuses = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                String key = buf.readString();
                String id = buf.readString();
                int level = buf.readInt();
                skillBonuses.add(new SkillBonus(key, id, level));
            }
            return new SkillBonusesRecord(skillBonuses);
        }

    }

}