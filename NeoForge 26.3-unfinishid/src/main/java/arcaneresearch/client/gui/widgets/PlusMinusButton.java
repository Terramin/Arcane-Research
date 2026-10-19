package arcaneresearch.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.util.ARGB; // NEOFORGE_PORT: tint via blit color
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import arcaneresearch.Arcaneresearch;

import java.util.function.Consumer;

public class PlusMinusButton extends AbstractWidget {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final boolean minus;
    private final Consumer<PlusMinusButton> onPress;

    public PlusMinusButton(int x, int y, boolean minus, Consumer<PlusMinusButton> onPress) {
        super(x, y, 10, 10, Component.empty());
        this.minus = minus;
        this.onPress = onPress;
    }

    public boolean isMinus() {
        return minus;
    }

    // NEOFORGE_PORT: renderWidget -> extractWidgetRenderState.
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isMouseOver(mouseX, mouseY);

        // NEOFORGE_PORT: tint baked into blit color (same values).
        int tint = hovered ? ARGB.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f)
                : ARGB.colorFromFloat(0.9f, 0.9f, 0.9f, 0.9f);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), minus ? 0 : 10, 0, 10, 10, 256, 256, tint);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {

        return mouseX >= getX() && mouseX < getX() + width
                && mouseY >= getY() && mouseY < getY() + height;
    }

    @Override
    // NEOFORGE_PORT: click via MouseButtonEvent.

    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (onPress != null) {
            onPress.accept(this);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}