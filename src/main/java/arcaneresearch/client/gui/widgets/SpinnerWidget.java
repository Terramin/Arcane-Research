package arcaneresearch.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.util.ARGB; 
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.NodeSetting;

public class SpinnerWidget extends AbstractWidget {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final NodeSetting setting;
    private final OnValueChange onValueChange;

    public SpinnerWidget(int x, int y, int width, NodeSetting setting, OnValueChange onValueChange) {
        super(x, y, width, 10, Component.literal(setting.getLocalizedName()));
        this.setting = setting;
        this.onValueChange = onValueChange;
    }

    public NodeSetting getSetting() {
        return setting;
    }

    
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        boolean hovered = mouseX >= getX() && mouseX < getX() + width + 10
                && mouseY >= getY() && mouseY < getY() + height;
        float brightness = hovered ? 1.0f : 0.9f;

        
        int tint = ARGB.colorFromFloat(brightness, brightness, brightness, brightness);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY(), 20, 0, 10, 10, 256, 256, tint);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() + width, getY(), 30, 0, 10, 10, 256, 256, tint);

        String valueText = setting.getValueText();
        int textX = getX() + (width + 10) / 2 - font.width(valueText) / 2;
        graphics.text(font, valueText, textX, getY() + 1, 0xFFFFFFFF, true);
    }

    
    
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && this.active
                && mouseX >= getX() && mouseX < getX() + width + 10
                && mouseY >= getY() && mouseY < getY() + height;
    }

    
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (!visible || !active) return false;

        if (mouseX >= getX() && mouseX < getX() + 10 &&
                mouseY >= getY() && mouseY < getY() + height) {
            setting.decrement();
            if (onValueChange != null) {
                onValueChange.onValueChange(setting, setting.getValue());
            }
            return true;
        }

        if (mouseX >= getX() + width && mouseX < getX() + width + 10 &&
                mouseY >= getY() && mouseY < getY() + height) {
            setting.increment();
            if (onValueChange != null) {
                onValueChange.onValueChange(setting, setting.getValue());
            }
            return true;
        }

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @FunctionalInterface
    public interface OnValueChange {
        void onValueChange(NodeSetting setting, int newValue);
    }
}