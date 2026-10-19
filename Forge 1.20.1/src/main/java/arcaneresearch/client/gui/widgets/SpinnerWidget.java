package arcaneresearch.client.gui.widgets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.NodeSetting;
@OnlyIn(Dist.CLIENT)
public class SpinnerWidget extends AbstractWidget {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_base.png");
    private final NodeSetting setting;
    private final OnValueChange onValueChange;
    public SpinnerWidget(int x, int y, int width, NodeSetting setting, OnValueChange onValueChange) {
        super(x, y, width, 10, Component.literal(setting.getLocalizedName()));
        this.setting = setting;
        this.onValueChange = onValueChange;
    }
    public NodeSetting getSetting() {
        return setting;
    }
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        boolean hovered = mouseX >= getX() && mouseX < getX() + width + 10
                && mouseY >= getY() && mouseY < getY() + height;
        float brightness = hovered ? 1.0f : 0.9f;
        RenderSystem.setShaderColor(brightness, brightness, brightness, brightness);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(TEXTURE, getX(), getY(), 20, 0, 10, 10);
        graphics.blit(TEXTURE, getX() + width, getY(), 30, 0, 10, 10);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        String valueText = setting.getValueText();
        int textX = getX() + (width + 10) / 2 - font.width(valueText) / 2;
        graphics.drawString(font, valueText, textX, getY() + 1, 0xFFFFFF, true);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !active) return false;
        if (mouseX >= getX() && mouseX < getX() + 10 &&
                mouseY >= getY() && mouseY < getY() + height) {
            setting.decrement();
            if (onValueChange != null) {
                onValueChange.onValueChange(setting, setting.getValue());
            }
            return true;
        }
        if (mouseX >= getX() + width && mouseX < getX() + width + 10 &&
                mouseY >= getY() && mouseY < getY() + height) {
            setting.increment();
            if (onValueChange != null) {
                onValueChange.onValueChange(setting, setting.getValue());
            }
            return true;
        }
        return false;
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
    @FunctionalInterface
    public interface OnValueChange {
        void onValueChange(NodeSetting setting, int newValue);
    }
}
