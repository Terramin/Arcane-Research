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
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.items.casters.CasterManager;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.ArcaneWorkbenchMenu;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;
@OnlyIn(Dist.CLIENT)
public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/arcaneworkbench.png");
    private static final int[] ASPECT_COLORS = {
            Aspect.AIR.getColor(),
            Aspect.FIRE.getColor(),
            Aspect.WATER.getColor(),
            Aspect.EARTH.getColor(),
            Aspect.ORDER.getColor(),
            Aspect.ENTROPY.getColor()
    };
    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 190;
        this.imageHeight = 234;
        this.titleLabelY = 6;
        this.inventoryLabelX = 16;
        this.inventoryLabelY = 140;
    }
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        assert this.minecraft != null;
        IArcaneRecipe recipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(
                menu.getCraftMatrix(), this.minecraft.player);
        AspectList crystals = null;
        int visCost = 0;
        if (recipe != null) {
            crystals = recipe.getCrystals();
            visCost = ArcaneWorkbenchMenu.getDiscountedVisCost(recipe, this.minecraft.player);
        }
        if (crystals != null && crystals.size() > 0 && this.minecraft.player != null) {
            RenderSystem.blendFunc(
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
            float ticks = (this.minecraft.player.tickCount % 360) + partialTick;
            for (Aspect aspect : crystals.getAspects()) {
                int slotIndex = getAspectSlotIndex(aspect);
                if (slotIndex >= 0) {
                    float centerX = x + ArcaneWorkbenchMenu.CRYSTAL_X[slotIndex] + 7.5f;
                    float centerY = y + ArcaneWorkbenchMenu.CRYSTAL_Y[slotIndex] + 8.0f;
                    int color = aspect.getColor();
                    float r = ((color >> 16) & 0xFF) / 255.0f;
                    float g = ((color >> 8) & 0xFF) / 255.0f;
                    float b = (color & 0xFF) / 255.0f;
                    RenderSystem.setShaderColor(r, g, b, 0.33f);
                    graphics.pose().pushPose();
                    graphics.pose().translate(centerX, centerY, 0);
                    graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(
                            slotIndex * 60 + ticks));
                    graphics.pose().scale(0.5f, 0.5f, 1f);
                    graphics.blit(TEXTURE, -32, -32, 192, 0, 64, 64);
                    graphics.pose().popPose();
                }
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.blendFunc(
                    com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                    com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        }
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        TileArcaneWorkbench tile = menu.getBlockEntity();
        int auraVis = menu.getAuraVis();
        assert this.minecraft != null;
        IArcaneRecipe recipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(
                menu.getCraftMatrix(), this.minecraft.player);
        int visCost = 0;
        int discount = 0;
        if (recipe != null) {
            float discountPct = CasterManager.getTotalVisDiscount(this.minecraft.player);
            discount = (int) (discountPct * 100);
            visCost = ArcaneWorkbenchMenu.getDiscountedVisCost(recipe, this.minecraft.player);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(168, 46, 0);
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        String availText = auraVis + " " + Component.translatable("gui.arcaneresearch.workbench.available").getString();
        int textWidth = this.font.width(availText);
        int textColor = (auraVis < visCost) ? 0xEE6E6E : 0x6E6EEE;
        graphics.drawString(this.font, availText, -textWidth / 2, 0, textColor, true);
        graphics.pose().popPose();
        if (visCost > 0) {
            graphics.pose().pushPose();
            graphics.pose().translate(168, 38, 0);
            graphics.pose().scale(0.5f, 0.5f, 1.0f);
            String costText = visCost + " " + Component.translatable("gui.arcaneresearch.workbench.cost").getString();
            if (discount > 0) {
                costText += " (" + discount + "% " + Component.translatable("gui.arcaneresearch.workbench.discount").getString() + ")";
            }
            textWidth = this.font.width(costText);
            graphics.drawString(this.font, costText, -textWidth / 2, 0, 0xC0FFFF, true);
            graphics.pose().popPose();
            if (auraVis < visCost) {
                RenderSystem.enableBlend();
                graphics.fill(159, 63, 177, 81, 0x80000000);
                RenderSystem.disableBlend();
            }
        }
    }
    private int getAspectSlotIndex(Aspect aspect) {
        for (int i = 0; i < ArcaneWorkbenchMenu.PRIMAL_ASPECTS.length; i++) {
            if (ArcaneWorkbenchMenu.PRIMAL_ASPECTS[i] == aspect) {
                return i;
            }
        }
        return -1;
    }
}
