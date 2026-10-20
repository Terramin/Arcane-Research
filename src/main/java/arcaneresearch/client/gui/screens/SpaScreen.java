package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.SpaMenu;
import arcaneresearch.init.ModSounds;
import java.util.List;
import java.util.Objects;
@OnlyIn(Dist.CLIENT)
public class SpaScreen extends AbstractContainerScreen<SpaMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_spa.png");
    private static final int MIX_X = 89;
    private static final int MIX_Y = 35;
    private static final int MIX_SIZE = 8;
    private static final int MIX_HOVER_X = 88;
    private static final int MIX_HOVER_Y = 34;
    private static final int TANK_HOVER_X = 104;
    private static final int TANK_HOVER_Y = 10;
    public SpaScreen(SpaMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        int mhx = mouseX - (this.leftPos + MIX_HOVER_X);
        int mhy = mouseY - (this.topPos + MIX_HOVER_Y);
        if (mhx >= 0 && mhy >= 0 && mhx < 10 && mhy < 10) {
            String key = menu.getBlockEntity().mixMode ? "text.spa.mix.true" : "text.spa.mix.false";
            graphics.renderComponentTooltip(this.font, List.of(Component.translatable(key)), mouseX, mouseY);
        }
        int thx = mouseX - (this.leftPos + TANK_HOVER_X);
        int thy = mouseY - (this.topPos + TANK_HOVER_Y);
        if (thx >= 0 && thy >= 0 && thx < 10 && thy < 55) {
            var tank = menu.getBlockEntity().tank;
            if (!tank.getFluid().isEmpty()) {
                String name = tank.getFluid().getDisplayName().getString();
                graphics.renderComponentTooltip(this.font,
                        List.of(Component.literal(name),
                                Component.literal(tank.getFluidAmount() + " mb")),
                        mouseX, mouseY);
            }
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        int dx = (int) mouseX - (this.leftPos + MIX_X);
        int dy = (int) mouseY - (this.topPos + MIX_Y);
        if (dx >= 0 && dy >= 0 && dx < MIX_SIZE && dy < MIX_SIZE) {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
            }
            if (this.minecraft != null && this.minecraft.player != null) {
                ModSounds.CLACK.get();
                this.minecraft.player.playSound(ModSounds.CLACK.get(), 0.4f, 1.0f);
            }
        }
        return result;
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        if (menu.getBlockEntity().mixMode) {
            graphics.blit(TEXTURE, x + MIX_X, y + MIX_Y, 208, 16, MIX_SIZE, MIX_SIZE);
        } else {
            graphics.blit(TEXTURE, x + MIX_X, y + MIX_Y, 208, 32, MIX_SIZE, MIX_SIZE);
        }
        var tank = menu.getBlockEntity().tank;
        if (tank.getFluidAmount() > 0 && !tank.getFluid().isEmpty()) {
            renderFluid(graphics, x, y, tank.getFluid());
            float bar = tank.getFluidAmount() / (float) tank.getCapacity();
            graphics.blit(TEXTURE, x + 107, y + 15, 107, 15, 10, (int) (48.0f - 48.0f * bar));
        }
        graphics.blit(TEXTURE, x + 106, y + 11, 232, 0, 10, 55);
    }
    private void renderFluid(GuiGraphics graphics, int x, int y, net.minecraftforge.fluids.FluidStack stack) {
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(stack.getFluid());
        @SuppressWarnings("deprecation")
        TextureAtlasSprite sprite = Objects.requireNonNull(this.minecraft).getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(extensions.getStillTexture());
        int tint = extensions.getTintColor(stack);
        float r = ((tint >> 16) & 0xFF) / 255.0f;
        float g = ((tint >> 8) & 0xFF) / 255.0f;
        float b = (tint & 0xFF) / 255.0f;
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.setShaderColor(r, g, b, 1.0f);
        graphics.pose().pushPose();
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        for (int a = 0; a < 6; a++) {
            graphics.blit((x + 107) * 2, (y + 15) * 2 + a * 16, 0, 16, 16, sprite);
        }
        graphics.pose().popPose();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
