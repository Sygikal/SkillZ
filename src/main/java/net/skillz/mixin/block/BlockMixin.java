package net.skillz.mixin.block;

import java.util.List;

import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.skillz.access.LevelManagerAccess;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.DoubleOreDropBonus;
import net.skillz.level.LevelManager;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.skillz.content.entity.LevelExperienceOrbEntity;
import net.skillz.init.ConfigInit;
import net.skillz.init.EntityInit;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Nullable
    private ServerPlayerEntity serverPlayerEntity = null;

    //TODO explosions
    @Shadow
    protected abstract Block asBlock();

    @Inject(method = "shouldDropItemsOnExplosion", at = @At(value = "HEAD"))
    private void shouldDropItemsOnExplosion(Explosion explosion, CallbackInfoReturnable<Boolean> cir) {
        if (explosion.getCausingEntity() instanceof PlayerEntity playerEntity && !playerEntity.isCreative() && !((LevelManagerAccess) playerEntity).getLevelManager().hasRequiredMiningLevel(asBlock())) {
            cir.setReturnValue(false);
        }
    }

    //TODO dropStacks
    @Inject(method = "dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;"), cancellable = true)
    private static void dropStacksMixin(BlockState state, World world, BlockPos pos, @Nullable BlockEntity blockEntity, Entity entity, ItemStack stack, CallbackInfo info) {
        if (entity instanceof PlayerEntity playerEntity) {
            if (playerEntity.isCreative()) {
                return;
            }
            if (EntityInit.isRedstoneBitsLoaded && entity.getClass().getName().contains("RedstoneBitsFakePlayer")) {
            } else {
                LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
                if (!levelManager.hasRequiredMiningLevel(state.getBlock())) {
                    info.cancel();
                } else if (!levelManager.hasRequiredItemLevel(stack.getItem())) {
                    info.cancel();
                }
            }
        }
    }

    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getDroppedStacks(Lnet/minecraft/loot/context/LootContextParameterSet$Builder;)Ljava/util/List;"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void getDroppedStacksMixin(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, CallbackInfoReturnable<List<ItemStack>> info, LootContextParameterSet.Builder builder) {
        if (entity instanceof PlayerEntity playerEntity) {
            if (state.isIn(ConventionalBlockTags.ORES) && EnchantmentHelper.getEquipmentLevel(Enchantments.SILK_TOUCH, playerEntity) <= 0) {
                if (BonusManager.doBooleanBonus(DoubleOreDropBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.doubleOreDropChance)) {
                    List<ItemStack> list = state.getDroppedStacks(builder);
                    if (!list.isEmpty()) {
                        Block.dropStack(playerEntity.getWorld(), pos, state.getDroppedStacks(builder).get(0).split(1));
                    }
                }
            }
        }
    }

    //TODO: level manager
    @Inject(method = "dropExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;spawn(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;I)V"))
    protected void dropExperienceMixin(ServerWorld world, BlockPos pos, int size, CallbackInfo info) {
        if (ConfigInit.MAIN.EXPERIENCE.oreXPMultiplier > 0.0F) {
            LevelExperienceOrbEntity.spawn(world, Vec3d.ofCenter(pos),
                    (int) (size * ConfigInit.MAIN.EXPERIENCE.oreXPMultiplier
                            * (ConfigInit.MAIN.EXPERIENCE.dropXPbasedOnLvl && this.serverPlayerEntity != null
                            ? 1.0F + ConfigInit.MAIN.EXPERIENCE.basedOnMultiplier * ((LevelManagerAccess) this.serverPlayerEntity).getLevelManager().getOverallLevel()
                            : 1.0F)));
        }
    }

    @Inject(method = "onBreak", at = @At(value = "HEAD"))
    private void onBreakMixin(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo info) {
        if (!world.isClient()) {
            this.serverPlayerEntity = (ServerPlayerEntity) player;
        }
    }

}
