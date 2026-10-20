package arcaneresearch.common.golems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;

import java.util.function.BooleanSupplier;

public class GuiGolemPropButton extends Button {
    static final Identifier TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final BooleanSupplier value;

    public GuiGolemPropButton(int x, int y, int width, int height, Component label, BooleanSupplier value, OnPress onPress) {
        super(x, y, width, height, label, onPress, Button.DEFAULT_NARRATION);
        this.value = value;
    }

    // NEOFORGE_PORT (26.3 render-state rework): renderWidget(GuiGraphics) removed ->
    // extractContents(GuiGraphicsExtractor).
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX() - 2, getY() - 2, 2, 18, 12, 12, 256, 256, 0xFFFFFFFF);
        if (value.getAsBoolean()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX() - 2, getY() - 2, 18, 18, 12, 12, 256, 256, 0xFFFFFFFF);
        }
        graphics.text(Minecraft.getInstance().font, getMessage().getString(),
                getX() + 12, getY(), 0xFFFFFFFF, false);
    }
}