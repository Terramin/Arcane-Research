package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.player.Inventory;
import arcaneresearch.common.menu.HungryChestMenu;

public class HungryChestScreen extends AbstractContainerScreen<HungryChestMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    private static final int ROWS = 3;

    public HungryChestScreen(HungryChestMenu menu, Inventory playerInventory, Component title) {
        
        super(menu, playerInventory, title, 176, 114 + ROWS * 18);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, ROWS * 18 + 17, 256, 256);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y + ROWS * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
    }

}