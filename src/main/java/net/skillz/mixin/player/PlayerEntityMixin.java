package net.skillz.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.skillz.SkillZMain;
import net.skillz.access.ItemStackAccess;
import net.skillz.access.LevelManagerAccess;
import net.skillz.access.PlayerDropAccess;
import net.skillz.bonus.BonusManager;
import net.skillz.bonus.impl.combat.*;
import net.skillz.bonus.impl.player.DamageReflectionBonus;
import net.skillz.bonus.impl.player.EvadeDamageBonus;
import net.skillz.bonus.impl.player.ExhaustionReductionBonus;
import net.skillz.bonus.impl.player.IncreasedNutritionBonus;
import net.skillz.content.entity.LevelExperienceOrbEntity;
import net.skillz.init.ConfigInit;
import net.skillz.level.LevelManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Debug(export=true)
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements LevelManagerAccess, PlayerDropAccess {

    private final PlayerEntity playerEntity = (PlayerEntity) (Object) this;
    @Unique
    private final LevelManager levelManager = new LevelManager(playerEntity);

    @Unique
    private int killedMobsInChunk;
    @Unique
    @Nullable
    private Chunk killedMobChunk;

    public PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interactMixin(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (SkillZMain.shouldRestrictItem(playerEntity, playerEntity.getStackInHand(hand).getItem())) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At(value = "TAIL"))
    public void readCustomDataFromNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.levelManager.readNbt(nbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At(value = "TAIL"))
    public void writeCustomDataToNbtMixin(NbtCompound nbt, CallbackInfo info) {
        this.levelManager.writeNbt(nbt);
    }

    @ModifyArg(method = "addExhaustion", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;addExhaustion(F)V"), index = 0)
    private float injected(float original) {
        original *= BonusManager.returnBonusValue(ExhaustionReductionBonus.ID, playerEntity, 1);
        return original;
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private boolean attackKnockbackChanceMixin(boolean original) {
        if (!original && BonusManager.doBooleanBonus(AttackKnockbackBonus.ID, this.playerEntity, ConfigInit.MAIN.BONUSES.knockbackAttackChance)) {
            return true;
        }
        return original;
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 1), ordinal = 2)
    private boolean attackCriticalChanceMixin(boolean original) {
        if (!original && BonusManager.doBooleanBonus(CriticalAttackBonus.ID, this.playerEntity, ConfigInit.MAIN.BONUSES.criticalAttackChance)) {
            return true;
        }
        return original;
    }

    //Sweeping
    /*@ModifyVariable(method = "attack", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getKnockback(Lnet/minecraft/entity/LivingEntity;)I"), ordinal = 1)
    private boolean attackSweepingMixin(boolean original) {
        System.out.println("asd");
        if (!original && BonusHelper.nonMeleeSweepingAttackChanceBonus(this.playerEntity)) {
            return true;
        }
        return original;
    }*/

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 2), ordinal = 0)
    private float attackCriticalDamageMixin(float original) {
        original += BonusManager.doBonus(BonusManager.BonusTypes.SCALING_FLOAT, CriticalAttackDamageBonus.ID, this.playerEntity, (float) 0, ConfigInit.MAIN.BONUSES.bonusCriticalAttackDamage);
        return original;
    }

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float attackDoubleDamageMixin(float original) {
        if (BonusManager.doBooleanBonus(DoubleAttackDamageBonus.ID, this.playerEntity, ConfigInit.MAIN.BONUSES.doubleAttackDamageChance)) {
            original *= 2f;
        }
        return original;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropShoulderEntities()V"), cancellable = true)
    private void damageMixin(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        if (source.getAttacker() != null) {
            if (BonusManager.doBooleanBonus(DamageReflectionBonus.ID, playerEntity, ConfigInit.MAIN.BONUSES.damageReflectionChance)) {
                source.getAttacker().damage(source, amount/* * level * ConfigInit.MAIN.BONUSES.damageReflectionBonus*/);
            }
        }
        if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && BonusManager.doBonus(BonusManager.BonusTypes.LINEAR_BOOLEAN, EvadeDamageBonus.ID, this.playerEntity, false, ConfigInit.MAIN.BONUSES.evadeDamageChance)) {
            playerEntity.getWorld().playSound(null, playerEntity.getBlockPos(), SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 1F, 1.5F);
            info.setReturnValue(false);
        }
    }

    @Inject(method = "eatFood", at = @At(value = "HEAD"))
    private void eatFoodMixin(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        if (stack.getItem().isFood()) {
            if (BonusManager.hasBonus(IncreasedNutritionBonus.ID, playerEntity)) {
                net.minecraft.item.FoodComponent foodComponent = stack.getItem().getFoodComponent();
                float multiplier = BonusManager.returnBonusValue(IncreasedNutritionBonus.ID, playerEntity, 0) * ConfigInit.MAIN.BONUSES.bonusFoodNutrition;
                playerEntity.getHungerManager().add((int) (foodComponent.getHunger() * multiplier), foodComponent.getSaturationModifier() * multiplier);
            }
        }
    }

    @Override
    public LevelManager getLevelManager() {
        return this.levelManager;
    }

    @Override
    public void increaseKilledMobStat(Chunk chunk) {
        if (killedMobChunk != null && killedMobChunk == chunk) {
            killedMobsInChunk++;
        } else {
            killedMobChunk = chunk;
            killedMobsInChunk = 0;
        }
    }

    @Override
    public void resetKilledMobStat() {
        killedMobsInChunk = 0;
    }

    @Override
    public boolean allowMobDrop() {
        return killedMobsInChunk < ConfigInit.MAIN.LEVEL.mobKillCount;
    }

    @Override
    protected void dropXp() {
        if (this.playerEntity.getWorld() instanceof ServerWorld serverWorld && this.shouldDropXp() && this.getWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT) && ConfigInit.MAIN.EXPERIENCE.resetCurrentXp) {
            LevelExperienceOrbEntity.spawn(serverWorld, this.getPos(), (int) (this.levelManager.getLevelProgress() * this.levelManager.getNextLevelExperience()));
        }
        super.dropXp();
    }

    @ModifyReturnValue(method = "getEquippedStack", at = @At(value = "RETURN"))
    private ItemStack setHolder(ItemStack original) {
        ((ItemStackAccess)(Object)(original)).setHoldingPlayer(this.playerEntity);
        return original;
    }

}