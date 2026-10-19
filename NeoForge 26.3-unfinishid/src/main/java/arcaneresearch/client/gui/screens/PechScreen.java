package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphics
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was ResourceLocation
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.PechMenu;
import arcaneresearch.init.ModSounds;

public class PechScreen extends AbstractContainerScreen<PechMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_pech.png");

    private static final int TRADE_X = 67;
    private static final int TRADE_Y = 24;
    private static final int TRADE_SIZE = 25;

    public PechScreen(PechMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 175, 232);
    }

    private boolean canTrade() {
        ItemStack input = this.menu.getSlot(0).getItem();
        if (input.isEmpty() || !this.menu.getPech().isValued(input)) {
            return false;
        }
        for (int i = 1; i <= 4; i++) {
            if (!this.menu.getSlot(i).getItem().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // NEOFORGE_PORT: mouse coords/button via MouseButtonEvent.
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        boolean result = super.mouseClicked(event, doubleClick);
        int dx = (int) mouseX - (this.leftPos + TRADE_X);
        int dy = (int) mouseY - (this.topPos + TRADE_Y);
        if (dx >= 0 && dy >= 0 && dx < TRADE_SIZE && dy < TRADE_SIZE && canTrade()) {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
            }
            playTradeSound();
        }
        return result;
    }

    private void playTradeSound() {
        if (this.minecraft != null && this.minecraft.player != null) {
            ModSounds.PECH_DICE.get();
            this.minecraft.player.playSound(ModSounds.PECH_DICE.get(), 0.5f,
                    0.95f + this.minecraft.player.getRandom().nextFloat() * 0.1f);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // 1.20.1 Screen.renderBackground dim (fillGradient)
        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if (canTrade()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + TRADE_X, y + TRADE_Y, 176, 0, TRADE_SIZE, TRADE_SIZE, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }
}