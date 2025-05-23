package net.skillz.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.skillz.SkillZMain;
import net.skillz.init.ConfigInit;
import net.skillz.init.KeyInit;
import net.skillz.level.LevelManager;
import net.skillz.level.Skill;
import net.skillz.level.SkillBonus;
import net.skillz.level.restriction.PlayerRestriction;
import net.skillz.screen.widget.LineWidget;
import net.skillz.util.DrawUtil;
import dev.sygii.tabapi.api.Tab;
import dev.sygii.tabapi.util.DrawTabHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Environment(EnvType.CLIENT)
public class SkillInfoScreen extends Screen implements Tab {

    public static final Identifier BACKGROUND_TEXTURE = SkillZMain.identifierOf("textures/gui/skill_info_background.png");

    private final int backgroundWidth = 200;
    private final int backgroundHeight = 215;
    private int x;
    private int y;

    private final List<LineWidget> lines = new ArrayList<>();

    private final Skill skill;
    private final LevelManager levelManager;

    private int lineIndex = 0;

    public SkillInfoScreen(LevelManager levelManager, String skillId) {
        super(LevelManager.SKILLS.get(skillId).getText());
        this.skill = LevelManager.SKILLS.get(skillId);
        this.levelManager = levelManager;
    }

    @Override
    protected void init() {
        super.init();

        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        for (int i = 0; i < 50; i++) {
            String skillExtra = "skill.skillz." + this.skill.id() + "." + i;
            Text skillExtraText = Text.translatable(skillExtra);

            if (skillExtraText.getString().equals(skillExtra)) {
                break;
            }
            this.lines.add(new LineWidget(this.client, skillExtraText, null, 0));
        }
        if (!this.lines.isEmpty()) {
            //this.lines.addFirst(new LineWidget(this.client, Text.translatable("skill.skillz.info"), null, 0));
            this.lines.add(0, new LineWidget(this.client, Text.translatable("skill.skillz.info"), null, 0));
        }
        int skillInfoLines = this.lines.size();
        for (String bonusKey : SkillBonus.BONUS_KEYS) {
            if (LevelManager.BONUSES.containsKey(bonusKey)) {
                SkillBonus bonus = LevelManager.BONUSES.get(bonusKey);
                if (bonus.getId().equals(this.skill.id())) {
                    for (int i = 0; i < 50; i++) {
                        String bonusInfo = "bonus.skillz." + bonus.getKey() + "." + i;
                        Text bonusInfoText = Text.translatable(bonusInfo, Text.translatable("text.skillz.gui.short_lower_level", bonus.getLevel()));

                        if (bonusInfoText.getString().equals(bonusInfo)) {
                            break;
                        }
                        if (bonusInfoText.getString().startsWith("---")) {
                            break;
                        }
                        this.lines.add(new LineWidget(this.client, bonusInfoText, null, 0));
                    }
                }
            }
        }
        if (this.lines.size() > skillInfoLines) {
            this.lines.add(skillInfoLines, new LineWidget(this.client, Text.translatable("bonus.skillz.info"), null, 0));
        }

        addRestrictionLines(LevelManager.ITEM_RESTRICTIONS, Text.translatable("restriction.skillz.item_usage"), 0);
        addRestrictionLines(LevelManager.BLOCK_RESTRICTIONS, Text.translatable("restriction.skillz.block_usage"), 1);
        addRestrictionLines(LevelManager.ENTITY_RESTRICTIONS, Text.translatable("restriction.skillz.entity_usage"), 2);
        addRestrictionLines(LevelManager.ENCHANTMENT_RESTRICTIONS, Text.translatable("restriction.skillz.enchantments"), 3);
    }

