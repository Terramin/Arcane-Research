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
public class GuiGolemBWListButton extends Button {
    static final ResourceLocation TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private final BooleanSupplier blacklist;
    public GuiGolemBWListButton(int x, int y, BooleanSupplier blacklist, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.blacklist = blacklist;
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
        boolean isBlacklist = blacklist.getAsBoolean();
        if (isBlacklist) {
            graphics.blit(TEX, getX(), getY(), 0, 136, 16, 16);
        } else {
            graphics.blit(TEX, getX(), getY(), 16, 136, 16, 16);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (isHovered()) {
            String s = Component.translatable(isBlacklist ? "button.bl" : "button.wl").getString();
            graphics.drawCenteredString(Minecraft.getInstance().font, s, getX() + 8, getY() + 17, 0xFFFFFF);
        }
    }
}
