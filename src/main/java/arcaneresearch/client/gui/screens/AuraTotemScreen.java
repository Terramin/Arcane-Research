package arcaneresearch.client.gui.screens;
import arcaneresearch.common.menu.AuraTotemMenu;
import arcaneresearch.common.tiles.aura.AuraTotemTile;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
@OnlyIn(Dist.CLIENT)
public class AuraTotemScreen extends AbstractContainerScreen<AuraTotemMenu> {
    private static final ResourceLocation BACKDROP =
            new ResourceLocation(arcaneresearch.Arcaneresearch.MODID, "textures/gui/gui_auratotem.png");
    public AuraTotemScreen(AuraTotemMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(BACKDROP, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        Container totem = this.menu.getTotem();
        int width = totem instanceof AuraTotemTile tile ? tile.getTimeScaled(48) : this.menu.getLastTime();
        Aspect aspect = totem instanceof AuraTotemTile tile ? tile.getAspect() : null;
        if (aspect != null) {
            int rgb = aspect.getColor();
            graphics.setColor((rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F,
                    (rgb & 255) / 255.0F, 1.0F);
            graphics.blit(BACKDROP, this.leftPos + 64, this.topPos + 52, 176, 48, width, 8, 256, 256);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            graphics.blit(BACKDROP, this.leftPos + 64, this.topPos + 52, 176, 58, width, 8, 256, 256);
        }
        graphics.blit(BACKDROP, this.leftPos + 60, this.topPos + 51, 176, 32, 56, 10, 256, 256);
    }
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
