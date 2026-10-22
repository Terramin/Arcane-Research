package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.player.Inventory;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.menu.PotionSprayerMenu;
import arcaneresearch.common.tiles.devices.TilePotionSprayer;

import java.util.Objects;

public class PotionSprayerScreen extends AbstractContainerScreen<PotionSprayerMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_potion_sprayer.png");

    public PotionSprayerScreen(PotionSprayerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 192, 233);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        TilePotionSprayer tile = menu.getBlockEntity();

        if (tile.charges > 0) {
            int color = tile.color;
            
            int tint = net.minecraft.util.ARGB.colorFromFloat(1.0f,
                    ((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f,
                    (color & 0xFF) / 255f);
                int scroll = Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player).tickCount % 256;
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 128, y + 36 + (8 - tile.charges) * 9, 232, scroll, 8, tile.charges * 9, 256, 256, tint);
            }

        drawAspects(graphics, x, y, tile);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 125, y + 28, 205, 28, 14, 88, 256, 256);
    }

    private void drawAspects(GuiGraphicsExtractor graphics, int x, int y, TilePotionSprayer tile) {
        AspectList recipe = tile.recipe;
        if (recipe == null || recipe.size() <= 0) {
            return;
        }
        int pos = 0;
        for (Aspect aspect : recipe.getAspectsSortedByName()) {
            int need = recipe.getAmount(aspect);
            int have = tile.recipeProgress.getAmount(aspect);

            int fill = need > 0 ? (int) (have / (float) need * 14.0f) : 0;

            int bx = x + 96 + 22 * (pos % 2);
            int by = y + 46 + 16 * (pos / 2);
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, bx, by - 14, 192, 56, 2, 14, 256, 256,
                        net.minecraft.util.ARGB.colorFromFloat(1.0f, 0.2f, 0.2f, 0.2f));
            int color = aspect.getColor();
                graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, bx, by - fill, 192, 56, 2, fill, 256, 256,
                        net.minecraft.util.ARGB.colorFromFloat(1.0f,
                                ((color >> 16) & 0xFF) / 255f,
                                ((color >> 8) & 0xFF) / 255f,
                                (color & 0xFF) / 255f));
                pos++;
        }

        pos = 0;
        for (Aspect aspect : recipe.getAspectsSortedByName()) {
            AspectRenderer.drawAspect(graphics, x + 79 + 22 * (pos % 2), y + 31 + 16 * (pos / 2),
                    aspect, recipe.getAmount(aspect));
            pos++;
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }
}