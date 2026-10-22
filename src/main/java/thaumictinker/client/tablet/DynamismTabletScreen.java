package thaumictinker.client.tablet;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import thaumictinker.net.TtNet;
import thaumictinker.tablet.DynamismTabletMenu;

 



 
public class DynamismTabletScreen extends AbstractContainerScreen<DynamismTabletMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("thaumictinker", "textures/gui/animationtablet.png");
    private static final int TEXT_COLOR = 0xFF999999;
    private static final int BUTTON_SIZE = 13;
    private static final int LEFT_X = 52;
    private static final int RIGHT_X = 111;
    private static final int MODE_Y = 15;
    private static final int REDSTONE_Y = 60;

    public DynamismTabletScreen(DynamismTabletMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        
        
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private int redstoneX() {
        return imageWidth / 2 - 7;
    }

    private boolean over(double mouseX, double mouseY, int x, int y) {
        double offsetX = mouseX - (leftPos + x);
        double offsetY = mouseY - (topPos + y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    private void button(GuiGraphics graphics, int x, int y, boolean enabled) {
        if (enabled) {
            graphics.blit(TEXTURE, leftPos + x, topPos + y, 176, 13, BUTTON_SIZE, BUTTON_SIZE);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (over(mouseX, mouseY, LEFT_X, MODE_Y)) {
            graphics.renderTooltip(font, Component.translatable("ttinkerer.ttmisc.leftClick"), mouseX, mouseY);
        } else if (over(mouseX, mouseY, RIGHT_X, MODE_Y)) {
            graphics.renderTooltip(font, Component.translatable("ttinkerer.ttmisc.rightClick"), mouseX, mouseY);
        } else if (over(mouseX, mouseY, redstoneX(), REDSTONE_Y)) {
            graphics.renderTooltip(font, Component.translatable("ttinkerer.ttmisc.redstoneControl"), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        button(graphics, LEFT_X, MODE_Y, menu.isLeftClick());
        button(graphics, RIGHT_X, MODE_Y, !menu.isLeftClick());
        button(graphics, redstoneX(), REDSTONE_Y, menu.isRedstone());
        Component left = Component.translatable("ttinkerer.ttmisc.leftClick");
        Component redstone = Component.translatable("ttinkerer.ttmisc.redstoneControl");
        graphics.drawString(font, left, leftPos + 48 - font.width(left), topPos + 18, TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("ttinkerer.ttmisc.rightClick"), leftPos + 128, topPos + 18,
                TEXT_COLOR, false);
        graphics.drawString(font, redstone, leftPos + imageWidth / 2 - font.width(redstone) / 2, topPos + 50,
                TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (over(mouseX, mouseY, LEFT_X, MODE_Y)) {
                TtNet.sendTabletButton(true, menu.isRedstone());
                return true;
            }
            if (over(mouseX, mouseY, RIGHT_X, MODE_Y)) {
                TtNet.sendTabletButton(false, menu.isRedstone());
                return true;
            }
            if (over(mouseX, mouseY, redstoneX(), REDSTONE_Y)) {
                TtNet.sendTabletButton(menu.isLeftClick(), !menu.isRedstone());
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
