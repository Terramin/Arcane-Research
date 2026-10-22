package thaumictinker.client.trinkets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import thaumictinker.kami.trinkets.IchorPouchMenu;

 



 
public class IchorPouchScreen extends AbstractContainerScreen<IchorPouchMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("thaumictinker", "textures/gui/ichorpouch.png");
    private final int blockSlot;

    public IchorPouchScreen(IchorPouchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
        this.inventoryLabelY = 171;
        this.blockSlot = inventory.selected;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.getInventory().getItem(blockSlot).isEmpty()) {
            this.onClose();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        
    }
}
