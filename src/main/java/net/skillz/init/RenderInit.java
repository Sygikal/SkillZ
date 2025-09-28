package net.skillz.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.skillz.SkillZMain;
import net.skillz.content.entity.render.LevelExperienceOrbEntityRenderer;
import net.skillz.screen.SkillInfoScreen;
import net.skillz.screen.SkillRestrictionScreen;
import net.skillz.screen.LevelScreen;
import net.skillz.screen.widget.*;
import net.skillz.util.TextUtil;
import net.skillz.util.TooltipUtil;
import dev.sygii.tabapi.TabAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderInit {

    public static final Identifier SKILL_TAB_ICON = SkillZMain.identifierOf("textures/gui/sprites/skill_tab_icon.png");
    public static final Identifier BAG_TAB_ICON = SkillZMain.identifierOf("textures/gui/sprites/bag_tab_icon.png");

    public static final Identifier MINEABLE_INFO = SkillZMain.identifierOf("mineable_info");
    public static final Identifier USABLE_INFO = SkillZMain.identifierOf("usable_info");

    public static final Identifier MINEABLE_LEVEL_INFO = SkillZMain.identifierOf("mineable_level_info");

    public static void init() {
        EntityRendererRegistry.register(EntityInit.LEVEL_EXPERIENCE_ORB, LevelExperienceOrbEntityRenderer::new);

        TabAPI.registerInventoryTab(new VanillaInventoryTab(SkillZMain.identifierOf("inventory_tab"), Text.translatable("container.crafting"), BAG_TAB_ICON, 0, InventoryScreen.class));
        TabAPI.registerInventoryTab(new SkillTab(SkillZMain.identifierOf("skill_tab"), TextUtil.getGui("skill_screen.title"), SKILL_TAB_ICON, 1, LevelScreen.class, SkillInfoScreen.class, SkillRestrictionScreen.class));

        /*TabAPI.registerSideInventoryTab(new BookTab(Text.translatable("restriction.levelz.enchantments"),
                new RestrictionBook(LevelManager.ENCHANTMENT_RESTRICTIONS, Text.translatable("restriction.levelz.enchantments"), 3),
                new Color(178, 127, 255), 0, SkillRestrictionScreen.class));

        TabAPI.registerSideInventoryTab(new BookTab(Text.translatable("restriction.levelz.entity_usage"),
                new RestrictionBook(LevelManager.ENTITY_RESTRICTIONS, Text.translatable("restriction.levelz.entity_usage"), 2),
                new Color(242, 127, 255), 1, SkillRestrictionScreen.class));

        TabAPI.registerSideInventoryTab(new BookTab(Text.translatable("restriction.levelz.block_usage"),
                new RestrictionBook(LevelManager.BLOCK_RESTRICTIONS, Text.translatable("restriction.levelz.block_usage"), 1),
                new Color(123, 175, 255), 1, SkillRestrictionScreen.class));*/


        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            TooltipUtil.renderTooltip(MinecraftClient.getInstance(), drawContext);
        });

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            TooltipUtil.renderItemTooltip(MinecraftClient.getInstance(), stack, lines);
        });
    }
}
