package net.skillz.mixin.player;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.init.ItemInit;
import net.skillz.level.LevelManager;
import net.skillz.util.PacketHelper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.authlib.GameProfile;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    /*@Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerConnected(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info, GameProfile gameProfile, UserCache userCache, String string,
            NbtCompound nbtCompound) {
        PlayerStatsManager playerStatsManager = ((PlayerStatsManagerAccess) player).getPlayerStatsManager();
        boolean isFirstTimeJoin = nbtCompound == null;
        if (isFirstTimeJoin && server != null && (server.getSaveProperties().getGeneratorOptions().hasBonusChest() || ConfigInit.CONFIG.enableStartPoints)) {
            playerStatsManager.setSkillPoints(ConfigInit.CONFIG.startPoints);
        }
        PlayerStatsServerPacket.writeS2CListPacket(player);
        if (isFirstTimeJoin) {
            player.setHealth(player.getMaxHealth());
        }
        // Sync strength on client cause out of any reason it doesn't work naturally as with respawnPlayer
        PlayerStatsServerPacket.writeS2CStrengthPacket(player);
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerRespawned(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void respawnPlayerMixin(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> info, BlockPos blockPos, float f, boolean bl, ServerWorld serverWorld,
            Optional<Object> optional, ServerWorld serverWorld2, ServerPlayerEntity serverPlayerEntity) {
        if (alive || !ConfigInit.CONFIG.hardMode) {
            PlayerStatsManager playerStatsManager = ((PlayerStatsManagerAccess) player).getPlayerStatsManager();
            PlayerStatsManager serverPlayerStatsManager = ((PlayerStatsManagerAccess) serverPlayerEntity).getPlayerStatsManager();
            // Set on client
            PlayerStatsServerPacket.writeS2CSkillPacket(playerStatsManager, serverPlayerEntity);
            // Set on server
            serverPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH));
            serverPlayerEntity.setHealth(serverPlayerEntity.getMaxHealth());
            serverPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
            serverPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED));
            serverPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_ARMOR));
            serverPlayerEntity.getAttributeInstance(EntityAttributes.GENERIC_LUCK).setBaseValue(player.getAttributeBaseValue(EntityAttributes.GENERIC_LUCK));
            // Sync strength on client cause out of any reason it doesn't work naturally
            PlayerStatsServerPacket.writeS2CStrengthPacket(serverPlayerEntity);
            // Check if Client will set to 0 after death
            boolean keepInventory = serverWorld.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);
            serverPlayerStatsManager.setLevelProgress(keepInventory ? playerStatsManager.getLevelProgress() : ConfigInit.CONFIG.resetCurrentXP ? 0 : playerStatsManager.getLevelProgress());
            serverPlayerStatsManager
                    .setTotalLevelExperience(keepInventory ? playerStatsManager.getTotalLevelExperience() : ConfigInit.CONFIG.resetCurrentXP ? 0 : playerStatsManager.getTotalLevelExperience());
            // Level
            serverPlayerStatsManager.setOverallLevel(playerStatsManager.getOverallLevel());
            serverPlayerStatsManager.setSkillPoints(playerStatsManager.getSkillPoints());
            // Skill
            for (Skill skill : Skill.values()) {
                serverPlayerStatsManager.setSkillLevel(skill, playerStatsManager.getSkillLevel(skill));
            }
        }
        if (ConfigInit.CONFIG.hardMode) {
            serverPlayerEntity.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, serverPlayerEntity));
            serverPlayerEntity.getScoreboard().forEachScore(CriteriaInit.LEVELZ, serverPlayerEntity.getEntityName(), ScoreboardPlayerScore::clearScore);
        }
    }*/

    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getBossBarManager()Lnet/minecraft/entity/boss/BossBarManager;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerConnectMixin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info, GameProfile gameProfile, UserCache userCache, String string, NbtCompound optional, RegistryKey<World> registryKey, ServerWorld serverWorld, ServerWorld serverWorld2) {
       if (optional == null || optional.isEmpty()) {
           if (ConfigInit.MAIN.LEVEL.bookOfKnowledgeStartingPoints > 0) {
               /*LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
               levelManager.setSkillPoints(ConfigInit.MAIN.LEVEL.startPoints);*/
               player.getInventory().insertStack(ItemInit.BOOK_OF_KNOWLEDGE.getDefaultStack());
               PacketHelper.updateLevels(player);
           }
       }
    }

}