package net.skillz.mixin.player;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.skillz.SkillZMain;
import net.skillz.access.ItemStackAccess;
import net.skillz.access.LevelManagerAccess;
import net.skillz.access.PlayerDropAccess;
import net.skillz.entity.LevelExperienceOrbEntity;
import net.skillz.init.ConfigInit;
import net.skillz.init.EventInit;
import net.skillz.level.LevelManager;
import net.skillz.util.BonusHelper;
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
        /*if (!playerEntity.isCreative() && !playerEntity.isSpectator()) {
            LevelManager levelManager = ((LevelManagerAccess) playerEntity).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(playerEntity.getStackInHand(hand).getItem())) {
                playerEntity.sendMessage(EventInit.sendRestriction(levelManager.getRequiredItemLevel(playerEntity.getStackInHand(hand).getItem()), levelManager), true);
                cir.setReturnValue(ActionResult.PASS);
            }
        }*/
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
        //original *= BonusHelper.exhaustionReductionBonus(this.playerEntity);
        original *= BonusHelper.doIntegerBonus("exhaustionReduction", this.playerEntity, 0, (level) -> (int) (1.0f - (level * ConfigInit.MAIN.BONUSES.exhaustionReductionBonus)));
        return original;
    }

    //Updated
    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private boolean attackKnockbackChanceMixin(boolean original) {
        if (!original && BonusHelper.doBooleanBonus("meleeKnockbackAttackChance", this.playerEntity, ConfigInit.MAIN.BONUSES.meleeKnockbackAttackChanceBonus)) {
            return true;
        }
        return original;
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 1), ordinal = 2)
    private boolean attackCriticalChanceMixin(boolean original) {
        if (!original && BonusHelper.doBooleanBonus("meleeCriticalAttackChance", this.playerEntity, ConfigInit.MAIN.BONUSES.meleeCriticalAttackChanceBonus)) {
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

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private float attackMixin(float original) {
        if (this.playerEntity.isCreative()) {
            return original;
        }
        if (!levelManager.hasRequiredItemLevel(this.getMainHandStack().getItem())) {
            return 0.0f;
        }
        return original;
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 2), ordinal = 0)
    private float attackCriticalDamageMixin(float original) {
        original /= 1.5F;
        //original += BonusHelper.meleeCriticalDamageBonus(this.playerEntity);
        original += BonusHelper.doScalingFloatBonus("meleeCriticalAttackDamage", this.playerEntity, 0 , ConfigInit.MAIN.BONUSES.meleeCriticalAttackDamageBonus);
        original *= 1.5F;
        return original;
    }

    /*@ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float injected(float f) {
        f += BonusHelper.meleeCriticalDamageBonus(this.playerEntity);
        return f;
    }

    @ModifyVariable(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getVelocity()Lnet/minecraft/util/math/Vec3d;"), ordinal = 1)
    private float attackDoubleDamageMixin(float original) {
        if (BonusHelper.meleeDoubleDamageBonus(this.playerEntity)) {
            original *= 2f;
        }
        return original;
    }*/

    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"), index = 1)
    private float attackDoubleDamageMixin(float original) {
        if (/*BonusHelper.meleeDoubleDamageBonus(this.playerEntity)*/BonusHelper.doBooleanBonus("meleeDoubleAttackDamageChance", this.playerEntity, ConfigInit.MAIN.BONUSES.meleeDoubleAttackDamageChanceBonus)) {
            original *= 2f;
        }
        return original;
    }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;dropShoulderEntities()V"), cancellable = true)
    private void damageMixin(DamageSource source, float amount, CallbackInfoReturnable<Boolean> info) {
        BonusHelper.damageReflectionBonus(this.playerEntity, source, amount);
        if (!source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) && /*BonusHelper.evadingDamageBonus(this.playerEntity)*/ BonusHelper.doLinearBooleanBonus("evadingDamageChance", this.playerEntity, ConfigInit.MAIN.BONUSES.evadingDamageChanceBonus)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "eatFood", at = @At(value = "HEAD"))
    private void eatFoodMixin(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> info) {
        if (stack.getItem().isFood()) {
            BonusHelper.doRunnableBonus("foodIncreasion", this.playerEntity, (level) -> {
                net.minecraft.item.FoodComponent foodComponent = stack.getItem().getFoodComponent();
                float multiplier = level * ConfigInit.MAIN.BONUSES.foodIncreasionBonus;
                playerEntity.getHungerManager().add((int) (foodComponent.getHunger() * multiplier), foodComponent.getSaturationModifier() * multiplier);
            });
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