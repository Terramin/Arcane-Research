package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.SmelterMenu;

public class SmelterScreen extends AbstractContainerScreen<SmelterMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_smelter.png");

    public SmelterScreen(SmelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if (menu.isBurning()) {
            int burnProgress = menu.getBurnTimeScaled(20);

            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 80, y + 26 + 20 - burnProgress, 176, 20 - burnProgress, 16, burnProgress, 256, 256);
        }

        int cookProgress = menu.getCookProgressScaled(46);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 106, y + 13 + 46 - cookProgress, 216, 46 - cookProgress, 9, cookProgress, 256, 256);

        int visLevel = menu.getVisScaled(48);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 61, y + 12 + 48 - visLevel, 200, 48 - visLevel, 8, visLevel, 256, 256);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 60, y + 8, 232, 0, 10, 55, 256, 256);
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }
}