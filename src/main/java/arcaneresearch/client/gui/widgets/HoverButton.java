package arcaneresearch.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.util.ARGB; 
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.client.lib.UtilsFX;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HoverButton extends AbstractWidget {

    private final String description;
    private final int color;
    private final Object content;

    public HoverButton(int x, int y, int width, int height, String text, String description, Object content) {
        this(x, y, width, height, text, description, content, 0xFFFFFFFF);
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
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        boolean hovered = isMouseOver(mouseX, mouseY);

        Color c = new Color(color);
        float brightness = hovered ? 1.0f : 0.9f;
        float alpha = hovered ? 1.0f : 0.9f;

        
        int drawX = getX() - width / 2;
        int drawY = getY() - height / 2;

        if (content instanceof Aspect aspect) {

            Color aspectColor = new Color(aspect.getColor());
            int tint;
            if (hovered) {
                tint = ARGB.colorFromFloat(1.0f,
                        aspectColor.getRed() / 255.0f,
                        aspectColor.getGreen() / 255.0f,
                        aspectColor.getBlue() / 255.0f);
            } else {
                tint = ARGB.colorFromFloat(0.9f,
                        aspectColor.getRed() / 290.0f,
                        aspectColor.getGreen() / 290.0f,
                        aspectColor.getBlue() / 290.0f);
            }
            
            
            Identifier stripped = UtilsFX.strippedAspectTexture(aspect.getImage());
            int[] dims = AspectRenderer.textureDims(stripped);
            int sw = dims != null ? dims[0] : 16;
            int sh = dims != null ? dims[1] : 16;
            graphics.blit(RenderPipelines.GUI_TEXTURED, stripped, drawX, drawY, 0, 0, 16, 16,
                    sw, sh, sw, sh, tint);
        } else if (content instanceof Identifier texture) {

            int tint = ARGB.colorFromFloat(alpha,
                    brightness * (c.getRed() / 255.0f),
                    brightness * (c.getGreen() / 255.0f),
                    brightness * (c.getBlue() / 255.0f));
            int[] dims = AspectRenderer.textureDims(texture);
            int sw = dims != null ? dims[0] : 16;
            int sh = dims != null ? dims[1] : 16;
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, drawX, drawY, 0, 0, 16, 16,
                    sw, sh, sw, sh, tint);
        } else if (content instanceof ItemStack stack) {

            int yOffset = hovered ? -1 : 0;
            arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, stack, drawX, drawY + yOffset);
        } else if (content instanceof net.minecraft.client.renderer.texture.TextureAtlasSprite sprite) {

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, drawX, drawY, 16, 16);
        }

    }

    public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return;

        List<Component> tooltip = new ArrayList<>();

        if (content instanceof ItemStack stack) {
            
            tooltip.addAll(stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(
                            Minecraft.getInstance().level),
                    Minecraft.getInstance().player,
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
            
            graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {

        return mouseX >= getX() - width / 2 && mouseX < getX() + width / 2 &&
                mouseY >= getY() - height / 2 && mouseY < getY() + height / 2;
    }

    
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}