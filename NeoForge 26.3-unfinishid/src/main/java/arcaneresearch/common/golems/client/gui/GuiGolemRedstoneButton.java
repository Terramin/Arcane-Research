package arcaneresearch.common.golems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;

import java.util.function.BooleanSupplier;

public class GuiGolemRedstoneButton extends Button {
    static final Identifier TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final BooleanSupplier sensitive;

    public GuiGolemRedstoneButton(int x, int y, BooleanSupplier sensitive, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.sensitive = sensitive;
    }

    // NEOFORGE_PORT (26.3 render-state rework): renderWidget(GuiGraphics) removed ->
    // extractContents(GuiGraphicsExtractor); old setShaderColor tint -> per-blit color arg.
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int tint = isHovered() ? 0xFFFFFFFF : 0xE6FFFFFF;
        boolean isSensitive = sensitive.getAsBoolean();
        if (isSensitive) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 64, 136, 16, 16, 256, 256, tint);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 80, 136, 16, 16, 256, 256, tint);
        }
        if (isHovered()) {
            String s = Component.translatable(isSensitive ? "golem.prop.redon" : "golem.prop.redoff").getString();
            graphics.text(Minecraft.getInstance().font, s,
                    getX() - 2 - Minecraft.getInstance().font.width(s), getY() + 4, 0xFFFFFFFF, false);
        }
    }
}