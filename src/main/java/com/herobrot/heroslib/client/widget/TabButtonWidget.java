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

    // Sprites nativos de las pestañas del creativo en Vanilla 1.21.1
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
        // 1. Dibujar el fondo de la pestaña (Usa el sprite nativo dependiendo de si está seleccionada o no)
        ResourceLocation bgSprite = this.isSelected ? TAB_SELECTED : TAB_UNSELECTED;
        graphics.blitSprite(bgSprite, this.getX(), this.getY(), this.width, this.height);

        // 2. Dibujar el ItemStack (Ícono del mod)
        // Offset de +6 y +9 para centrar el ítem de 16x16 en el widget de 28x32
        graphics.renderFakeItem(this.icon, this.getX() + 6, this.getY() + 9);

        // 3. Dibujar el Tooltip (Al pasar el ratón)
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