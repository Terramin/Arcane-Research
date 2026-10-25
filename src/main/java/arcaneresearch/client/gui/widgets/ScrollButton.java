package arcaneresearch.client.gui.widgets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import java.util.function.Consumer;
@OnlyIn(Dist.CLIENT)
public class ScrollButton extends AbstractWidget {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_base.png");
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
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (hovered) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 0.9f);
        }
        int texX = vertical ? 67 : (minus ? 20 : 30);
        int texY = vertical ? (minus ? 0 : 10) : 0;
        graphics.blit(TEXTURE, getX(), getY(), texX, texY, 10, 10);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX < getX() + width
                && mouseY >= getY() && mouseY < getY() + height;
    }
    @Override
    public void onClick(double mouseX, double mouseY) {
        if (onPress != null) {
            onPress.accept(this);
        }
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
