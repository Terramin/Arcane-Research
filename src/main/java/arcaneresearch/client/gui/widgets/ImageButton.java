package arcaneresearch.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.util.ARGB; 
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 

import java.awt.*;
import java.util.function.Consumer;

public class ImageButton extends AbstractWidget {

    private final Identifier texture;
    private final int texX;
    private final int texY;
    private final int texWidth;
    private final int texHeight;
    private final String description;
    private final int color;
    private final Consumer<ImageButton> onPress;

    private boolean buttonActive = true;

    public ImageButton(int x, int y, int width, int height, String text, String description,
                       Identifier texture, int texX, int texY, int texWidth, int texHeight,
                       Consumer<ImageButton> onPress) {
        this(x, y, width, height, text, description, texture, texX, texY, texWidth, texHeight, 0xFFFFFFFF, onPress);
    }

    public ImageButton(int x, int y, int width, int height, String text, String description,
                       Identifier texture, int texX, int texY, int texWidth, int texHeight,
                       int color, Consumer<ImageButton> onPress) {
        super(x, y, width, height, text != null ? Component.translatable(text) : Component.empty());
        this.texture = texture;
        this.texX = texX;
        this.texY = texY;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.description = description;
        this.color = color;
        this.onPress = onPress;
    }

    public boolean isButtonActive() {
        return buttonActive;
    }

    public void setButtonActive(boolean active) {
        this.buttonActive = active;
    }

    public String getDescription() {
        return description;
    }

    
    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;

        boolean hovered = isMouseOver(mouseX, mouseY);

        Color c = new Color(color);
        float brightness = 0.9f;
        float alpha = 1.0f;

        if (hovered) {
            brightness = 1.0f;
            alpha = 1.0f;
        }
        if (!buttonActive) {
            brightness = 0.5f;
            alpha = 0.9f;
        }

        
        int tint = ARGB.colorFromFloat(alpha,
                brightness * (c.getRed() / 255.0f),
                brightness * (c.getGreen() / 255.0f),
                brightness * (c.getBlue() / 255.0f));

        int drawX = getX() - texWidth / 2;
        int drawY = getY() - texHeight / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, drawX, drawY, texX, texY, texWidth, texHeight, 256, 256, tint);

        String text = getMessage().getString();
        if (text != null && !text.isEmpty()) {
            int textColor = 0xFFFFFFFF;
            if (!active) {
                textColor = 0xFFA0A0A0;
            } else if (hovered) {
                textColor = 0xFFFFFFA0;
            }

            graphics.pose().pushMatrix();
            
            graphics.pose().translate(getX(), getY());
            graphics.pose().scale(0.5f, 0.5f);
            graphics.centeredText(font, Component.translatable(text), 0, -4, textColor);
            graphics.pose().popMatrix();
        }
    }

    
    
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {

        return this.visible && mouseX >= getX() - width / 2 && mouseX < getX() + width / 2 &&
                mouseY >= getY() - height / 2 && mouseY < getY() + height / 2;
    }

    
    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        if (buttonActive && active && visible && isMouseOver(mouseX, mouseY)) {
            if (onPress != null) {
                onPress.accept(this);
            }
            return true;
        }
        return false;
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return;
        java.util.List<net.minecraft.network.chat.Component> tooltip = new java.util.ArrayList<>();
        String text = getMessage().getString();
        if (text != null && !text.isEmpty()) {
            tooltip.add(net.minecraft.network.chat.Component.literal(text));
        }
        if (description != null && !description.isEmpty()) {
            tooltip.add(net.minecraft.network.chat.Component.literal(description)
                    .withStyle(style -> style.withItalic(true).withColor(0x5555FF)));
        }
        if (!tooltip.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(net.minecraft.client.Minecraft.getInstance().font, tooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}