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
    private static final ResourceLocation TAB_SELECTED = ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1");
    private static final ResourceLocation TAB_UNSELECTED = ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1");

    private final ItemStack icon;
    private final boolean isSelected;
    private final Runnable onPressAction;

    public TabButtonWidget(int x, int y, boolean isFirstTab, boolean isSelected, TabDefinition tab, Runnable onPressAction) {
        super(x, y, 28, 32, tab.tooltip());
        this.icon = tab.icon();
        this.isSelected = isSelected;
        this.onPressAction = onPressAction;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Fondo de pestaña al ras del inventario
        ResourceLocation bgSprite = this.isSelected ? TAB_SELECTED : TAB_UNSELECTED;
        graphics.blitSprite(bgSprite, this.getX(), this.getY(), this.width, this.height);

        // 2. Ítem rendering sin anomalías de profundidad
        graphics.renderFakeItem(this.icon, this.getX() + 6, this.getY() + 9);

        // 3. Tooltip al frente de todos los elementos
        if (this.isHovered()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.renderTooltip(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
            graphics.pose().popPose();
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