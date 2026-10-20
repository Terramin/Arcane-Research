package arcaneresearch.common.golems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;

import java.util.function.BooleanSupplier;

public class GuiGolemBWListButton extends Button {
    static final Identifier TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final BooleanSupplier blacklist;

    public GuiGolemBWListButton(int x, int y, BooleanSupplier blacklist, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.blacklist = blacklist;
    }

    // NEOFORGE_PORT (26.3 render-state rework): renderWidget(GuiGraphics) removed ->
    // extractContents(GuiGraphicsExtractor); old setShaderColor tint -> per-blit color arg.
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // NEOFORGE_PORT (26.3): setShaderColor(c, c, c, c) scaled RGB as well as alpha -> 0xAARRGGBB = c in every channel.
        int tint = isHovered() ? 0xFFFFFFFF : 0xE6E6E6E6;
        boolean isBlacklist = blacklist.getAsBoolean();
        if (isBlacklist) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 0, 136, 16, 16, 256, 256, tint);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 16, 136, 16, 16, 256, 256, tint);
        }
        if (isHovered()) {
            String s = Component.translatable(isBlacklist ? "button.bl" : "button.wl").getString();
            graphics.centeredText(Minecraft.getInstance().font, s, getX() + 8, getY() + 17, 0xFFFFFFFF);
        }
    }
}