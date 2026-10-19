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
import arcaneresearch.common.menu.SmelterMenu;
@OnlyIn(Dist.CLIENT)
public class SmelterScreen extends AbstractContainerScreen<SmelterMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_smelter.png");
    public SmelterScreen(SmelterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
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
        if (menu.isBurning()) {
            int burnProgress = menu.getBurnTimeScaled(20);
            graphics.blit(TEXTURE, x + 80, y + 26 + 20 - burnProgress, 176, 20 - burnProgress, 16, burnProgress);
        }
        int cookProgress = menu.getCookProgressScaled(46);
        graphics.blit(TEXTURE, x + 106, y + 13 + 46 - cookProgress, 216, 46 - cookProgress, 9, cookProgress);
        int visLevel = menu.getVisScaled(48);
        graphics.blit(TEXTURE, x + 61, y + 12 + 48 - visLevel, 200, 48 - visLevel, 8, visLevel);
        graphics.blit(TEXTURE, x + 60, y + 8, 232, 0, 10, 55);
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
