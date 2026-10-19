package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.FocusPouchMenu;
@OnlyIn(Dist.CLIENT)
public class FocusPouchScreen extends AbstractContainerScreen<FocusPouchMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_focuspouch.png");
    private final int blockSlot;
    public FocusPouchScreen(FocusPouchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 175;
        this.imageHeight = 232;
        this.inventoryLabelY = this.imageHeight - 94;
        this.blockSlot = playerInventory.selected;
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
        graphics.blit(TEXTURE, x + 8 + blockSlot * 18, y + 209, 240, 0, 16, 16);
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.minecraft != null) {
            for (KeyMapping hotbarKey : this.minecraft.options.keyHotbarSlots) {
                if (hotbarKey.matches(keyCode, scanCode)) {
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
