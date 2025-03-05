package net.levelz.mixin.entity;

import net.levelz.access.LevelManagerAccess;
import net.levelz.util.BonusHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.levelz.entity.LevelExperienceOrbEntity;
import net.levelz.init.ConfigInit;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity {

    public AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    /*@Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/AnimalEntity;eat(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"), cancellable = true)
    private void interactMobMixin(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        ArrayList<Object> levelList = LevelLists.breedingList;
        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, null, true)) {
            player.sendMessage(Text.translatable("item.levelz." + levelList.get(0) + ".tooltip", levelList.get(1)).formatted(Formatting.RED), true);
            info.setReturnValue(ActionResult.FAIL);
        }
    }*/

    @Inject(method = "Lnet/minecraft/entity/passive/AnimalEntity;breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void breedMixin(ServerWorld world, AnimalEntity other, CallbackInfo info, PassiveEntity passiveEntity) {
        /*if (getLovingPlayer() != null || other.getLovingPlayer() != null) {
            PlayerEntity playerEntity = getLovingPlayer() != null ? getLovingPlayer() : other.getLovingPlayer();
            if (((PlayerStatsManagerAccess) playerEntity).getPlayerStatsManager().getSkillLevel(Skill.FARMING) >= ConfigInit.CONFIG.maxLevel
                    && world.random.nextFloat() < ConfigInit.CONFIG.farmingTwinChance) {
                PassiveEntity extraPassiveEntity = this.createChild(world, other);
                extraPassiveEntity.setBaby(true);
                extraPassiveEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                world.spawnEntityAndPassengers(extraPassiveEntity);
            }
        }*/
        //TODO breedTwinChanceBonus
        if (getLovingPlayer() != null || other.getLovingPlayer() != null) {
            //BonusHelper.breedTwinChanceBonus(world, getLovingPlayer() != null ? getLovingPlayer() : other.getLovingPlayer(), passiveEntity, other);
            PlayerEntity playerEntity = getLovingPlayer() != null ? getLovingPlayer() : other.getLovingPlayer();
            BonusHelper.doRunnableBonus("breedTwinChance", playerEntity, (level) -> {
                if (playerEntity.getRandom().nextFloat() <= ConfigInit.MAIN.BONUSES.twinBreedChanceBonus) {
                    PassiveEntity extraPassiveEntity = passiveEntity.createChild(world, other);
                    extraPassiveEntity.setBaby(true);
                    extraPassiveEntity.refreshPositionAndAngles(passiveEntity.getX(), passiveEntity.getY(), passiveEntity.getZ(), playerEntity.getRandom().nextFloat() * 360F, 0.0F);
                    world.spawnEntityAndPassengers(extraPassiveEntity);
                }
            });
        }
    }

    @Inject(method = "Lnet/minecraft/entity/passive/AnimalEntity;breed(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/passive/AnimalEntity;Lnet/minecraft/entity/passive/PassiveEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private void breedExperienceMixin(ServerWorld world, AnimalEntity other, @Nullable PassiveEntity baby, CallbackInfo info) {
        if (ConfigInit.MAIN.EXPERIENCE.breedingXPMultiplier > 0.0F) {
            LevelExperienceOrbEntity.spawn(world, this.getPos().add(0.0D, 0.1D, 0.0D),
                    (int) ((this.getRandom().nextInt(7) + 1) * ConfigInit.MAIN.EXPERIENCE.breedingXPMultiplier
                            * (ConfigInit.MAIN.EXPERIENCE.dropXPbasedOnLvl && getLovingPlayer() != null
                            ? 1.0F + ConfigInit.MAIN.EXPERIENCE.basedOnMultiplier * ((LevelManagerAccess) getLovingPlayer()).getLevelManager().getOverallLevel()
                            : 1.0F)));
        }
    }

    @Shadow
    @Nullable
    public ServerPlayerEntity getLovingPlayer() {
        return null;
    }
}
