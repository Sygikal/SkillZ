package net.skillz.init;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.skillz.access.LevelManagerAccess;
import net.skillz.access.ServerPlayerSyncAccess;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.util.LevelHelper;
import net.skillz.util.PacketHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.function.Supplier;

public class CommandInit {
    private static final SuggestionProvider<ServerCommandSource> SKILL_SUGGESTOR = (context, builder) -> {
        return CommandSource.suggestIdentifiers(LevelManager.SKILLS.values().stream().map((Skill::id)), builder);
    };

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            dispatcher.register((CommandManager.literal("skillz").requires((serverCommandSource) -> {
                return serverCommandSource.hasPermissionLevel(2);
            }))
            .then(CommandManager.literal("level").then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("operation", OperationArgument.operation()).then(CommandManager.argument("skill_id", IdentifierArgumentType.identifier()).suggests(SKILL_SUGGESTOR).then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> {
                return executeLevel(OperationArgument.getOperation(context, "operation"), (lm, s, l) -> {return lm.getSkillLevel(s.id());}, context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "skill_id"), IntegerArgumentType.getInteger(context, "amount"));
            }))))))
            .then(CommandManager.literal("overall_level").then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("operation", OperationArgument.operation()).then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> {
                return executeOverall(OperationArgument.getOperation(context, "operation"), (lm, s, l) -> {return lm.getOverallLevel();}, context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"));
            })))))
            .then(CommandManager.literal("points").then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("operation", OperationArgument.operation()).then(CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> {
                return executePoints(OperationArgument.getOperation(context, "operation"), (lm, s, l) -> {return lm.getSkillPoints();}, context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IntegerArgumentType.getInteger(context, "amount"));
            })))))
            );
        });
    }

    public interface OperationRunner {
        int run(int inject, int value);
    }

    public enum Operation implements StringIdentifiable {
        ADD(Integer::sum),
        REMOVE((inject, value) -> {return Math.max(inject - value, 0);}),
        SET((inject, value) -> {return Math.max(value, 0);});

        public final OperationRunner runner;

        Operation(OperationRunner runner) {
            this.runner = runner;
        }

        public int run(int num, int num2) {
            return runner.run(num, num2);
        }

        @Override
        public String asString() {
            return toString().toLowerCase();
        }
    }

    public static class OperationArgument extends EnumArgumentType<Operation> {

        public static final StringIdentifiable.Codec<Operation> CODEC = StringIdentifiable
                .createCodec(Operation::values);

        protected OperationArgument() {
            super(CODEC, Operation::values);
        }

        public static OperationArgument operation() {
            return new OperationArgument();
        }

        public static Operation getOperation(CommandContext<ServerCommandSource> context,
                                                String id) {
            return context.getArgument(id, Operation.class);
        }
    }

    public interface LevelRunner {
        int run(LevelManager levelManager, Skill skill, int level);
    }

    private static int executeOverall(Operation operation, LevelRunner runner, ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            LevelManager levelManager = ((LevelManagerAccess) serverPlayerEntity).getLevelManager();

            levelManager.setOverallLevel(operation.run(runner.run(levelManager, null, amount), amount));

            final int set = levelManager.getOverallLevel();
            serverPlayerEntity.getScoreboard().forEachScore(CriteriaInit.SKILLZ, serverPlayerEntity.getEntityName(), score -> score.setScore(set));
            serverPlayerEntity.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, serverPlayerEntity));

            PacketHelper.updateLevels(serverPlayerEntity);
            PacketHelper.updatePlayerSkills(serverPlayerEntity, null);

            source.sendFeedback(() -> Text.translatable("commands.level.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executePoints(Operation operation, LevelRunner runner, ServerCommandSource source, Collection<ServerPlayerEntity> targets, int amount) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            LevelManager levelManager = ((LevelManagerAccess) serverPlayerEntity).getLevelManager();

            levelManager.setSkillPoints(operation.run(runner.run(levelManager, null, amount), amount));

            PacketHelper.updateLevels(serverPlayerEntity);
            PacketHelper.updatePlayerSkills(serverPlayerEntity, null);

            source.sendFeedback(() -> Text.translatable("commands.level.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    private static int executeLevel(Operation operation, LevelRunner runner, ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier skillId, int amount) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            LevelManager levelManager = ((LevelManagerAccess) serverPlayerEntity).getLevelManager();

            if (!LevelManager.SKILLS.containsKey(skillId)) {
                source.sendFeedback(() -> Text.translatable("commands.level.failed"), false);
                return 0;
            }

            Skill skill = LevelManager.SKILLS.get(skillId);

            levelManager.setSkillLevel(skill.id(), operation.run(runner.run(levelManager, skill, amount), amount));

            if (!skill.attributes().isEmpty()) {
                LevelHelper.updateSkill(serverPlayerEntity, skill);
            }

            PacketHelper.updateLevels(serverPlayerEntity);
            PacketHelper.updatePlayerSkills(serverPlayerEntity, null);

            source.sendFeedback(() -> Text.translatable("commands.level.changed", serverPlayerEntity.getDisplayName()), true);
        }
        return targets.size();
    }

    // Reference 0:Add, 1:Remove, 2:Set, 3:Print
    private static int executeSkillCommand(ServerCommandSource source, Collection<ServerPlayerEntity> targets, String skillKey, int i, int reference) {

        // loop over players
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            LevelManager levelManager = ((LevelManagerAccess) serverPlayerEntity).getLevelManager();
            if (skillKey.equals("experience")) {
                if (reference == 0) {
                    ((ServerPlayerSyncAccess) serverPlayerEntity).addLevelExperience(i);
                } else if (reference == 1) {
                    int currentXP = (int) (levelManager.getLevelProgress() * levelManager.getNextLevelExperience());
                    float oldProgress = levelManager.getLevelProgress();
                    levelManager.setLevelProgress(currentXP - i > 0 ? (float) (currentXP - 1) / (float) levelManager.getNextLevelExperience() : 0.0F);
                    levelManager.setTotalLevelExperience(currentXP - i > 0 ? levelManager.getTotalLevelExperience() - i
                            : levelManager.getTotalLevelExperience() - (int) (oldProgress * levelManager.getNextLevelExperience()));
                } else if (reference == 2) {
                    float oldProgress = levelManager.getLevelProgress();
                    levelManager.setLevelProgress(i >= levelManager.getNextLevelExperience() ? 1.0F : (float) i / levelManager.getNextLevelExperience());
                    levelManager.setTotalLevelExperience((int) (levelManager.getTotalLevelExperience() - oldProgress * levelManager.getNextLevelExperience()
                            + levelManager.getLevelProgress() * levelManager.getNextLevelExperience()));
                } else if (reference == 3) {
                    source.sendFeedback(() -> Text.translatable("commands.level.printProgress", serverPlayerEntity.getDisplayName(),
                            (int) (levelManager.getLevelProgress() * levelManager.getNextLevelExperience()), levelManager.getNextLevelExperience()), true);
                }
            } else {
                Skill skill = null;
                int playerSkillLevel = 0;
                if (skillKey.equals("points")) {
                    playerSkillLevel = levelManager.getSkillPoints();
                } else if (skillKey.equals("level")) {
                    playerSkillLevel = levelManager.getOverallLevel();
                } else {
                    for (Skill overallSkill : LevelManager.SKILLS.values()) {
                        if (overallSkill.id().equals(skillKey)) {
                            playerSkillLevel = levelManager.getSkillLevel(overallSkill.id());
                            skill = overallSkill;
                            break;
                        }
                    }
                    if (skill == null) {
                        source.sendFeedback(() -> Text.translatable("commands.level.failed"), false);
                        return 0;
                    }
                }
                if (reference == 0) {
                    playerSkillLevel += i;
                } else if (reference == 1) {
                    playerSkillLevel = Math.max(playerSkillLevel - i, 0);
                } else if (reference == 2) {
                    playerSkillLevel = i;
                } else if (reference == 3) {
                    if (skillKey.equals("all")) {
                        for (Skill overallSkill : LevelManager.SKILLS.values()) {
                            final String finalSkill = overallSkill.id().toString();
                            source.sendFeedback(() -> Text.translatable("commands.level.printLevel", serverPlayerEntity.getDisplayName(),
                                            StringUtils.capitalize(finalSkill) + (finalSkill.equals("level") || finalSkill.equals("points") ? ":" : " Level:"),
                                            finalSkill.equals("level") ? levelManager.getOverallLevel()
                                                    : finalSkill.equals("points") ? levelManager.getSkillPoints() : levelManager.getSkillLevel(overallSkill.id())),
                                    true);
                        }
                    } else {
                        final String finalSkill = skillKey;
                        final int finalPlayerSkillLevel = playerSkillLevel;
                        source.sendFeedback(() -> Text.translatable("commands.level.printLevel", serverPlayerEntity.getDisplayName(),
                                StringUtils.capitalize(finalSkill) + (finalSkill.equals("level") || finalSkill.equals("points") ? ":" : " Level:"), finalPlayerSkillLevel), true);
                    }
                    continue;
                }
                if (skillKey.equals("points")) {
                    levelManager.setSkillPoints(playerSkillLevel);
                } else if (skillKey.equals("level")) {
                    levelManager.setOverallLevel(playerSkillLevel);
                    final int level = playerSkillLevel;
                    serverPlayerEntity.getScoreboard().forEachScore(CriteriaInit.SKILLZ, serverPlayerEntity.getEntityName(), score -> score.setScore(level));
                    serverPlayerEntity.server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_GAME_MODE, serverPlayerEntity));
                } else {
                    levelManager.setSkillLevel(skill.id(), playerSkillLevel);
                    if (!skill.attributes().isEmpty()) {
                        LevelHelper.updateSkill(serverPlayerEntity, skill);
                    }
                }
            }
            PacketHelper.updateLevels(serverPlayerEntity);
            PacketHelper.updatePlayerSkills(serverPlayerEntity, null);

            if (reference != 3) {
                source.sendFeedback(() -> Text.translatable("commands.level.changed", serverPlayerEntity.getDisplayName()), true);
            }
        }

        return targets.size();
    }

}