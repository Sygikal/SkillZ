package net.skillz.init;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.mixin.entity.EntityAccessor;
import net.skillz.util.LevelHelper;
import net.skillz.util.PacketHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.skillz.util.TooltipUtil;

import java.util.Map;

public class EventInit {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (Skill skill : LevelManager.SKILLS.values()) {
                LevelHelper.updateSkill(handler.getPlayer(), skill);
            }
            PacketHelper.syncEnchantments(handler.getPlayer());
            PacketHelper.updateSkills(handler.getPlayer());
            PacketHelper.updatePlayerSkills(handler.getPlayer(), null);
            PacketHelper.updateRestrictions(handler.getPlayer());
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            PacketHelper.updatePlayerSkills(player, null);
            PacketHelper.updateLevels(player);
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (alive) {
                PacketHelper.updatePlayerSkills(newPlayer, oldPlayer);
                PacketHelper.updateLevels(newPlayer);
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (ConfigInit.MAIN.LEVEL.hardMode) {
                newPlayer.getServer().getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, newPlayer));
                newPlayer.getScoreboard().forEachScore(CriteriaInit.SKILLZ, newPlayer.getEntityName(), ScoreboardPlayerScore::clearScore);
            } else {
                PacketHelper.updatePlayerSkills(newPlayer, oldPlayer);

                if (ConfigInit.MAIN.EXPERIENCE.resetCurrentXp) {
                    LevelManager levelManager = ((LevelManagerAccess) newPlayer).getLevelManager();
                    levelManager.setLevelProgress(0);
                    levelManager.setTotalLevelExperience(0);
                }

                PacketHelper.updateLevels(newPlayer);
                for (Skill skill : LevelManager.SKILLS.values()) {
                    LevelHelper.updateSkill(newPlayer, skill);
                }
            }
        });

        //Called when item right-clicked

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!player.isCreative() && !player.isSpectator()) {
                LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                if (!levelManager.hasRequiredItemLevel(player.getStackInHand(hand).getItem())) {
                    //System.out.println(levelManager.getRequiredItemLevel(player.getStackInHand(hand).getItem()));
                    player.sendMessage(sendRestriction(levelManager.getRequiredItemLevel(player.getStackInHand(hand).getItem()), levelManager), true);
                    return TypedActionResult.fail(player.getStackInHand(hand));
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });

        //Called when block right-clicked
        UseBlockCallback.EVENT.register((player, world, hand, result) -> {
            if (!player.isCreative() && !player.isSpectator()) {
                BlockPos blockPos = result.getBlockPos();
                if (world.canPlayerModifyAt(player, blockPos)) {
                    LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                    if (!levelManager.hasRequiredBlockLevel(world.getBlockState(blockPos).getBlock())) {
                        player.sendMessage(sendRestriction(levelManager.getRequiredBlockLevel(world.getBlockState(blockPos).getBlock()), levelManager), true);
                        return ActionResult.success(false);
                    }
                }
            }
            return ActionResult.PASS;
        });

        //Called when entity right-clicked
        UseEntityCallback.EVENT.register((player, world, hand, entity, entityHitResult) -> {
            if (SkillZMain.shouldRestrictEntity(player, entity)) {
                return ActionResult.success(false);
            }
            return ActionResult.PASS;
        });
    }

    public static MutableText sendRestriction(Map<Identifier, Integer> map, LevelManager levelManager, boolean showLines) {
        MutableText asd = Text.literal("");
        int count = 0;
        for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
            boolean noHasLevel = levelManager.getSkillLevel(entry.getKey()) < entry.getValue();
            if (showLines || noHasLevel) {
                asd.append(TooltipUtil.getRestrictionKey(entry.getKey(), entry.getValue()).formatted(noHasLevel ? Formatting.RED : Formatting.GRAY));
                if ((map.size() - count) > 1) {
                    asd.append(Text.literal(",").formatted(noHasLevel ? Formatting.RED : Formatting.GRAY)).append(ScreenTexts.SPACE);
                }
            }
            count++;
        }
        return asd;
    }

    public static MutableText sendRestriction(Map<Identifier, Integer> map, LevelManager levelManager) {
        return sendRestriction(map, levelManager, !ConfigInit.CLIENT.hideReachedLevels);
    }

}
