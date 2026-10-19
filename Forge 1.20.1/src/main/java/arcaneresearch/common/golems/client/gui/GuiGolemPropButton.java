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
import java.util.function.BooleanSupplier;
@OnlyIn(Dist.CLIENT)
public class GuiGolemPropButton extends Button {
    static final ResourceLocation TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private final BooleanSupplier value;
    public GuiGolemPropButton(int x, int y, int width, int height, Component label, BooleanSupplier value, OnPress onPress) {
        super(x, y, width, height, label, onPress, Button.DEFAULT_NARRATION);
        this.value = value;
    }
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(TEX, getX() - 2, getY() - 2, 2, 18, 12, 12);
        if (value.getAsBoolean()) {
            graphics.blit(TEX, getX() - 2, getY() - 2, 18, 18, 12, 12);
        }
        graphics.drawString(Minecraft.getInstance().font, getMessage().getString(),
                getX() + 12, getY(), 0xFFFFFF, false);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
