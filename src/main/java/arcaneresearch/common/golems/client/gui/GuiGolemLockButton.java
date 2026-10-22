package arcaneresearch.common.golems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;

import java.util.function.BooleanSupplier;

public class GuiGolemLockButton extends Button {
    static final Identifier TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    private final BooleanSupplier locked;

    public GuiGolemLockButton(int x, int y, BooleanSupplier locked, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.locked = locked;
    }

    
    
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        
        int tint = isHovered() ? 0xFFFFFFFF : 0xE6E6E6E6;
        boolean isLocked = locked.getAsBoolean();
        if (isLocked) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 32, 136, 16, 16, 256, 256, tint);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), 48, 136, 16, 16, 256, 256, tint);
        }
        if (isHovered()) {
            String s = Component.translatable(isLocked ? "golem.prop.lock" : "golem.prop.unlock").getString();
            graphics.centeredText(Minecraft.getInstance().font, s, getX() + 8, getY() + 17, 0xFFFFFFFF);
        }
    }
}