package arcaneresearch.client.gui.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import arcaneresearch.Arcaneresearch;

public class SliderWidget extends AbstractWidget {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private final float min;
    private final boolean vertical;
    private final OnValueChange onValueChange;
    private float sliderPosition = 0.0f;
    private boolean dragging = false;
    private float max;

    public SliderWidget(int x, int y, int width, int height, float min, float max, float defaultValue,
                        boolean vertical, OnValueChange onValueChange) {
        super(x, y, width, height, Component.empty());
        this.min = min;
        this.max = max;
        this.vertical = vertical;
        this.onValueChange = onValueChange;

        if (max > min) {
            this.sliderPosition = (defaultValue - min) / (max - min);
        }
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
        this.sliderPosition = 0.0f;
    }

    public float getSliderValue() {
        return min + (max - min) * sliderPosition;
    }

    public void setSliderValue(float value, boolean notify) {
        if (max > min) {
            sliderPosition = Math.max(0.0f, Math.min(1.0f, (value - min) / (max - min)));
        }
        if (notify && onValueChange != null) {
            onValueChange.onValueChange(getSliderValue());
        }
    }

    public float getSliderPosition() {
        return sliderPosition;
    }

    public void setSliderPosition(float pos) {
        this.sliderPosition = pos;
    }

    // NEOFORGE_PORT: renderWidget -> extractWidgetRenderState.
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // NEOFORGE_PORT: white-only tints dropped; blit takes pipeline + sheet size (256).
        graphics.pose().pushMatrix();
        if (vertical) {
            graphics.pose().translate(getX() + 2, getY());
            graphics.pose().scale(1.0f, height / 32.0f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 240, 176, 4, 32, 256, 256);
        } else {
            graphics.pose().translate(getX(), getY() + 2);
            graphics.pose().scale(width / 32.0f, 1.0f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, 208, 176, 32, 4, 256, 256);
        }
        graphics.pose().popMatrix();

        if (vertical) {
            int handleY = (int) (sliderPosition * (height - 8));
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX(), getY() + handleY, 20, 20, 8, 8, 256, 256);
        } else {
            int handleX = (int) (sliderPosition * (width - 8));
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() + handleX, getY(), 20, 20, 8, 8, 256, 256);
        }
    }

    // NEOFORGE_PORT: 26.3 widget input via onClick/onDrag/onRelease (Forge
    // mouseClicked/mouseDragged/mouseReleased 1:1: valid-button + hit guards,
    // dragging reset on release).
    @Override
    public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        if (isValidClickButton(event.buttonInfo()) && this.visible && isMouseOver(event.x(), event.y())) {
            dragging = true;
            updateSliderPosition(event.x(), event.y());
        }
    }

    @Override
    public void onRelease(net.minecraft.client.input.MouseButtonEvent event) {
        if (dragging) {
            dragging = false;
        }
    }

    @Override
    protected void onDrag(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        if (dragging) {
            updateSliderPosition(event.x(), event.y());
        }
    }

    private void updateSliderPosition(double mouseX, double mouseY) {
        float oldValue = getSliderValue();

        if (vertical) {
            sliderPosition = (float) ((mouseY - (getY() + 4)) / (height - 8));
        } else {
            sliderPosition = (float) ((mouseX - (getX() + 4)) / (width - 8));
        }

        sliderPosition = Math.max(0.0f, Math.min(1.0f, sliderPosition));

        float newValue = getSliderValue();
        if (oldValue != newValue && onValueChange != null) {
            onValueChange.onValueChange(newValue);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }

    @FunctionalInterface
    public interface OnValueChange {
        void onValueChange(float value);
    }
}