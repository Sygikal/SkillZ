package net.levelz.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import net.levelz.LevelzMain;
import net.levelz.init.ItemInit;
import net.minecraft.item.Items;

public class LevelzEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addRecipe(new EmiBrewingRecipe(EmiStack.of(Items.DRAGON_BREATH), EmiStack.of(Items.NETHER_STAR), EmiStack.of(ItemInit.STRANGE_POTION), LevelzMain.identifierOf( "/strange_potion")));
    }

}
