package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.PechMenu;
import arcaneresearch.init.ModSounds;
@OnlyIn(Dist.CLIENT)
public class PechScreen extends AbstractContainerScreen<PechMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_pech.png");
    private static final int TRADE_X = 67;
    private static final int TRADE_Y = 24;
    private static final int TRADE_SIZE = 25;
    public PechScreen(PechMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 175;
        this.imageHeight = 232;
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
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
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
        if (canTrade()) {
            graphics.blit(TEXTURE, x + TRADE_X, y + TRADE_Y, 176, 0, TRADE_SIZE, TRADE_SIZE);
        }
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
