package arcaneresearch.common.golems.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import arcaneresearch.Arcaneresearch;

public class GuiGolemCategoryButton extends Button {
    static final Identifier TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_base.png");

    final int icon;
    boolean active;

    public GuiGolemCategoryButton(int x, int y, String labelKey, int icon, boolean active, OnPress onPress) {
        super(x, y, 16, 16, Component.translatable(labelKey), onPress, Button.DEFAULT_NARRATION);
        this.icon = icon;
        this.active = active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    
    
    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        
        int tint = active ? 0xFFFFFFFF : (isHovered() ? 0xE6E6E6E6 : 0xB3B3B3B3);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEX, getX(), getY(), icon * 16, 120, 16, 16, 256, 256, tint);
        if (isHovered()) {
            String s = getMessage().getString();
            graphics.text(Minecraft.getInstance().font, s,
                    getX() - 10 - Minecraft.getInstance().font.width(s), getY() - 4, 0xFFFFFFFF, false);
        }
    }
}