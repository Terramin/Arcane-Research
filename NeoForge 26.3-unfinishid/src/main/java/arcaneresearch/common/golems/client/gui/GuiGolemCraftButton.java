package arcaneresearch.common.golems.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;

public class GuiGolemCraftButton extends Button {
    static final Identifier TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_golembuilder.png");

    public GuiGolemCraftButton(int x, int y, OnPress onPress) {
        super(x, y, 24, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }

    // NEOFORGE_PORT (26.3 render-state rework): renderWidget(GuiGraphics) removed ->
    // extractContents(GuiGraphicsExtractor); old setShaderColor tint -> per-blit color arg.
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int tint = (active && isHovered()) ? 0xFFFFFFFF : 0xE6FFFFFF;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 216, 64, 24, 16, 256, 256, tint);
        if (!active) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 216, 40, 24, 16, 256, 256, 0xFFFFFFFF);
        }
    }
}