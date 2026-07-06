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
        // El fondo de las pestañas NO seleccionadas ya se dibujó en ScreenEvent.Render.Pre,
        // ANTES del panel, para que su borde quede "detrás" del marco del inventario.
        if (this.isSelected) {
            renderTabBackground(graphics); // Se dibuja aquí (fase normal) para quedar POR ENCIMA del panel
        }

        graphics.renderFakeItem(this.icon, this.getX() + 6, this.getY() + 9);

        if (this.isHovered()) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 400);
            graphics.renderTooltip(Minecraft.getInstance().font, this.getMessage(), mouseX, mouseY);
            graphics.pose().popPose();
        }
    }

    public void renderTabBackground(GuiGraphics graphics) {
        ResourceLocation bgSprite = this.isSelected ? TAB_SELECTED : TAB_UNSELECTED;
        graphics.blitSprite(bgSprite, this.getX(), this.getY(), this.width, this.height);
    }

    // Usado por TabInjectionHandler en la fase Pre, cuando aún no existe una instancia del widget en ese frame
    public static void drawBackgroundStatic(GuiGraphics graphics, int x, int y, boolean selected) {
        ResourceLocation bg = selected ? TAB_SELECTED : TAB_UNSELECTED;
        graphics.blitSprite(bg, x, y, 28, 32);
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