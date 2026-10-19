package arcaneresearch.common.golems.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class GuiGolemCraftButton extends Button {
    static final ResourceLocation TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_golembuilder.png");
    public GuiGolemCraftButton(int x, int y, OnPress onPress) {
        super(x, y, 24, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
    }
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 0.9f);
        if (active && isHovered()) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        graphics.blit(TEX, getX(), getY(), 216, 64, 24, 16);
        if (!active) {
            graphics.blit(TEX, getX(), getY(), 216, 40, 24, 16);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
