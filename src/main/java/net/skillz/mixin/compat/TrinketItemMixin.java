package net.skillz.mixin.compat;

import org.spongepowered.asm.mixin.Mixin;

import dev.emi.trinkets.api.TrinketItem;

@Mixin(TrinketItem.class)
public class TrinketItemMixin {

    /*@Inject(method = "equipItem", at = @At("HEAD"), cancellable = true)
    private static void equipItemMixin(PlayerEntity user, ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        ArrayList<Object> levelList = LevelLists.customItemList;
        if (!levelList.isEmpty() && levelList.contains(Registries.ITEM.getId(stack.getItem()).toString())) {
            String string = Registries.ITEM.getId(stack.getItem()).toString();
//            if (!PlayerStatsManager.playerLevelisHighEnough(user, LevelLists.customItemList, string, true)) {
                user.sendMessage(Text.translatable("item.skillz." + levelList.get(levelList.indexOf(string) + 1) + ".tooltip", levelList.get(levelList.indexOf(string) + 2)).formatted(Formatting.RED),
                        true);
                info.setReturnValue(false);
//            }
        } else if (stack.getItem() instanceof ArmorItem armorItem) {
            levelList = LevelLists.armorList;
            String string = armorItem.getMaterial().getName().toLowerCase();
//            if (!PlayerStatsManager.playerLevelisHighEnough(user, levelList, string, true)) {
                user.sendMessage(Text.translatable("item.skillz." + levelList.get(levelList.indexOf(string) + 1) + ".tooltip", levelList.get(levelList.indexOf(string) + 2)).formatted(Formatting.RED),
                        true);
                info.setReturnValue(false);
//            }
        } else {
            levelList = LevelLists.elytraList;
//            if (stack.getItem() == Items.ELYTRA && !PlayerStatsManager.playerLevelisHighEnough(user, levelList, null, true)) {
                user.sendMessage(Text.translatable("item.skillz." + levelList.get(0) + ".tooltip", levelList.get(1)).formatted(Formatting.RED), true);
                info.setReturnValue(false);
//            }
        }
    }*/
}
