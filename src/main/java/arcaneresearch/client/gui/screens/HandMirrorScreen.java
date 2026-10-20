package arcaneresearch.client.gui.screens;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.HandMirrorMenu;

public class HandMirrorScreen extends AbstractContainerScreen<HandMirrorMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_handmirror.png");

    public HandMirrorScreen(HandMirrorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if (this.minecraft != null && this.minecraft.player != null) {
            int ci = this.minecraft.player.getInventory().getSelectedSlot();
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 8 + ci * 18, y + 142, 240, 0, 16, 16, 256, 256);
        }
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }

    @Override
    // NEOFORGE_PORT: hotbar keys via KeyEvent (matches(KeyEvent) verified via javap).

    public boolean keyPressed(KeyEvent event) {
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