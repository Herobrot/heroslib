package com.herobrot.heroslib.client.widget;

import com.herobrot.heroslib.client.tab.TabDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TabButtonWidget extends AbstractButton {
    private static final ResourceLocation ICONS_TEXTURE = ResourceLocation.fromNamespaceAndPath("heroslib", "textures/gui/icons.png");

    private final ResourceLocation icon;
    private final boolean isSelected;
    private final boolean isFirstTab;
    private final Runnable onPressAction;

    public TabButtonWidget(int x, int y, boolean isFirstTab, boolean isSelected, TabDefinition tab, Runnable onPressAction) {
        super(x, y, 28, 32, tab.tooltip());
        this.icon = tab.icon();
        this.isSelected = isSelected;
        this.isFirstTab = isFirstTab;
        this.onPressAction = onPressAction;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int uOffset = this.isFirstTab ? 0 : 28;
        int vOffset = this.isSelected ? 0 : 32;

        graphics.blit(ICONS_TEXTURE, this.getX(), this.getY(), uOffset, vOffset, this.width, this.height);
        graphics.blit(this.icon, this.getX() + 6, this.getY() + 8, 0, 0, 16, 16, 16, 16);

        if (this.isHovered()) {
            graphics.renderTooltip(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        if (!this.isSelected) {
            this.onPressAction.run();
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {}
}