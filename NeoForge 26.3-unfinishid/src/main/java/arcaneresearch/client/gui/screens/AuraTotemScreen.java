package arcaneresearch.client.gui.screens;
import arcaneresearch.common.menu.AuraTotemMenu;
import arcaneresearch.common.tiles.aura.AuraTotemTile;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import arcaneresearch.api.aspects.Aspect;

/**
 * Totem fuel screen, ported from TC5 GuiAuraTotem.
 * Dedicated gui_auratotem.png backdrop with an aspect-tinted charge bar.
 */
public class AuraTotemScreen extends AbstractContainerScreen<AuraTotemMenu> {
    private static final Identifier BACKDROP =
            Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID, "textures/gui/gui_auratotem.png");
    public AuraTotemScreen(AuraTotemMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        // 1.20.1 Screen.renderBackground dim (fillGradient)
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKDROP, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        Container totem = this.menu.getTotem();
        int width = totem instanceof AuraTotemTile tile ? tile.getTimeScaled(48) : this.menu.getLastTime();
        Aspect aspect = totem instanceof AuraTotemTile tile ? tile.getAspect() : null;
        if (aspect != null) {
            int rgb = aspect.getColor();
            int tint = net.minecraft.util.ARGB.colorFromFloat(1.0F,
                    (rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F);
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKDROP, this.leftPos + 64, this.topPos + 52, 176, 48, width, 8, 256, 256, tint);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, BACKDROP, this.leftPos + 64, this.topPos + 52, 176, 58, width, 8, 256, 256);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKDROP, this.leftPos + 60, this.topPos + 51, 176, 32, 56, 10, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}