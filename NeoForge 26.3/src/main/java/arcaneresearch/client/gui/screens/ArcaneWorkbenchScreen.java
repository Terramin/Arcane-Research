package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.items.casters.CasterManager;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.ArcaneWorkbenchMenu;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;

public class ArcaneWorkbenchScreen extends AbstractContainerScreen<ArcaneWorkbenchMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/arcaneworkbench.png");

    private static final int[] ASPECT_COLORS = {
            Aspect.AIR.getColor(),
            Aspect.FIRE.getColor(),
            Aspect.WATER.getColor(),
            Aspect.EARTH.getColor(),
            Aspect.ORDER.getColor(),
            Aspect.ENTROPY.getColor()
    };

    public ArcaneWorkbenchScreen(ArcaneWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 190, 234);

        this.titleLabelY = 6;
        this.inventoryLabelX = 16;
        this.inventoryLabelY = 140;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        int x = this.leftPos;
        int y = this.topPos;

        // NEOFORGE_PORT: GL blend/shader-color gone (pipelines own state); bg blit +
        // rotating crystal highlights via tinted blits (additive pass approximated
        // with low-alpha tint; same positions/rotation as Forge).
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0,
                this.imageWidth, this.imageHeight, 256, 256);

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
            float ticks = (this.minecraft.player.tickCount % 360) + partialTick;
            for (Aspect aspect : crystals.getAspects()) {
                int slotIndex = getAspectSlotIndex(aspect);
                if (slotIndex >= 0) {
                    float centerX = x + ArcaneWorkbenchMenu.CRYSTAL_X[slotIndex] + 7.5f;
                    float centerY = y + ArcaneWorkbenchMenu.CRYSTAL_Y[slotIndex] + 8.0f;

                    int color = aspect.getColor();
                    int tint = net.minecraft.util.ARGB.colorFromFloat(0.33f,
                            ((color >> 16) & 0xFF) / 255.0f,
                            ((color >> 8) & 0xFF) / 255.0f,
                            (color & 0xFF) / 255.0f);
                    graphics.pose().pushMatrix();
                    graphics.pose().translate(centerX, centerY);
                    graphics.pose().rotate((float) Math.toRadians(slotIndex * 60 + ticks));
                    graphics.pose().scale(0.5f, 0.5f);
                    // NEOFORGE_PORT (26.3): Forge blendFunc(SRC_ALPHA, ONE) -> additive GUI pipeline.
                    graphics.blit(arcaneresearch.client.renderers.TCRenderTypes.GUI_TEXTURED_ADDITIVE, TEXTURE, -32, -32,
                            192, 0, 64, 64, 256, 256, tint);
                    graphics.pose().popMatrix();
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

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

        graphics.pose().pushMatrix();
        graphics.pose().translate(168, 46);
        graphics.pose().scale(0.5f, 0.5f);

        String availText = auraVis + " " + Component.translatable("gui.arcaneresearch.workbench.available").getString();
        int textWidth = this.font.width(availText);

        int textColor = (auraVis < visCost) ? 0xFFEE6E6E : 0xFF6E6EEE;
        graphics.text(this.font, availText, -textWidth / 2, 0, textColor, true);

        graphics.pose().popMatrix();

        if (visCost > 0) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(168, 38);
            graphics.pose().scale(0.5f, 0.5f);

            String costText = visCost + " " + Component.translatable("gui.arcaneresearch.workbench.cost").getString();
            if (discount > 0) {
                costText += " (" + discount + "% " + Component.translatable("gui.arcaneresearch.workbench.discount").getString() + ")";
            }
            textWidth = this.font.width(costText);
            graphics.text(this.font, costText, -textWidth / 2, 0, 0xFFC0FFFF, true);

            graphics.pose().popMatrix();

            if (auraVis < visCost) {

                graphics.fill(159, 63, 177, 81, 0x80000000);
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