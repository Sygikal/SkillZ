package net.skillz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.skillz.SkillZMain;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.init.EventInit;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.content.registry.EnchantmentRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.skillz.screen.SkillRestrictionScreen;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class TooltipUtil {
    public static Text USABLE = Text.translatable("restriction.skillz.usable.tooltip");
    public static Text MINEABLE = Text.translatable("restriction.skillz.mineable.tooltip");
    public static Text CRAFTABLE = Text.translatable("restriction.skillz.craftable.tooltip");

    public static MutableText getRestrictionKey(Identifier id, int value) {
        return TextUtil.getGui("restriction_format", LevelManager.SKILLS.get(id).getText(), value).copy();
        //return Text.translatable("restriction.skillz." + LevelManager.SKILLS.get(id).id() + ".tooltip", value);
    }

    public static void addLines(List<Text> lines, Text category, int id, LevelManager manager, boolean showLines, Map<Integer, PlayerRestriction> restrictionMap) {
        if (showLines || !hasRequiredLevel(manager, id, restrictionMap)) {
            if (restrictionMap.containsKey(id)) {
                PlayerRestriction playerRestriction = restrictionMap.get(id);
                lines.add(category);
                for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                    boolean noHasLevel = manager.getSkillLevel(entry.getKey()) < entry.getValue();
                    if (showLines || noHasLevel) {
                        lines.add(getRestrictionKey(entry.getKey(), entry.getValue()).formatted(noHasLevel ? Formatting.RED : Formatting.GREEN));
                    }
                }
            }
        }
    }

    public static boolean hasRequiredLevel(LevelManager manager, int id, Map<Integer, PlayerRestriction> restrictionMap) {
        if (restrictionMap.containsKey(id)) {
            PlayerRestriction playerRestriction = restrictionMap.get(id);
            for (Map.Entry<Identifier, Integer> entry : playerRestriction.getSkillLevelRestrictions().entrySet()) {
                if (manager.getSkillLevel(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void renderItemTooltip(MinecraftClient client, ItemStack stack, List<Text> lines) {
        if (client.player != null) {
            LevelManager levelManager = ((LevelManagerAccess) client.player).getLevelManager();
            boolean showLines = client.player.isCreative() || !ConfigInit.CLIENT.hideReachedLevels || client.currentScreen instanceof SkillRestrictionScreen; // Add all lines, not only the missing ones

            if (stack.getItem() instanceof BlockItem blockItem) {
                int blockId = Registries.BLOCK.getRawId(blockItem.getBlock());
                addLines(lines, USABLE, blockId, levelManager, showLines, LevelManager.BLOCK_RESTRICTIONS);
                addLines(lines, MINEABLE, blockId, levelManager, showLines, LevelManager.MINING_RESTRICTIONS);
            }else if (stack.getItem() instanceof SpawnEggItem spawnEggItem) {
                addLines(lines, USABLE, Registries.ENTITY_TYPE.getRawId(spawnEggItem.getEntityType(stack.getNbt())), levelManager, showLines, LevelManager.ENTITY_RESTRICTIONS);
            }

            int itemId = Registries.ITEM.getRawId(stack.getItem());
            addLines(lines, USABLE, itemId, levelManager, showLines, LevelManager.ITEM_RESTRICTIONS);
            addLines(lines, CRAFTABLE, itemId, levelManager, showLines, LevelManager.CRAFTING_RESTRICTIONS);

            final Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(stack);

            if (!enchantments.isEmpty()) {
                for (Enchantment enchantment : enchantments.keySet()) {
                    final Text fullName = enchantment.getName(enchantments.get(enchantment));
                    for (Text line : lines) {
                        if (line.equals(fullName)) {
                            MutableText asd = Text.literal("");
                            if (showLines || !levelManager.hasRequiredEnchantmentLevel(Registries.ENCHANTMENT.getEntry(enchantment), enchantments.get(enchantment))) {
                                int enchantmentId = EnchantmentRegistry.getId(Registries.ENCHANTMENT.getEntry(enchantment), enchantments.get(enchantment));
                                if (LevelManager.ENCHANTMENT_RESTRICTIONS.containsKey(enchantmentId)) {
                                    asd = EventInit.sendRestriction(levelManager.getRequiredEnchantmentLevel(Registries.ENCHANTMENT.getEntry(enchantment), enchantments.get(enchantment)), levelManager, showLines);
                                }
                            }
                            if (!Objects.equals(asd.getString(), "")) {
                                lines.set(lines.indexOf(line), line.copy().append(ScreenTexts.SPACE).append(Text.literal("(").formatted(Formatting.GRAY))
                                        .append(asd).append(Text.literal(")").formatted(Formatting.GRAY)));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void renderTooltip(MinecraftClient client, DrawContext context) {
        if (client.crosshairTarget != null && ConfigInit.CLIENT.showRestrictionOnHUD) {
            boolean showLines = client.player.isCreative() || !ConfigInit.CLIENT.hideReachedLevels;

            HitResult hitResult = client.crosshairTarget;
            LevelManager levelManager = ((LevelManagerAccess) client.player).getLevelManager();
            List<Text> textList = new ArrayList<>();
            Identifier identifier = null;
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityType<?> entityType = ((EntityHitResult) hitResult).getEntity().getType();
                textList.add(Text.of(entityType.getName().getString()));
                addLines(textList, USABLE, Registries.ENTITY_TYPE.getRawId(entityType), levelManager, showLines, LevelManager.ENTITY_RESTRICTIONS);

            } else if (hitResult.getType() == HitResult.Type.BLOCK) {
                Block block = client.world.getBlockState(((BlockHitResult) hitResult).getBlockPos()).getBlock();
                textList.add(Text.of(block.getName().getString()));
                addLines(textList, MINEABLE, Registries.BLOCK.getRawId(block), levelManager, showLines, LevelManager.MINING_RESTRICTIONS);
                addLines(textList, USABLE, Registries.BLOCK.getRawId(block), levelManager, showLines, LevelManager.BLOCK_RESTRICTIONS);
                identifier = Registries.BLOCK.getId(block);
            }
            if (textList.size() > 1) {
                renderTooltip(client, context, textList,
                        identifier, context.getScaledWindowWidth() / 2 + ConfigInit.CLIENT.hudInfoX, ConfigInit.CLIENT.hudInfoY);
            }
        }
    }

    private static void renderTooltip(MinecraftClient client, DrawContext context, List<Text> textList, @Nullable Identifier identifier, int x, int y) {
        int maxTextWidth = 0;
        for (int i = 0; i < textList.size(); i++) {
            if (client.textRenderer.getWidth(textList.get(i)) > maxTextWidth) {
                maxTextWidth = client.textRenderer.getWidth(textList.get(i));
                if (i == 0 && identifier != null) {
                    maxTextWidth += 22;
                }
            }
        }
        maxTextWidth += 5;

        context.getMatrices().push();

        int colorStart = 0xBF191919; // background
        int colorTwo = 0xBF7F0200; // light border
        int colorThree = 0xBF380000; // darker border

        DrawUtil.render(context, x - maxTextWidth / 2 - 3, y + 4, maxTextWidth, textList.size() * 10 + 11, 400, colorStart, colorTwo, colorThree);

        context.getMatrices().translate(0.0, 0.0, 400.0);

        int i = 9;
        for (Text text : textList) {
            if (i == 9) {
                context.drawText(client.textRenderer, text, x - maxTextWidth / 2 + (identifier != null ? 20 : 0), y + i, 0xFFFFFF, false);
            } else {
                context.drawText(client.textRenderer, text, x - maxTextWidth / 2, y + i + 8, 0xFFFFFF, false);
            }
            i += 10;
        }

        if (identifier != null) {
            context.drawItem(Registries.ITEM.get(identifier).getDefaultStack(), x - maxTextWidth / 2, y + 5);
        }
        context.getMatrices().pop();
    }

}
