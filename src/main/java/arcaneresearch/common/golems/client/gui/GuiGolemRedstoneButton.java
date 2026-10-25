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
public class GuiGolemRedstoneButton extends Button {
    static final ResourceLocation TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private final BooleanSupplier sensitive;
    public GuiGolemRedstoneButton(int x, int y, BooleanSupplier sensitive, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.sensitive = sensitive;
    }
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (isHovered()) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        } else {
            RenderSystem.setShaderColor(0.9f, 0.9f, 0.9f, 0.9f);
        }
        boolean isSensitive = sensitive.getAsBoolean();
        if (isSensitive) {
            graphics.blit(TEX, getX(), getY(), 64, 136, 16, 16);
        } else {
            graphics.blit(TEX, getX(), getY(), 80, 136, 16, 16);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (isHovered()) {
            String s = Component.translatable(isSensitive ? "golem.prop.redon" : "golem.prop.redoff").getString();
            graphics.drawString(Minecraft.getInstance().font, s,
                    getX() - 2 - Minecraft.getInstance().font.width(s), getY() + 4, 0xFFFFFF, false);
        }
    }
}
