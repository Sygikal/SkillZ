package net.skillz.content.item;

import com.google.common.collect.Lists;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.skillz.access.LevelManagerAccess;
import net.skillz.access.ServerPlayerSyncAccess;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.util.LevelHelper;
import net.skillz.util.PacketHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class BookOfKnowledgeItem extends Item {

    public BookOfKnowledgeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!user.getAbilities().creativeMode) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }

        LevelManager levelManager = ((LevelManagerAccess) user).getLevelManager();
        NbtCompound nbt = itemStack.getOrCreateNbt();
        if (!nbt.contains("Points")) {
            nbt.put("Points", NbtInt.of(ConfigInit.MAIN.LEVEL.bookOfKnowledgeStartingPoints));
        }

        int points = nbt.getInt("Points");

        if (!world.isClient) {
            levelManager.setSkillPoints(levelManager.getSkillPoints() + points);
            PacketHelper.updateLevels((ServerPlayerEntity) user);
        }

        world.playSound(user, user.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0f, 1.6f);

        return TypedActionResult.success(itemStack, world.isClient());
    }

}
