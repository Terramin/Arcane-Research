package thaumictinker.client.gateway;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import thaumictinker.kami.gateway.WarpGateMenu;
import thaumictinker.net.TtNet;

 


 
public class WarpGateScreen extends AbstractContainerScreen<WarpGateMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("thaumictinker", "textures/gui/warpgate.png");
    private static final int TEXT_COLOR = 0xFF999999;
    private static final int BUTTON_SIZE = 13;
    private static final int BUTTON_X = 5;
    private static final int BUTTON_Y = 5;

    public WarpGateScreen(WarpGateMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private boolean overButton(double mouseX, double mouseY) {
        double offsetX = mouseX - (leftPos + BUTTON_X);
        double offsetY = mouseY - (topPos + BUTTON_Y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isLocked()) {
            graphics.blit(TEXTURE, leftPos + BUTTON_X, topPos + BUTTON_Y, 176, 0, BUTTON_SIZE, BUTTON_SIZE);
        }
        graphics.drawString(font, Component.translatable("ttinkerer.ttmisc.lockedGate"),
                leftPos + 20, topPos + 7, TEXT_COLOR, true);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && overButton(mouseX, mouseY)) {
            TtNet.sendWarpGateButton(!menu.isLocked());
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
