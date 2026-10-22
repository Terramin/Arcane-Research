package thaumictinker.client.attractor;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import thaumictinker.attractor.CorporealAttractorMenu;
import thaumictinker.attractor.SoulMouldItem;
import thaumictinker.net.TtNet;

 



 
public class CorporealAttractorScreen extends AbstractContainerScreen<CorporealAttractorMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("thaumictinker", "textures/gui/mobmagnet.png");
    private static final int TEXT_COLOR = 0xFF999999;
    private static final int BUTTON_SIZE = 13;
    private static final int BUTTON_X = 100;
    private static final int ADULT_Y = 28;
    private static final int CHILD_Y = 48;

    public CorporealAttractorScreen(CorporealAttractorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private boolean over(double mouseX, double mouseY, int y) {
        double offsetX = mouseX - (leftPos + BUTTON_X);
        double offsetY = mouseY - (topPos + y);
        return offsetX >= 0 && offsetY >= 0 && offsetX < BUTTON_SIZE && offsetY < BUTTON_SIZE;
    }

    private Component filterName() {
        ItemStack stack = menu.filter();
        if (stack.isEmpty()) {
            return Component.translatable("ttinkerer.ttmisc.all");
        }
        String pattern = SoulMouldItem.getPatternName(stack);
        if (pattern == null || pattern.isEmpty()) {
            return Component.translatable("ttinkerer.ttmisc.none");
        }
        return SoulMouldItem.patternName(pattern);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int enabledY = menu.isAdult() ? ADULT_Y : CHILD_Y;
        graphics.blit(TEXTURE, leftPos + BUTTON_X, topPos + enabledY, 176, 13, BUTTON_SIZE, BUTTON_SIZE);
        Component filter = filterName();
        graphics.drawString(font, filter, leftPos + imageWidth / 2 - font.width(filter) / 2 - 26, topPos + 16,
                TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("ttinkerer.ttmisc.adult"), leftPos + 120, topPos + 30,
                TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("ttinkerer.ttmisc.child"), leftPos + 120, topPos + 50,
                TEXT_COLOR, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (over(mouseX, mouseY, ADULT_Y)) {
                TtNet.sendMobMagnetButton(true);
                return true;
            }
            if (over(mouseX, mouseY, CHILD_Y)) {
                TtNet.sendMobMagnetButton(false);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
