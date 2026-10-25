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
import java.util.function.Consumer;
@OnlyIn(Dist.CLIENT)
public class ToggleButton extends AbstractWidget {
    private static final ResourceLocation DEFAULT_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_turret_advanced.png");
    private final ResourceLocation texture;
    private final Runnable updater;
    private final Consumer<ToggleButton> onPress;
    private boolean toggled;
    public ToggleButton(int x, int y, String text, Runnable updater, Consumer<ToggleButton> onPress) {
        this(x, y, text, DEFAULT_TEXTURE, updater, onPress);
    }
    public ToggleButton(int x, int y, String text, ResourceLocation texture,
                        Runnable updater, Consumer<ToggleButton> onPress) {
        super(x, y, 8, 8, text != null ? Component.translatable(text) : Component.empty());
        this.texture = texture;
        this.updater = updater;
        this.onPress = onPress;
    }
    public boolean isToggled() {
        return toggled;
    }
    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (updater != null) {
            updater.run();
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(texture, getX(), getY(), 192, 16, 8, 8);
        if (toggled) {
            graphics.blit(texture, getX(), getY(), 192, 24, 8, 8);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, getMessage(), getX() + 12, getY(), 0xFFFFFF, false);
    }
    @Override
    public void onClick(double mouseX, double mouseY) {
        if (onPress != null) {
            onPress.accept(this);
        }
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
