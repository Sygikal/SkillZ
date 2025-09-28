package net.skillz.mixin.block;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.DoubleCropDropBonus;
import net.skillz.init.ConfigInit;
import net.skillz.init.TagInit;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

@Mixin(PlantBlock.class)
public abstract class PlantBlockMixin extends Block {

    public PlantBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && player != null && !player.isCreative()) {
            if (EnchantmentHelper.getEquipmentLevel(Enchantments.SILK_TOUCH, player) <= 0) {
                if (BonusManager.doBooleanBonus(DoubleCropDropBonus.ID, player, ConfigInit.MAIN.BONUSES.doubleCropDropChance)) {
                    List<ItemStack> list = Block.getDroppedStacks(state, (ServerWorld) player.getWorld(), pos, null);
                    for (ItemStack itemStack : list) {
                        if (itemStack.isIn(TagInit.FARM_ITEMS)) {
                            Block.dropStack(player.getWorld(), pos, itemStack);
                            break;
                        }
                    }
                }
            }
        }
        super.onBreak(world, pos, state, player);
    }
}
