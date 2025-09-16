package net.skillz.content.item;

import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.skillz.util.LevelHelper;
import net.skillz.util.PacketHelper;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StrangePotionItem extends Item {

    public StrangePotionItem(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof ServerPlayerEntity playerEntity) {
            Criteria.CONSUME_ITEM.trigger(playerEntity, stack);

            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            List<String> list = new ArrayList<>(levelManager.getPlayerSkills().keySet());
            Collections.shuffle(list);

            for (String skillId : list) {
                if (levelManager.resetSkill(skillId) && !ConfigInit.MAIN.LEVEL.opStrangePotion) {
                    LevelHelper.updateSkill(playerEntity, LevelManager.SKILLS.get(skillId));
                    break;
                }
            }
            PacketHelper.updatePlayerSkills(playerEntity, null);

            if (!playerEntity.isCreative()) {
                stack.decrement(1);
                if (stack.isEmpty()) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                }
                playerEntity.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
            }

            user.emitGameEvent(GameEvent.DRINK);
        }
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }

}
