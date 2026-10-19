package arcaneresearch.common.golems.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class GuiGolemCategoryButton extends Button {
    static final ResourceLocation TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_base.png");
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
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 0.9f);
        if (active) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else if (!isHovered()) {
            RenderSystem.setShaderColor(0.7f, 0.7f, 0.7f, 0.7f);
        }
        graphics.blit(TEX, getX(), getY(), icon * 16, 120, 16, 16);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (isHovered()) {
            String s = getMessage().getString();
            graphics.drawString(Minecraft.getInstance().font, s,
                    getX() - 10 - Minecraft.getInstance().font.width(s), getY() - 4, 0xFFFFFF, false);
        }
    }
}
