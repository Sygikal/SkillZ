package net.skillz.screen.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.SkillZMain;
import net.skillz.level.LevelManager;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.content.registry.EnchantmentRegistry;
import net.skillz.content.registry.EnchantmentZ;
import net.skillz.screen.LevelScreen;
import net.skillz.util.DrawUtil;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.skillz.util.TextUtil;
import net.skillz.util.TooltipUtil;
import org.jetbrains.annotations.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;

import java.io.FileNotFoundException;
import java.util.*;

@Environment(EnvType.CLIENT)
public class LineWidget {

    private final MinecraftClient client;
    @Nullable
    private final Text text;
    @Nullable
    private final Map<Integer, PlayerRestriction> restrictions;
    public final int code;

    private Map<Integer, ItemStack> customStacks;
    private Map<Integer, Identifier> customImages;

    /**
     * @param code 0 = item, 1 = block, 2 = entity, 3 = enchantment
     */
    public LineWidget(MinecraftClient client, @Nullable Text text, @Nullable Map<Integer, PlayerRestriction> restrictions, int code) {
        this.client = client;
        this.text = text;
        this.restrictions = restrictions;
        this.code = code;

        if (this.text == null) {
            if (this.code == 2) {
                this.customStacks = new HashMap<>();
                this.customImages = new HashMap<>();
                for (Integer id : this.restrictions.keySet()) {
                    EntityType<?> entityType = Registries.ENTITY_TYPE.get(id);
                    boolean imageExists = false;
                    try {
                        client.getResourceManager().getResourceOrThrow(SkillZMain.identifierOf("textures/gui/sprites/entity/" + Registries.ENTITY_TYPE.getId(entityType).getPath() + ".png"));
                        imageExists = true;
                    } catch (FileNotFoundException ignored) {
                    }
                    if (imageExists) {
                        this.customImages.put(id, SkillZMain.identifierOf("textures/gui/sprites/entity/" + Registries.ENTITY_TYPE.getId(entityType).getPath() + ".png"));
                    } else if (SpawnEggItem.forEntity(entityType) != null) {
                        this.customStacks.put(id, new ItemStack(Objects.requireNonNull(SpawnEggItem.forEntity(entityType))));
                    } else if (entityType.create(this.client.world) instanceof AbstractMinecartEntity vehicleEntity) {
                        this.customStacks.put(id, new ItemStack(vehicleEntity.getItem()));
                    } else if (entityType.create(this.client.world) instanceof BoatEntity vehicleEntity) {
                        this.customStacks.put(id, new ItemStack(vehicleEntity.asItem()));
                    } else {
                        this.customImages.put(id, SkillZMain.identifierOf("textures/gui/sprites/entity/default.png"));
                    }
                }
            } else if (this.code == 3) {
                this.customStacks = new HashMap<>();
                for (Integer id : this.restrictions.keySet()) {
                    EnchantmentZ enchantmentZ = EnchantmentRegistry.getEnchantmentZ(id);
                    this.customStacks.put(id, EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantmentZ.getEntry().value(), enchantmentZ.getLevel())));
                }
            }
        }
    }

    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        if (text != null) {
            drawContext.drawText(this.client.textRenderer, this.text, x, y + 4, 0x3F3F3F, false);
        } else {
            int separator = 0;
            //int count = 0;
            //int vert = 0;
            boolean showTooltip = false;
            for (Map.Entry<Integer, PlayerRestriction> entry : this.restrictions.entrySet()) {
                Text tooltipTitle = Text.literal(entry.getKey().toString());
                //if (code != 2) {
                /*if (count > 8) {
                    count = 0;
                    separator = 0;
                    vert += 18;
                }*/
                drawContext.drawTexture(LevelScreen.ICON_TEXTURE, x + separator - 1, y - 1, 0, 148, 18, 18);
                //}
                if (this.code == 0) {
                    Item item = Registries.ITEM.get(entry.getKey());
                    tooltipTitle = item.getName();
                    drawContext.drawItem(Registries.ITEM.get(entry.getKey()).getDefaultStack(), x + separator, y);
                    if (DrawUtil.isPointWithinBounds(x + separator, y, 16, 16, mouseX, mouseY)) {
                        drawContext.drawItemTooltip(this.client.textRenderer, Registries.ITEM.get(entry.getKey()).getDefaultStack(), mouseX, mouseY);
                        showTooltip = true;
                    }
                } else if (this.code == 1) {
                    Block block = Registries.BLOCK.get(entry.getKey());
                    tooltipTitle = block.getName();
                    drawContext.drawItem(block.asItem().getDefaultStack(), x + separator, y);
                    if (DrawUtil.isPointWithinBounds(x + separator, y, 16, 16, mouseX, mouseY)) {
                        drawContext.drawItemTooltip(this.client.textRenderer, block.asItem().getDefaultStack(), mouseX, mouseY);
                        showTooltip = true;
                    }
                } else if (this.code == 2) {
                    EntityType<?> entityType = Registries.ENTITY_TYPE.get(entry.getKey());
                    tooltipTitle = entityType.getName();
                    if (this.customStacks.containsKey(entry.getKey()) && this.customStacks.get(entry.getKey()).getItem() instanceof SpawnEggItem) {
                        if (DrawUtil.isPointWithinBounds(x + separator, y, 16, 16, mouseX, mouseY)) {
                            drawContext.drawItemTooltip(this.client.textRenderer, this.customStacks.get(entry.getKey()), mouseX, mouseY);
                            showTooltip = true;
                        }
                    }
                    Entity entity = entityType.create(this.client.world);
                    //InventoryScreen.drawEntity(drawContext, x + separator + 9, y + 18, 10, (float)(x + separator) - mouseX, (float)y - mouseY, (LivingEntity) entity);
                    if (entity instanceof LivingEntity) {
                        InventoryScreen.drawEntity(drawContext, x + separator + 9, y + 18, 10, (float)(x + separator + 9) - mouseX, (float)(y) - mouseY, (LivingEntity) entity);
                    }else {
                        DrawUtil.drawEntity(drawContext, x + separator + 9, y + 18, 10, (float) (x + separator) - mouseX, (float) y - mouseY, entity);
                    }
                    //separator += 18;
                } else {// if (this.code == 3) {
                    ItemStack stack = this.customStacks.get(entry.getKey());
                    if (EnchantmentHelper.get(stack).entrySet().iterator().hasNext()) {
                        Map.Entry<Enchantment, Integer> asd = EnchantmentHelper.get(stack).entrySet().iterator().next();
                        Enchantment ench = asd.getKey();
                        int level = asd.getValue();
                        tooltipTitle = ench.getName(level);
                    /*for (Enchantment enchantment : asd.keySet()) {
                        tooltipTitle = enchantment.getName(asd.get(enchantment));
                    }*/
                        //System.out.println(asd);
                        //stack.getEnchantments();
                        //RegistryEntry<Enchantment> enchantment = EnchantmentHelper.getEnchantments(stack).getEnchantments().stream().findFirst().get();
                    /*int level = stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(enchantment);
                    tooltipTitle = Enchantment.getName(enchantment, level);*/
                        drawContext.drawItem(stack, x + separator, y);
                        if (DrawUtil.isPointWithinBounds(x + separator, y, 16, 16, mouseX, mouseY)) {
                            drawContext.drawItemTooltip(this.client.textRenderer, stack, mouseX, mouseY);
                            showTooltip = true;
                        }
                    }
                }
                if (!showTooltip && DrawUtil.isPointWithinBounds(x + separator, y, 16, 16, mouseX, mouseY)) {
                    List<Text> tooltip = new ArrayList<>();
                    tooltip.add(tooltipTitle);
                    for (Map.Entry<Identifier, Integer> restriction : entry.getValue().getSkillLevelRestrictions().entrySet()) {
                        tooltip.add(TooltipUtil.getRestrictionKey(restriction.getKey(), restriction.getValue()));
                    }
                    drawContext.drawTooltip(this.client.textRenderer, tooltip, mouseX, mouseY);
                    showTooltip = true;
                }
                separator += 18;
                //count++;
            }
        }
    }
}

