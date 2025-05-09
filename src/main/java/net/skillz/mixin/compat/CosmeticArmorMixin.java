package net.skillz.mixin.compat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;

// dev.emi.trinkets.api.TrinketsApi;
//import io.github.apace100.cosmetic_armor.CosmeticArmor;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

//@Mixin(CosmeticArmor.class)
public class CosmeticArmorMixin {

    //@Shadow
    @Final
    @Mutable
    public static TagKey<Item> BLACKLIST;

    /*@Inject(method = "Lio/github/apace100/cosmetic_armor/CosmeticArmor;onInitialize()V", at = @At(value = "INVOKE", target = "Ldev/emi/trinkets/api/TrinketsApi;registerTrinketPredicate(Lnet/minecraft/util/Identifier;Lcom/mojang/datafixers/util/Function3;)V"), cancellable = true)
    private void onInitializeMixin(CallbackInfo info) {
        for (int i = 0; i < 4; i++) {
            EquipmentSlot slot = EquipmentSlot.fromTypeIndex(EquipmentSlot.Type.ARMOR, i);
            TrinketsApi.registerTrinketPredicate(id(slot.getName()), (stack, slotReference, entity) -> {
                if (stack.isIn(BLACKLIST)) {
                    return TriState.FALSE;
                }
                if (entity instanceof PlayerEntity && stack.getItem() instanceof ArmorItem) {
                    ArrayList<Object> levelList = LevelLists.customItemList;
                    try {
                        if (!levelList.isEmpty() && levelList.contains(Registries.ITEM.getId(stack.getItem()).toString())) {
                            if (!PlayerStatsManager.playerLevelisHighEnough((PlayerEntity) entity, levelList, Registries.ITEM.getId(stack.getItem()).toString(), true))
                                return TriState.FALSE;
                        } else {
                            levelList = LevelLists.armorList;
                            if (!PlayerStatsManager.playerLevelisHighEnough((PlayerEntity) entity, levelList, ((ArmorItem) stack.getItem()).getMaterial().getName().toLowerCase(), true))
                                return TriState.FALSE;
                        }
                    } catch (AbstractMethodError ignore) {
                    }
                }
                if (MobEntity.getPreferredEquipmentSlot(stack) == slot) {
                    return TriState.TRUE;
                }
                return TriState.DEFAULT;
            });
        }
        info.cancel();
    }*/

    //@Shadow
    private static Identifier id(String path) {
        return null;
    }
}
