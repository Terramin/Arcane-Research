package arcaneresearch.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.util.ARGB; 
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import arcaneresearch.Arcaneresearch;

import java.util.function.Consumer;

public class ScrollButton extends AbstractWidget {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final boolean minus;
    private final boolean vertical;
    private final Consumer<ScrollButton> onPress;

    public ScrollButton(int x, int y, boolean minus, Consumer<ScrollButton> onPress) {
        this(x, y, minus, false, onPress);
    }

    public ScrollButton(int x, int y, boolean minus, boolean vertical, Consumer<ScrollButton> onPress) {
        super(x, y, 10, 10, Component.empty());
        this.minus = minus;
        this.vertical = vertical;
        this.onPress = onPress;
    }

    public boolean isMinus() {
        return minus;
    }

    public boolean isVertical() {
        return vertical;
    }

    
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isMouseOver(mouseX, mouseY);

        
        int tint = hovered ? ARGB.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f)
                : ARGB.colorFromFloat(0.9f, 0.9f, 0.9f, 0.9f);

        int texX = vertical ? 67 : (minus ? 20 : 30);
        int texY = vertical ? (minus ? 0 : 10) : 0;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), texX, texY, 10, 10, 256, 256, tint);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {

        return mouseX >= getX() && mouseX < getX() + width
                && mouseY >= getY() && mouseY < getY() + height;
    }

    @Override
    

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