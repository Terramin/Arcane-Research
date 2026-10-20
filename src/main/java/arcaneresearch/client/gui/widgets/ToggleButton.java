package arcaneresearch.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import arcaneresearch.Arcaneresearch;

import java.util.function.Consumer;

public class ToggleButton extends AbstractWidget {

    private static final Identifier DEFAULT_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_turret_advanced.png");

    private final Identifier texture;
    private final Runnable updater;
    private final Consumer<ToggleButton> onPress;
    private boolean toggled;

    public ToggleButton(int x, int y, String text, Runnable updater, Consumer<ToggleButton> onPress) {
        this(x, y, text, DEFAULT_TEXTURE, updater, onPress);
    }

    public ToggleButton(int x, int y, String text, Identifier texture,
                        Runnable updater, Consumer<ToggleButton> onPress) {
        super(x, y, 8, 8, text != null ? Component.translatable(text) : Component.empty());
        this.texture = texture;
        this.updater = updater;
        this.onPress = onPress;
    }

    public boolean isToggled() {
        return toggled;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    // NEOFORGE_PORT: renderWidget -> extractWidgetRenderState.
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (updater != null) {
            updater.run();
        }

        // NEOFORGE_PORT: white-only tints dropped.
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 192, 16, 8, 8, 256, 256);
        if (toggled) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), 192, 24, 8, 8, 256, 256);
        }

        Font font = Minecraft.getInstance().font;
        graphics.text(font, getMessage(), getX() + 12, getY(), 0xFFFFFFFF, false);
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