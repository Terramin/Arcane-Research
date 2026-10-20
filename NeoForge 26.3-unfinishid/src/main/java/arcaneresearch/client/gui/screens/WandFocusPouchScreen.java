package arcaneresearch.client.gui.screens;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.player.Inventory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.WandFocusPouchMenu;

public class WandFocusPouchScreen extends AbstractContainerScreen<WandFocusPouchMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_focuspouch.png");

    private final int blockSlot;

    public WandFocusPouchScreen(WandFocusPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 175, 232);
        this.inventoryLabelY = this.imageHeight - 94;
        this.blockSlot = playerInventory.getSelectedSlot();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.getInventory().getItem(this.blockSlot).isEmpty()) {
            this.minecraft.player.closeContainer();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8 + blockSlot * 18, y + 209, 240, 0, 16, 16, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    // NEOFORGE_PORT: hotbar keys via KeyEvent (matches(KeyEvent) verified via javap).
    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (this.minecraft != null) {
            for (KeyMapping hotbarKey : this.minecraft.options.keyHotbarSlots) {
                if (hotbarKey.matches(event)) {
                    return true;
                }
            }
        }
        return super.keyPressed(event);
    }
}