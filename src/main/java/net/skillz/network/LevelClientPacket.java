package net.skillz.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import net.skillz.level.PlayerSkill;
import net.skillz.level.Skill;
import net.skillz.content.registry.EnchantmentRegistry;
import net.skillz.content.registry.EnchantmentZ;
import net.skillz.screen.LevelScreen;
import net.skillz.network.packet.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class LevelClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SkillSyncPacket.PACKET_ID, (client, handler, buf, sender) -> {
            SkillSyncPacket payload = new SkillSyncPacket(buf);
            List<String> skillIds = payload.skillIds();
            List<Integer> skillIndexes = payload.skillIndexes();
            List<Integer> skillMaxLevels = payload.skillMaxLevels();
            List<SkillSyncPacket.SkillAttributesRecord> skillAttributes = payload.skillAttributes();
            SkillSyncPacket.SkillBonusesRecord skillBonuses = payload.skillBonuses();

            client.execute(() -> {
                LevelManager levelManager = ((LevelManagerAccess) client.player).getLevelManager();

                LevelManager.SKILLS.clear();
                for (int i = 0; i < skillIds.size(); i++) {
                    Skill skill = new Skill(skillIds.get(i), skillIndexes.get(i), skillMaxLevels.get(i), skillAttributes.get(i).skillAttributes());
                    LevelManager.SKILLS.put(skillIds.get(i), skill);

                    if (!levelManager.getPlayerSkills().containsKey(skillIds.get(i))) {
                        PlayerSkill playerSkill = new PlayerSkill(skillIds.get(i), 0);
                        levelManager.getPlayerSkills().put(skillIds.get(i), playerSkill);
                    }
                }
                LevelManager.BONUSES.clear();
                for (int i = 0; i < skillBonuses.skillBonuses().size(); i++) {
                    String bonusKey = skillBonuses.skillBonuses().get(i).getKey();
                    LevelManager.BONUSES.put(bonusKey, skillBonuses.skillBonuses().get(i));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(PlayerSkillSyncPacket.PACKET_ID, (client, handler, buf, sender) -> {
            List<String> playerSkillIds = buf.readList(PacketByteBuf::readString);
            List<Integer> playerSkillLevels = buf.readList(PacketByteBuf::readInt);

            client.execute(() -> {
                System.out.printf("%s %s%n", playerSkillIds, playerSkillLevels);
                LevelManager levelManager = ((LevelManagerAccess) client.player).getLevelManager();
                for (int i = 0; i < playerSkillIds.size(); i++) {
                    levelManager.setSkillLevel(playerSkillIds.get(i), playerSkillLevels.get(i));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(LevelPacket.PACKET_ID, (client, handler, buf, sender) -> {
            //LevelPacket packet = new LevelPacket(buf);
            LevelPacket payload = new LevelPacket(buf);
            int overallLevel = payload.overallLevel();
            int skillPoints = payload.skillPoints();
            int totalLevelExperience = payload.totalLevelExperience();
            float levelProgress = payload.levelProgress();
            client.execute(() -> {
                LevelManager levelManager = ((LevelManagerAccess) client.player).getLevelManager();
                levelManager.setOverallLevel(overallLevel);
                levelManager.setSkillPoints(skillPoints);
                levelManager.setTotalLevelExperience(totalLevelExperience);
                levelManager.setLevelProgress(levelProgress);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(RestrictionPacket.PACKET_ID,  (client, handler, buf, sender) -> {
            RestrictionPacket payload = new RestrictionPacket(buf);
            RestrictionPacket.RestrictionRecord blockRestrictions = payload.blockRestrictions();
            RestrictionPacket.RestrictionRecord craftingRestrictions = payload.craftingRestrictions();
            RestrictionPacket.RestrictionRecord entityRestrictions = payload.entityRestrictions();
            RestrictionPacket.RestrictionRecord itemRestrictions = payload.itemRestrictions();
            RestrictionPacket.RestrictionRecord miningRestrictions = payload.miningRestrictions();
            RestrictionPacket.RestrictionRecord enchantmentRestrictions = payload.enchantmentRestrictions();

            client.execute(() -> {
                LevelManager.BLOCK_RESTRICTIONS.clear();
                LevelManager.CRAFTING_RESTRICTIONS.clear();
                LevelManager.ENTITY_RESTRICTIONS.clear();
                LevelManager.ITEM_RESTRICTIONS.clear();
                LevelManager.MINING_RESTRICTIONS.clear();
                LevelManager.ENCHANTMENT_RESTRICTIONS.clear();

                for (int i = 0; i < blockRestrictions.ids().size(); i++) {
                    LevelManager.BLOCK_RESTRICTIONS.put(blockRestrictions.ids().get(i), blockRestrictions.restrictions().get(i));
                }
                for (int i = 0; i < craftingRestrictions.ids().size(); i++) {
                    LevelManager.CRAFTING_RESTRICTIONS.put(craftingRestrictions.ids().get(i), craftingRestrictions.restrictions().get(i));
                }
                for (int i = 0; i < entityRestrictions.ids().size(); i++) {
                    LevelManager.ENTITY_RESTRICTIONS.put(entityRestrictions.ids().get(i), entityRestrictions.restrictions().get(i));
                }
                for (int i = 0; i < itemRestrictions.ids().size(); i++) {
                    LevelManager.ITEM_RESTRICTIONS.put(itemRestrictions.ids().get(i), itemRestrictions.restrictions().get(i));
                }
                for (int i = 0; i < miningRestrictions.ids().size(); i++) {
                    LevelManager.MINING_RESTRICTIONS.put(miningRestrictions.ids().get(i), miningRestrictions.restrictions().get(i));
                }
                for (int i = 0; i < enchantmentRestrictions.ids().size(); i++) {
                    LevelManager.ENCHANTMENT_RESTRICTIONS.put(enchantmentRestrictions.ids().get(i), enchantmentRestrictions.restrictions().get(i));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(StatPacket.PACKET_ID, (client, handler, buf, sender) -> {
            StatPacket payload = new StatPacket(buf);
            String id = payload.id();
            int level = payload.level();
            client.execute(() -> {
                LevelManager levelManager = ((LevelManagerAccess) client.player).getLevelManager();
                levelManager.setSkillLevel(id, level);
                if (client.currentScreen instanceof LevelScreen levelScreen) {
                    levelScreen.updateLevelButtons();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(EnchantmentZPacket.PACKET_ID, (client, handler, buf, sender) -> {
            EnchantmentZPacket payload = new EnchantmentZPacket(buf);
            Map<String, Integer> indexed = payload.indexed();
            List<Integer> keys = payload.keys();
            List<String> ids = payload.ids();
            List<Integer> levels = payload.levels();
            client.execute(() -> {
                EnchantmentRegistry.ENCHANTMENTS.clear();
                EnchantmentRegistry.INDEX_ENCHANTMENTS.clear();

                Registry<Enchantment> registry = client.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
                for (int i = 0; i < keys.size(); i++) {
                    int key = keys.get(i);

                    //Identifier id = new Identifier(ids.get(i));
                    Identifier id = Identifier.splitOn(ids.get(i), ':');
                    RegistryEntry<Enchantment> entry = registry.getEntry(registry.get(id));
                    //System.out.printf("%s %s %s%n%n", ids.get(i), id, entry);
                    int level = levels.get(i);
                    EnchantmentRegistry.ENCHANTMENTS.put(key, new EnchantmentZ(entry, level));
                }
                EnchantmentRegistry.INDEX_ENCHANTMENTS.putAll(indexed);
            });
        });
    }
}

