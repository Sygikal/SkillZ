package net.skillz.mixin.player;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.EventInit;
import net.skillz.level.LevelManager;
import net.minecraft.block.Block;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.init.ConfigInit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Debug(export = true)
@Environment(EnvType.CLIENT)
@Mixin(value = MinecraftClient.class, priority = 999)
public class MinecraftClientMixin {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public ClientWorld world;

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isItemEnabled(Lnet/minecraft/resource/featuretoggle/FeatureSet;)Z"), cancellable = true)
    private void doItemUseMixin(CallbackInfo ci, @Local ItemStack itemStack) {
        if (player != null && SkillZMain.shouldRestrictItem(player, itemStack.getItem())) {
            ci.cancel();
        }
        /*if (!player.isCreative() && !player.isSpectator()) {
            LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
            if (!levelManager.hasRequiredItemLevel(itemStack.getItem())) {
                player.sendMessage(EventInit.sendRestriction(levelManager.getRequiredItemLevel(itemStack.getItem()), levelManager), true);
                ci.cancel();
            }
        }*/
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void handleBlockBreakingMixin(boolean breaking, CallbackInfo info) {
        if (restrictItemUsage() || restrictBlockBreaking(breaking)) {
            info.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void doAttackMixin(CallbackInfoReturnable<Boolean> info) {
        if (restrictItemUsage()) {
            info.setReturnValue(false);
        }
    }

    @Unique
    private boolean restrictItemUsage() {
        if (ConfigInit.MAIN.LEVEL.lockedHandUsage && player != null) {
            Item item = player.getMainHandStack().getItem();
            if (item != null && !item.equals(Items.AIR)) {
                /*LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                if (!levelManager.hasRequiredItemLevel(item)) {
                    player.sendMessage(EventInit.sendRestriction(levelManager.getRequiredItemLevel(item), levelManager), true);
                    return true;
                }*/
                return SkillZMain.shouldRestrictItem(player, item);
            }
        }
        return false;
    }

    @Unique
    private boolean restrictBlockBreaking(boolean breaking) {
        if (ConfigInit.MAIN.LEVEL.lockedBlockBreaking && player != null && !player.isCreative()) {
            if (this.crosshairTarget != null && this.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
                Block block = world.getBlockState(blockHitResult.getBlockPos()).getBlock();
                LevelManager levelManager = ((LevelManagerAccess) player).getLevelManager();
                if (!levelManager.hasRequiredMiningLevel(block)) {
                    if (breaking) {
                        player.sendMessage(EventInit.sendRestriction(levelManager.getRequiredMiningLevel(block), levelManager), true);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /*private boolean restrictHandUsage(boolean blockBreaking) {
        if (ConfigInit.CONFIG.lockedHandUsage && player != null && !player.isCreative()) {
            Item item = player.getMainHandStack().getItem();
            if (item != null && !item.equals(Items.AIR)) {
                ArrayList<Object> levelList = LevelLists.customItemList;
                if (!levelList.isEmpty() && levelList.contains(Registries.ITEM.getId(item).toString())) {
                    String string = Registries.ITEM.getId(item).toString();
                    if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, string, true)) {
                        player.sendMessage(
                                Text.translatable("item.skillz." + levelList.get(levelList.indexOf(string) + 1) + ".tooltip", levelList.get(levelList.indexOf(string) + 2)).formatted(Formatting.RED),
                                true);
                        return true;
                    }
                } else if (item instanceof ToolItem) {
                    levelList = null;
                    if (item instanceof SwordItem) {
                        levelList = LevelLists.swordList;
                    } else if (item instanceof AxeItem) {
                        if (ConfigInit.CONFIG.bindAxeDamageToSwordRestriction && !blockBreaking) {
                            levelList = LevelLists.swordList;
                        } else {
                            levelList = LevelLists.axeList;
                        }
                    } else if (item instanceof HoeItem) {
                        levelList = LevelLists.hoeList;
                    } else if (item instanceof PickaxeItem || item instanceof ShovelItem) {
                        levelList = LevelLists.toolList;
                    }
                    if (levelList != null && ((ToolItem) item).getMaterial() != null) {
                        String material = ((ToolItem) item).getMaterial().toString().toLowerCase();
                        if (!PlayerStatsManager.playerLevelisHighEnough(player, levelList, material, true)) {
                            player.sendMessage(Text
                                    .translatable("item.skillz." + levelList.get(levelList.indexOf(material) + 1).toString() + ".tooltip", levelList.get(levelList.indexOf(material) + 2).toString())
                                    .formatted(Formatting.RED), true);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }*/
}
