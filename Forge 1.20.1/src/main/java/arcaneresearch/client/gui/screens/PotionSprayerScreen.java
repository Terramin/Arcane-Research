package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.menu.PotionSprayerMenu;
import arcaneresearch.common.tiles.devices.TilePotionSprayer;
import java.util.Objects;
@OnlyIn(Dist.CLIENT)
public class PotionSprayerScreen extends AbstractContainerScreen<PotionSprayerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_potion_sprayer.png");
    public PotionSprayerScreen(PotionSprayerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 192;
        this.imageHeight = 233;
        this.inventoryLabelY = this.imageHeight - 94;
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        TilePotionSprayer tile = menu.getBlockEntity();
        if (tile.charges > 0) {
            int color = tile.color;
            RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1f);
            int scroll = Objects.requireNonNull(Objects.requireNonNull(this.minecraft).player).tickCount % 256;
            graphics.blit(TEXTURE, x + 128, y + 36 + (8 - tile.charges) * 9,
                    232, scroll, 8, tile.charges * 9);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        }
        drawAspects(graphics, x, y, tile);
        graphics.blit(TEXTURE, x + 125, y + 28, 205, 28, 14, 88);
    }
    private void drawAspects(GuiGraphics graphics, int x, int y, TilePotionSprayer tile) {
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
            RenderSystem.setShaderColor(0.2f, 0.2f, 0.2f, 1f);
            graphics.blit(TEXTURE, bx, by - 14, 192, 56, 2, 14);
            int color = aspect.getColor();
            RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255f,
                    ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f, 1f);
            graphics.blit(TEXTURE, bx, by - fill, 192, 56, 2, fill);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
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
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
