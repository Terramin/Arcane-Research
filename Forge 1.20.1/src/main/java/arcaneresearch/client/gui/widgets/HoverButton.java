package arcaneresearch.client.gui.widgets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class HoverButton extends AbstractWidget {
    private final String description;
    private final int color;
    private final Object content;
    public HoverButton(int x, int y, int width, int height, String text, String description, Object content) {
        this(x, y, width, height, text, description, content, 0xFFFFFF);
    }
    public HoverButton(int x, int y, int width, int height, String text, String description, Object content, int color) {
        super(x, y, width, height, text != null ? Component.literal(text) : Component.empty());
        this.description = description;
        this.content = content;
        this.color = color;
    }
    public String getDescription() {
        return description;
    }
    public Object getContent() {
        return content;
    }
    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean hovered = isMouseOver(mouseX, mouseY);
        Color c = new Color(color);
        float brightness = hovered ? 1.0f : 0.9f;
        float alpha = hovered ? 1.0f : 0.9f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int drawX = getX() - width / 2;
        int drawY = getY() - height / 2;
        if (content instanceof Aspect aspect) {
            Color aspectColor = new Color(aspect.getColor());
            if (hovered) {
                RenderSystem.setShaderColor(
                        aspectColor.getRed() / 255.0f,
                        aspectColor.getGreen() / 255.0f,
                        aspectColor.getBlue() / 255.0f,
                        1.0f
                );
            } else {
                RenderSystem.setShaderColor(
                        aspectColor.getRed() / 290.0f,
                        aspectColor.getGreen() / 290.0f,
                        aspectColor.getBlue() / 290.0f,
                        0.9f
                );
            }
            graphics.blit(UtilsFX.strippedAspectTexture(aspect.getImage()), drawX, drawY, 0, 0, 16, 16, 16, 16);
        } else if (content instanceof ResourceLocation texture) {
            RenderSystem.setShaderColor(
                    brightness * (c.getRed() / 255.0f),
                    brightness * (c.getGreen() / 255.0f),
                    brightness * (c.getBlue() / 255.0f),
                    alpha
            );
            graphics.blit(texture, drawX, drawY, 0, 0, 16, 16, 16, 16);
        } else if (content instanceof ItemStack stack) {
            int yOffset = hovered ? -1 : 0;
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, stack, drawX, drawY + yOffset);
        } else if (content instanceof net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {
            graphics.blit(drawX, drawY, 0, 16, 16, sprite);
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return;
        List<Component> tooltip = new ArrayList<>();
        if (content instanceof ItemStack stack) {
            tooltip.addAll(stack.getTooltipLines(Minecraft.getInstance().player,
                    Minecraft.getInstance().options.advancedItemTooltips
                            ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED
                            : net.minecraft.world.item.TooltipFlag.Default.NORMAL));
        } else {
            String text = getMessage().getString();
            if (text != null && !text.isEmpty()) {
                tooltip.add(Component.literal(text));
            }
        }
        if (description != null && !description.isEmpty()) {
            tooltip.add(Component.literal(description).withStyle(style -> style.withItalic(true).withColor(0x5555FF)));
        }
        if (!tooltip.isEmpty()) {
            graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() - width / 2 && mouseX < getX() + width / 2 &&
                mouseY >= getY() - height / 2 && mouseY < getY() + height / 2;
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
