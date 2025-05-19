package net.skillz.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.skillz.SkillZMain;
import net.skillz.access.ClientPlayerAccess;
import net.skillz.access.LevelManagerAccess;
import net.skillz.init.ConfigInit;
import net.skillz.init.KeyInit;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.level.SkillAttribute;
import net.skillz.network.packet.AttributeSyncPacket;
import net.skillz.network.packet.StatPacket;
import net.skillz.screen.widget.BookWidget;
import net.skillz.util.DrawUtil;
import dev.sygii.tabapi.api.Tab;
import dev.sygii.tabapi.util.DrawTabHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class LevelScreen extends Screen implements Tab {

    public static final Identifier BACKGROUND_TEXTURE = SkillZMain.identifierOf("textures/gui/skill_background.png");
    public static final Identifier ATTRIBUTE_BACKGROUND_TEXTURE = SkillZMain.identifierOf("textures/gui/attribute_background.png");
    public static final Identifier ICON_TEXTURE = SkillZMain.identifierOf("textures/gui/icons.png");

    private final int backgroundWidth = 200;
    private final int backgroundHeight = 215;
    private int x;
    private int y;

    private LevelManager levelManager;
    private ClientPlayerEntity clientPlayerEntity;
    //private Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI).rotateLocalY(2.7f);
    //private boolean turnClientPlayer = false;

    private List<SkillAttribute> attributes = new ArrayList<>();
    private boolean showAttributes = false;
    private int attributeRow = 0;

    private final WidgetButtonPage[] levelButtons = new WidgetButtonPage[12];
    private ArrayList<WidgetButtonPage> newLeveButtons = new ArrayList<>();
    private int skillRow = 0;

    private List<BookWidget> bookWidgets = new ArrayList<>();

    public LevelScreen() {
        super(Text.translatable("screen.skillz.skill_screen"));
    }

    @Override
    protected void init() {
        super.init();
        ClientPlayNetworking.send(new AttributeSyncPacket());
        this.attributes.clear();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        this.levelManager = ((LevelManagerAccess) this.client.player).getLevelManager();
        this.clientPlayerEntity = this.client.interactionManager.createPlayer(this.client.world, this.client.player.getStatHandler(), this.client.player.getRecipeBook(), false, false);
        ((ClientPlayerAccess) this.clientPlayerEntity).setShouldRenderClientName(false);

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (!this.client.player.getEquippedStack(equipmentSlot).isEmpty()) {
                this.clientPlayerEntity.equipStack(equipmentSlot, this.client.player.getEquippedStack(equipmentSlot));
            }
        }

        Map<Integer, SkillAttribute> skillAttributes = new HashMap<>();
        int attributeCount = 0;
        for (Skill skill : LevelManager.SKILLS.values()) {
            for (SkillAttribute skillAttribute : skill.attributes()) {
                if (skillAttribute.getId() < 0) {
                    continue;
                }
                skillAttributes.put(skillAttribute.getId(), skillAttribute);
                attributeCount++;

            }
        }
        for (int i = 0; i < attributeCount; i++) {
            this.attributes.add(skillAttributes.get(i));
        }

        Map<String, Skill> asd = LevelManager.SKILLS.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingInt(Skill::index))).collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        int i = 0;
        for (Skill skill : asd.values()) {
            this.newLeveButtons.add(new WidgetButtonPage(skill,this.x + (i % 2 == 0 ? 80 : 169), this.y + 91 + i / 2 * 20, 13, 13, 33, 42, true, true, null, button -> {ClientPlayNetworking.send(new StatPacket(skill.id(), 1));}));

            i++;
        }
        updateLevelButtons();

        this.bookWidgets.clear();
        bookWidgets.add(new BookWidget(Text.translatable("text.skillz.gui.attributes"), this.x + 178, this.y + 5,
                () -> this.showAttributes = !this.showAttributes,
                new Color(255, 206, 127), !this.attributes.isEmpty()));
        bookWidgets.add(new BookWidget(Text.translatable("restriction.skillz.crafting"), this.x + 160, this.y + 68,
                () -> client.setScreen(new SkillRestrictionScreen(this.levelManager, LevelManager.CRAFTING_RESTRICTIONS, Text.translatable("restriction.skillz.crafting"), 0)),
                new Color(127, 255, 127), !LevelManager.CRAFTING_RESTRICTIONS.isEmpty()));
        bookWidgets.add(new BookWidget(Text.translatable("restriction.skillz.mining"), this.x + 178, this.y + 68,
                () -> client.setScreen(new SkillRestrictionScreen(this.levelManager, LevelManager.MINING_RESTRICTIONS, Text.translatable("restriction.skillz.mining"), 1)),
                new Color(255, 255, 150), !LevelManager.MINING_RESTRICTIONS.isEmpty()));

        bookWidgets.add(new BookWidget(Text.translatable("restriction.skillz.item_usage"), this.x + 142, this.y + 68,
                () -> client.setScreen(new SkillRestrictionScreen(this.levelManager, LevelManager.ITEM_RESTRICTIONS, Text.translatable("restriction.skillz.item_usage"), 0)),
                new Color(255, 121, 79), !LevelManager.ITEM_RESTRICTIONS.isEmpty()));

        bookWidgets.add(new BookWidget(Text.translatable("restriction.skillz.block_usage"), this.x + 124, this.y + 68,
                () -> client.setScreen(new SkillRestrictionScreen(this.levelManager, LevelManager.BLOCK_RESTRICTIONS, Text.translatable("restriction.skillz.block_usage"), 1)),
                new Color(123, 175, 255), !LevelManager.BLOCK_RESTRICTIONS.isEmpty()));

        bookWidgets.add(new BookWidget(Text.translatable("restriction.skillz.enchantments"), this.x + 106, this.y + 68,
                () -> client.setScreen(new SkillRestrictionScreen(this.levelManager, LevelManager.ENCHANTMENT_RESTRICTIONS, Text.translatable("restriction.skillz.enchantments"), 3)),
                new Color(178, 127, 255), !LevelManager.ENCHANTMENT_RESTRICTIONS.isEmpty()));

        bookWidgets.add(new BookWidget(Text.translatable("restriction.skillz.entity_usage"), this.x + 88, this.y + 68,
                () -> client.setScreen(new SkillRestrictionScreen(this.levelManager, LevelManager.ENTITY_RESTRICTIONS, Text.translatable("restriction.skillz.entity_usage"), 2)),
                new Color(242, 127, 255), !LevelManager.ENTITY_RESTRICTIONS.isEmpty()));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        /*{
            int i = 0;
            for (Skill skill : LevelManager.SKILLS.values()) {
                //if (i < 12) {
                    int skillId = i + this.skillRow * 2;
                    if (LevelManager.SKILLS.size() <= skillId) {
                        break;
                    }
                    if (this.levelManager.getPlayerSkills().size() <= skillId) {
                        break;
                    }
                    context.drawTexture(BACKGROUND_TEXTURE, this.x + (i % 2 == 0 ? 8 : 96), this.y + 87 + i / 2 * 20, 0, 215, 88, 20);
                    context.drawTexture(SkillZMain.identifierOf("textures/gui/sprites/" + skill.id() + ".png"), this.x + (i % 2 == 0 ? 11 : 99), this.y + 89 + i / 2 * 20, 0, 0, 16, 16, 16, 16);

                    Text skillLevel = Text.translatable("text.skillz.gui.current_level", this.levelManager.getSkillLevel(skill.id()), LevelManager.SKILLS.get(skill.id()).maxLevel());
                    context.drawText(this.textRenderer, skillLevel, this.x + (i % 2 == 0 ? 53 : 141) - this.textRenderer.getWidth(skillLevel) / 2, this.y + 94 + i / 2 * 20, 0x3F3F3F, false);

                    if (DrawUtil.isPointWithinBounds(this.x + (i % 2 == 0 ? 11 : 99), this.y + 89 + i / 2 * 20, 16, 16, mouseX, mouseY)) {
                        context.drawTooltip(this.textRenderer, skill.getText(), mouseX, mouseY);
                    }
                    i++;
                //}
            }
        }*/
        if (this.levelManager.getPlayerSkills().size() > 12) {
            int scrollLevels = (this.levelManager.getPlayerSkills().size() - 12) / 2;
            if (this.levelManager.getPlayerSkills().size() % 2 != 0) {
                scrollLevels += 1;
            }

            int sliderY = this.skillRow * 86 / scrollLevels;
            context.drawTexture(BACKGROUND_TEXTURE, this.x + 186, this.y + 87 + sliderY, 200, 0, 6, 34);
        } else {
            context.drawTexture(BACKGROUND_TEXTURE, this.x + 186, this.y + 87, 206, 0, 6, 34);
        }

        {
            int i = 0;
            for (WidgetButtonPage page : newLeveButtons) {
                if (page.chopped && (i < 12)) {

                    page.hovered = mouseX >= (this.x + (i % 2 == 0 ? 80 : 169)) && mouseY >= (this.y + 91 + i / 2 * 20) && mouseX < (this.x + (i % 2 == 0 ? 80 : 169)) + page.getWidth() && mouseY < (this.y + 91 + i / 2 * 20) + page.getHeight();

                    context.drawTexture(BACKGROUND_TEXTURE, this.x + (i % 2 == 0 ? 8 : 96), this.y + 87 + i / 2 * 20, 0, 215, 88, 20);
                    context.drawTexture(SkillZMain.identifierOf("textures/gui/sprites/" + page.skill.id() + ".png"), this.x + (i % 2 == 0 ? 11 : 99), this.y + 89 + i / 2 * 20, 0, 0, 16, 16, 16, 16);

                    Text skillLevel = Text.translatable("text.skillz.gui.current_level", this.levelManager.getSkillLevel(page.skill.id()), LevelManager.SKILLS.get(page.skill.id()).maxLevel());
                    context.drawText(this.textRenderer, skillLevel, this.x + (i % 2 == 0 ? 53 : 141) - this.textRenderer.getWidth(skillLevel) / 2, this.y + 94 + i / 2 * 20, 0x3F3F3F, false);

                    //page.renderButton(context, mouseX, mouseX, delta);
                    MinecraftClient minecraftClient = MinecraftClient.getInstance();
                    context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.enableBlend();
                    RenderSystem.enableDepthTest();
                    int xx = page.hoverOutline ? page.getTextureY() : 0;
                    context.drawTexture(ICON_TEXTURE, this.x + (i % 2 == 0 ? 80 : 169), this.y + 91 + i / 2 * 20, page.textureX + xx * page.getWidth(), page.textureY, page.getWidth(), page.getHeight());
                    context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    if (page.isHovered()) {
                        context.drawTooltip(minecraftClient.textRenderer, page.tooltip, mouseX, mouseY);
                    }

                    if (DrawUtil.isPointWithinBounds(this.x + (i % 2 == 0 ? 11 : 99), this.y + 89 + i / 2 * 20, 16, 16, mouseX, mouseY)) {
                        context.drawTooltip(this.textRenderer, page.skill.getText(), mouseX, mouseY);
                    }
                    i++;
                }
            }
        }

        DrawTabHelper.drawTab(client, context, this, this.x, this.y, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);

        if (this.client != null && this.client.player != null) {
            Text title = Text.translatable("text.skillz.gui.title", this.client.player.getName().getString());
            context.drawText(this.textRenderer, title, this.x + 118 - this.textRenderer.getWidth(title) / 2, this.y + 7, 0x3F3F3F, false);

            if (!this.attributes.isEmpty()) {
                if (this.showAttributes) {
                    context.drawTexture(ICON_TEXTURE, this.x + 178, this.y + 5, 30, 114, 15, 13);
                    context.drawTexture(ATTRIBUTE_BACKGROUND_TEXTURE, this.x + 202, this.y, 0, 0, 82, 215);
                    int maxAttributes = Math.min(this.attributes.size(), 15);
                    if (this.attributes.size() > 15) {
                        int scrollLevels = this.attributes.size() - 15;
                        int sliderY = this.attributeRow * 158 / scrollLevels;
                        context.drawTexture(ATTRIBUTE_BACKGROUND_TEXTURE, this.x + 270, this.y + 8 + sliderY, 82, 0, 6, 41);
                    } else {
                        context.drawTexture(ATTRIBUTE_BACKGROUND_TEXTURE, this.x + 270, this.y + 8, 88, 0, 6, 41);
                    }
                    context.drawText(this.textRenderer, Text.translatable("text.skillz.gui.attributes"), this.x + 214, this.y + 12, 0xE0E0E0, false);

                    int k = 27;
                    for (int i = this.attributeRow; i < this.attributeRow + maxAttributes; i++) {
                        String attributeKey = SkillZMain.getEntityAttributeIdAsString(this.attributes.get(i).getAttribute());
                        context.drawTexture(Identifier.of(attributeKey.split(":")[0],"textures/gui/sprites/attribute/" + attributeKey.split(":")[1] + ".png"), this.x + 214, this.y + k, 0, 0, 9, 9, 9, 9);
                        float attributeValue = (float) Math.round(this.client.player.getAttributeInstance(this.attributes.get(i).getAttribute().value()).getValue() * 100.0D) / 100.0F;
                        context.drawText(this.textRenderer, Text.of(String.valueOf(attributeValue)), this.x + 214 + 15, this.y + k, 0xE0E0E0, false);

                        k += 12;
                    }
                }
            }

            // Level label
            Text skillLevelText = Text.translatable("text.skillz.gui.level", this.levelManager.getOverallLevel());
            context.drawText(this.textRenderer, skillLevelText, this.x + 62, this.y + 42, 0x3F3F3F, false);
            // Point label
            Text skillPointText = Text.translatable("text.skillz.gui.points", this.levelManager.getSkillPoints());
            context.drawText(this.textRenderer, skillPointText, this.x + 62, this.y + 54, 0x3F3F3F, false);

            // Experience bar
            context.drawTexture(ICON_TEXTURE, this.x + 62, this.y + 21, 0, 100, 131, 5);

            int nextLevelExperience = this.levelManager.getNextLevelExperience();
            float levelProgress = this.levelManager.getLevelProgress();
            long experience = (int) (nextLevelExperience * levelProgress);

            context.drawTexture(ICON_TEXTURE, this.x + 62, this.y + 21, 0, 105, (int) (130.0f * levelProgress), 5);
            // current xp label
            Text currentXpText = Text.translatable("text.skillz.gui.current_xp", experience, nextLevelExperience);
            context.drawText(this.textRenderer, currentXpText, this.x - this.textRenderer.getWidth(currentXpText) / 2 + 127, this.y + 30, 0x3F3F3F, false);

            //RED
            //new Color(255, 100, 50)
            //GREEN
            //new Color(127, 255, 127)
            //YELLOW
            //new Color(246, 255, 127)
            //BROWN
            //new Color(255, 206, 127)
            for (BookWidget book : this.bookWidgets) {
                book.draw(this.textRenderer, context, mouseX, mouseY);
            }
        }


        if (this.clientPlayerEntity != null) {
            //InventoryScreen.drawEntity(context, this.x + 33, this.y + 43, 30, this.quaternionf, null, this.clientPlayerEntity);
            InventoryScreen.drawEntity(context, this.x + 33, this.y + 70, 30, (float)(this.x + 33) - mouseX, (float)(this.y + 70 - 50) - mouseY, this.clientPlayerEntity);

            /*if (isPointWithinBounds(this.x + 9, this.y + 67, 15, 10, mouseX, mouseY)) {
                context.drawTexture(ICON_TEXTURE, this.x + 9, this.y + 67, 0, 138, 15, 10);
            } else {
                context.drawTexture(ICON_TEXTURE, this.x + 9, this.y + 67, 0, 128, 15, 10);
            }
            if (isPointWithinBounds(this.x + 41, this.y + 67, 15, 10, mouseX, mouseY)) {
                context.drawTexture(ICON_TEXTURE, this.x + 41, this.y + 67, 15, 138, 15, 10);
            } else {
                context.drawTexture(ICON_TEXTURE, this.x + 41, this.y + 67, 15, 128, 15, 10);
            }*/
        }
    }

    @Override
    public void tick() {
        super.tick();
        /*if (this.clientPlayerEntity != null && this.turnClientPlayer) {
            double mouseX = this.client.mouse.getX() * (double) this.client.getWindow().getScaledWidth() / (double) this.client.getWindow().getWidth();
            double mouseY = this.client.mouse.getY() * (double) this.client.getWindow().getScaledHeight() / (double) this.client.getWindow().getHeight();
            if (isPointWithinBounds(this.x + 9, this.y + 67, 15, 10, mouseX, mouseY)) {
                this.quaternionf.rotateLocalY(0.087f);
            } else if (isPointWithinBounds(this.x + 41, this.y + 67, 15, 10, mouseX, mouseY)) {
                this.quaternionf.rotateLocalY(-0.087f);
            } else {
                this.turnClientPlayer = false;
            }
        }*/
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyInit.screenKey.matchesKey(keyCode, scanCode) || Objects.requireNonNull(client).options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        /*if (this.turnClientPlayer) {
            this.turnClientPlayer = false;
        }*/
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DrawTabHelper.onTabButtonClick(client, this, this.x, this.y, mouseX, mouseY, this.getFocused() != null);

        for (BookWidget book : this.bookWidgets) {
            if (book.context && book.isHovered((int)mouseX,(int) mouseY)) {
                book.click(this.client, this.levelManager, mouseX, mouseY);
                return true;
            }
        }
        /*if (this.clientPlayerEntity != null) {
            if (isPointWithinBounds(this.x + 9, this.y + 67, 15, 10, mouseX, mouseY)) {
                this.turnClientPlayer = true;
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else if (isPointWithinBounds(this.x + 41, this.y + 67, 15, 10, mouseX, mouseY)) {
                this.turnClientPlayer = true;
                this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }*/

        /*int i = 0;
        for (Skill skill : LevelManager.SKILLS.values()) {
            if (i < 12) {
                int skillId = i + this.skillRow * 2;
                if (LevelManager.SKILLS.size() <= skillId) {
                    break;
                }
                if (this.levelManager.getPlayerSkills().size() <= skillId) {
                    break;
                }
                if (DrawUtil.isPointWithinBounds(this.x + (i % 2 == 0 ? 11 : 99), this.y + 89 + i / 2 * 20, 16, 16, mouseX, mouseY)) {
                    this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    this.client.setScreen(new SkillInfoScreen(this.levelManager, skill.id()));
                    return true;
                }
                i++;
            }
        }*/

        {
            int i = 0;
            for (WidgetButtonPage page : newLeveButtons) {
                if (page.chopped && (i < 12)) {
                    if (DrawUtil.isPointWithinBounds(this.x + (i % 2 == 0 ? 11 : 99), this.y + 89 + i / 2 * 20, 16, 16, mouseX, mouseY)) {
                        this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.client.setScreen(new SkillInfoScreen(this.levelManager, page.skill.id()));
                        return true;
                    }

                    if (DrawUtil.isPointWithinBounds(this.x + (i % 2 == 0 ? 80 : 169), this.y + 91 + i / 2 * 20, 13, 13, mouseX, mouseY)) {
                        this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        page.onClick(mouseX, mouseY);
                        return true;
                    }
                    i++;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (this.showAttributes && this.attributes.size() > 15 && DrawUtil.isPointWithinBounds(this.x + 209, this.y + 7, 68, 201, mouseX, mouseY)) {
            int maxAttributeRow = this.attributes.size() - 15;
            int newAttributeRow = this.attributeRow;
            newAttributeRow = newAttributeRow - (int) (verticalAmount);
            if (newAttributeRow < 0) {
                this.attributeRow = 0;
            } else {
                this.attributeRow = Math.min(newAttributeRow, maxAttributeRow);
            }
        }
        if (DrawUtil.isPointWithinBounds(this.x + 7, this.y + 86, 186, 122, mouseX, mouseY)) {
            //System.out.println(this.newLeveButtons.size());
            //this.newLeveButtons.get(this.newLeveButtons.size()-1).visible = false;
            //this.newLeveButtons.get(0).visible = false;

            if (verticalAmount < 0) {
                int i = 0;
                int x = 0;
                for (WidgetButtonPage page : this.newLeveButtons) {
                    if (this.newLeveButtons.size() > 12) {
                        int others = this.newLeveButtons.size() - 12;
                        if (x < others) {
                            //System.out.println(i + " " + x + " " + others);
                            if (!page.chopped) {
                                x++;
                            }
                            /*if (i > 11) {
                                this.newLeveButtons.get(i+x).chopped = true;
                            }*/
                            if (page.chopped) {
                                if (i < 2 + x) {
                                    this.newLeveButtons.get(i).chopped = false;
                                }
                            }
                            i++;
                        }
                    }
                }
            }else {
                int i = 0;
                int x = 0;
                //System.out.println(this.newLeveButtons.size());
                for (WidgetButtonPage page : this.newLeveButtons) {
                    if (this.newLeveButtons.size() > 12) {
                        int others = this.newLeveButtons.size() - 12;
                        //if (x < others) {
                            //System.out.println(i + " " + x + " " + others);
                            /*if (!page.chopped) {
                                x++;
                            }*/
                            /*if (i > 11) {
                                this.newLeveButtons.get(i).chopped = false;
                            }*/
                            if (!page.chopped) {
                                if (i < 2) {
                                    this.newLeveButtons.get(i).chopped = true;
                                }
                            }
                            i++;
                        //}
                    }
                }
            }

            int maxSkillRow = (this.levelManager.getPlayerSkills().size() - 12) / 2;
            if (this.levelManager.getPlayerSkills().size() % 2 != 0) {
                maxSkillRow += 1;
            }
            int oldSkillRow = this.skillRow;
            int newSkillRow = this.skillRow;
            newSkillRow = newSkillRow - (int) (verticalAmount);
            if (newSkillRow < 0) {
                this.skillRow = 0;
            } else {
                this.skillRow = Math.min(newSkillRow, maxSkillRow);
            }
            if (oldSkillRow != this.skillRow) {
                updateLevelButtons();
            }
        }
        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void updateLevelButtons() {
        int i = 0;
        for (WidgetButtonPage page : newLeveButtons) {
            String skrillix = page.skill.id();
            if (ConfigInit.MAIN.LEVEL.overallMaxLevel > 0 && this.levelManager.getOverallLevel() >= ConfigInit.MAIN.LEVEL.overallMaxLevel) {
                page.active = false;
            } else if (LevelManager.SKILLS.get(skrillix).maxLevel() <= this.levelManager.getPlayerSkills().get(skrillix).getLevel()) {
                page.active = false;
            } else {
                page.active = this.levelManager.getSkillPoints() > 0;
            }
            if (ConfigInit.MAIN.LEVEL.allowHigherSkillLevel && this.levelManager.getSkillPoints() > 0) {
                boolean maxedAllSkills = true;
                for (Skill skillCheck : LevelManager.SKILLS.values()) {
                    if (skillCheck.maxLevel() > this.levelManager.getSkillLevel(skillCheck.id())) {
                        maxedAllSkills = false;
                        break;
                    }
                }
                if (maxedAllSkills) {
                    page.active = true;
                }
            }
            i++;
        }
    }

    private static class WidgetButtonPage extends ButtonWidget {

        private final Skill skill;
        private final boolean hoverOutline;
        private final boolean clickable;
        private final int textureX;
        private final int textureY;
        private List<Text> tooltip = new ArrayList<Text>();
        private int clickedKey = -1;
        public boolean chopped = true;
        public boolean hovered = false;

        public WidgetButtonPage(Skill skill, int x, int y, int sizeX, int sizeY, int textureX, int textureY, boolean hoverOutline, boolean clickable, @Nullable Text tooltip, ButtonWidget.PressAction onPress) {
            super(x, y, sizeX, sizeY, ScreenTexts.EMPTY, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.skill = skill;
            this.hoverOutline = hoverOutline;
            this.clickable = clickable;
            this.textureX = textureX;
            this.textureY = textureY;
            this.width = sizeX;
            this.height = sizeY;
            if (tooltip != null) {
                this.tooltip.add(tooltip);
            }
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            context.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            int i = hoverOutline ? this.getTextureY() : 0;
            context.drawTexture(ICON_TEXTURE, this.getX(), this.getY(), this.textureX + i * this.width, this.textureY, this.width, this.height);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (this.isHovered()) {
                context.drawTooltip(minecraftClient.textRenderer, this.tooltip, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.clickedKey = button;
            if (!this.clickable) {
                return false;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return super.isValidClickButton(button) || button == 1 || button == 2;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!this.clickable) {
                return false;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        public void addTooltip(Text text) {
            this.tooltip.add(text);
        }

        public boolean wasMiddleButtonClicked() {
            return clickedKey == 2;
        }

        public boolean wasRightButtonClicked() {
            return clickedKey == 1;
        }

        private int getTextureY() {
            int i = 1;
            if (!this.active) {
                i = 0;
            } else if (this.isHovered()) {
                i = 2;
            }
            return i;
        }

        public boolean isHovered() {
            return hovered;
        }
    }
}
