package net.skillz.mixin.block;

import net.skillz.util.BonusHelper;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PlantBlock.class)
public abstract class PlantBlockMixin extends Block {

    public PlantBlockMixin(Settings settings) {
        super(settings);
    }

    /*@Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        if (!world.isClient && player != null && !player.isCreative()) {
            int farmingLevel = ((PlayerStatsManagerAccess) player).getPlayerStatsManager().getSkillLevel(Skill.FARMING);
            if (farmingLevel >= ConfigInit.CONFIG.farmingBase && (float) farmingLevel * ConfigInit.CONFIG.farmingChanceBonus > world.random.nextFloat()) {
                List<ItemStack> list = Block.getDroppedStacks(state, (ServerWorld) world, pos, null);
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isIn(TagInit.FARM_ITEMS)) {
                        Block.dropStack(world, pos, list.get(i));
                        break;
                    }
                }
            }
        }

    }*/

    //TODO onBreak
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && player != null && !player.isCreative()) {
            BonusHelper.plantDropChanceBonus(player, state, pos);
        }
        super.onBreak(world, pos, state, player);
    }
}