    private void addRestrictionLines(Map<Integer, PlayerRestriction> levelRestrictions, Text restrictionText, int code) {
        // Lvl, Id (ex. Item), PlayerRestriction
        Map<Integer, Map<Integer, PlayerRestriction>> map = new TreeMap<>();

        // Id (ex. Item), PlayerRestriction
        for (Map.Entry<Integer, PlayerRestriction> itemRestriction : levelRestrictions.entrySet()) {
            // SkillId, Lvl
            for (Map.Entry<String, Integer> specificRestriction : itemRestriction.getValue().getSkillLevelRestrictions().entrySet()) {
                if (specificRestriction.getKey().equals(this.skill.id())) {
                    if (map.containsKey(specificRestriction.getValue())) {
                        map.get(specificRestriction.getValue()).put(itemRestriction.getKey(), itemRestriction.getValue());
                    } else {
                        Map<Integer, PlayerRestriction> newMap = new TreeMap<>();
                        newMap.put(itemRestriction.getKey(), itemRestriction.getValue());
                        map.put(specificRestriction.getValue(), newMap);
                    }
                    break;
                }

            }
        }

        if (!map.isEmpty()) {
            this.lines.add(new LineWidget(this.client, restrictionText, null, 0));
        }
        for (Map.Entry<Integer, Map<Integer, PlayerRestriction>> restrictions : map.entrySet()) {
            this.lines.add(new LineWidget(this.client, Text.translatable("text.skillz.gui.short_level", restrictions.getKey()), null, code));

            if (restrictions.getValue().size() > 9) {
                Map<Integer, PlayerRestriction> newMap = new TreeMap<>();

                int count = 0;
                for (Map.Entry<Integer, PlayerRestriction> specificRestriction : restrictions.getValue().entrySet()) {
                    newMap.put(specificRestriction.getKey(), specificRestriction.getValue());
                    count++;
                    if (count == restrictions.getValue().size()) {
                        this.lines.add(new LineWidget(this.client, null, newMap, code));
                        break;
                    }
                    if (count % 9 == 0) {
                        this.lines.add(new LineWidget(this.client, null, new TreeMap<>(newMap), code));
                        newMap.clear();
                    }
                }
            } else {
                this.lines.add(new LineWidget(this.client, null, restrictions.getValue(), code));
            }
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        this.renderBackground(context);
        context.drawTexture(BACKGROUND_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);

        if (this.lines.size() > 10) {
            int scrollLevels = this.lines.size() - 10;
            int sliderY = this.lineIndex * 156 / scrollLevels;
            context.drawTexture(BACKGROUND_TEXTURE, this.x + 186, this.y + 20 + sliderY, 200, 0, 6, 31);
        } else {
            context.drawTexture(BACKGROUND_TEXTURE, this.x + 186, this.y + 20, 206, 0, 6, 31);
        }
        DrawTabHelper.drawTab(client, context, this, this.x, this.y, mouseX, mouseY);

        context.drawText(this.textRenderer, this.title, this.x + 7, this.y + 7, 0x3F3F3F, false);
        context.drawText(this.textRenderer, Text.translatable("text.skillz.gui.short_level", this.levelManager.getSkillLevel(this.skill.id())), this.x + 11 + this.textRenderer.getWidth(this.title), this.y + 7, 0x3F3F3F, false);

        for (int i = 0; i < 10; i++) {
            if (this.lines.size() <= i) {
                break;
            }
            int index = this.lineIndex + i;

            if (this.lines.get(index).code == 2) {
                this.lines.get(index).render(context, this.x + 12, this.y + 24 + i * 18, mouseX, mouseY);
            }else {
                this.lines.get(index).render(context, this.x + 12, this.y + 24 + i * 18, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            if (ConfigInit.CLIENT.switchScreen) {
                this.client.setScreen(new LevelScreen());
            } else {
                this.close();
            }
            return true;

        } else if (KeyInit.screenKey.matchesKey(keyCode, scanCode)) {
            this.client.setScreen(new LevelScreen());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DrawTabHelper.onTabButtonClick(client, this, this.x, this.y, mouseX, mouseY, false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
        if (this.lines.size() > 10 && DrawUtil.isPointWithinBounds(this.x + 7, this.y + 19, 186, 189, mouseX, mouseY)) {
            int maxRow = this.lines.size() - 10;
            int newRow = this.lineIndex;
            newRow = newRow - (int) (verticalAmount);
            if (newRow < 0) {
                this.lineIndex = 0;
            } else {
                this.lineIndex = Math.min(newRow, maxRow);
            }
        }

        return super.mouseScrolled(mouseX, mouseY, verticalAmount);
    }

}
