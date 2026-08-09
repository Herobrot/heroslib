package com.herobrot.heroslib.client.widget;

import com.herobrot.heroslib.client.tab.TabDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TabButtonWidget extends AbstractButton {
    private static final ResourceLocation TAB_SELECTED_1 = ResourceLocation.withDefaultNamespace
            ("container/creative_inventory/tab_top_selected_1");
    private static final ResourceLocation TAB_UNSELECTED_1 = ResourceLocation.withDefaultNamespace
            ("container/creative_inventory/tab_top_unselected_1");
    private static final ResourceLocation TAB_SELECTED_2 = ResourceLocation.withDefaultNamespace
            ("container/creative_inventory/tab_top_selected_2");
    private static final ResourceLocation TAB_UNSELECTED_2 = ResourceLocation.withDefaultNamespace
            ("container/creative_inventory/tab_top_unselected_2");

    private final TabDefinition tab;
    private final ItemStack icon;
    private final boolean isSelected;
    private final boolean isFirstTab;
    private final Runnable onPressAction;

    public TabButtonWidget(int x, int y, boolean isFirstTab, boolean isSelected, TabDefinition tab, Runnable onPressAction) {
        super(x, y, 28, 32, tab.tooltip());
        this.tab = tab;
        this.icon = tab.icon();
        this.isSelected = isSelected;
        this.isFirstTab = isFirstTab;
        this.onPressAction = onPressAction;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.isSelected) renderTabBackground(graphics);
        renderIcon(graphics);
        if (this.isHovered()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.renderTooltip(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
            graphics.pose().popPose();
        }
    }

    private void renderIcon(GuiGraphics graphics) {
        if (this.tab.iconTexture() != null)
            graphics.blit(this.tab.iconTexture(), this.getX() + 6, this.getY() + 9, 0, 0,
                    16, 16, 16, 16);
        else if (this.icon != null)
            graphics.renderFakeItem(this.icon, this.getX() + 6, this.getY() + 9);
    }

    public void renderTabBackground(GuiGraphics graphics) {
        ResourceLocation bgSprite;

        if (this.isSelected && this.tab.bgSelected() != null) {
            graphics.blit(this.tab.bgSelected(), this.getX(), this.getY(), 0, 0,
                    this.width, this.height, this.width, this.height);
            return;
        } else if (!this.isSelected && this.tab.bgUnselected() != null) {
            graphics.blit(this.tab.bgUnselected(), this.getX(), this.getY(), 0, 0,
                    this.width, this.height, this.width, this.height);
            return;
        }
        if (this.isSelected)
            bgSprite = this.isFirstTab ? TAB_SELECTED_1 : TAB_SELECTED_2;
        else
            bgSprite = this.isFirstTab ? TAB_UNSELECTED_1 : TAB_UNSELECTED_2;

        graphics.blitSprite(bgSprite, this.getX(), this.getY(), this.width, this.height);
    }

    public static void drawBackgroundStatic(GuiGraphics graphics, int x, int y, boolean isFirstTab, boolean selected) {
        ResourceLocation bg;
        if (selected)
            bg = isFirstTab ? TAB_SELECTED_1 : TAB_SELECTED_2;
        else
            bg = isFirstTab ? TAB_UNSELECTED_1 : TAB_UNSELECTED_2;
        graphics.blitSprite(bg, x, y, 28, 32);
    }

    @Override
    public void onPress() {
        if (!this.isSelected) this.onPressAction.run();
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {}

    public TabDefinition getTab() { return this.tab; }
}