package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; 
import net.minecraft.client.renderer.RenderPipelines; 
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.SpaMenu;
import arcaneresearch.init.ModSounds;

import java.util.List;
import java.util.Objects;

public class SpaScreen extends AbstractContainerScreen<SpaMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_spa.png");

    private static final int MIX_X = 89;
    private static final int MIX_Y = 35;
    private static final int MIX_SIZE = 8;

    private static final int MIX_HOVER_X = 88;
    private static final int MIX_HOVER_Y = 34;

    private static final int TANK_HOVER_X = 104;
    private static final int TANK_HOVER_Y = 10;

    public SpaScreen(SpaMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    
    
    private void renderHoverTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int mhx = mouseX - (this.leftPos + MIX_HOVER_X);
        int mhy = mouseY - (this.topPos + MIX_HOVER_Y);
        if (mhx >= 0 && mhy >= 0 && mhx < 10 && mhy < 10) {
            String key = menu.getBlockEntity().mixMode ? "text.spa.mix.true" : "text.spa.mix.false";
            graphics.setComponentTooltipForNextFrame(this.font,
                    List.of(Component.translatable(key)), mouseX, mouseY);
        }

        int thx = mouseX - (this.leftPos + TANK_HOVER_X);
        int thy = mouseY - (this.topPos + TANK_HOVER_Y);
        if (thx >= 0 && thy >= 0 && thx < 10 && thy < 55) {
            var tank = menu.getBlockEntity().tank;
            if (!tank.getFluid().isEmpty()) {
                String name = tank.getFluid().getHoverName().getString();
                graphics.setComponentTooltipForNextFrame(this.font,
                        List.of(Component.literal(name),
                                Component.literal(tank.getFluidAmount() + " mb")),
                        mouseX, mouseY);
            }
        }
    }

    @Override
    
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();
        boolean result = super.mouseClicked(event, doubleClick);
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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        if (menu.getBlockEntity().mixMode) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + MIX_X, y + MIX_Y, 208, 16, MIX_SIZE, MIX_SIZE, 256, 256);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + MIX_X, y + MIX_Y, 208, 32, MIX_SIZE, MIX_SIZE, 256, 256);
        }

        var tank = menu.getBlockEntity().tank;
        if (tank.getFluidAmount() > 0 && !tank.getFluid().isEmpty()) {
            renderFluid(graphics, x, y, tank.getFluid());
            float bar = tank.getFluidAmount() / (float) tank.getCapacity();
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 107, y + 15, 107, 15, 10, (int) (48.0f - 48.0f * bar), 256, 256);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 106, y + 11, 232, 0, 10, 55, 256, 256);
        renderHoverTooltips(graphics, mouseX, mouseY);
    }

    private void renderFluid(GuiGraphicsExtractor graphics, int x, int y, net.neoforged.neoforge.fluids.FluidStack stack) {
        
        
        
        TextureAtlasSprite sprite = fluidStillSprite(stack);
        int tint = fluidTint(stack);
        graphics.pose().pushMatrix();
        graphics.pose().scale(0.5f, 0.5f);
        for (int a = 0; a < 6; a++) {
            if (sprite != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite,
                        (x + 107) * 2, (y + 15) * 2 + a * 16, 16, 16, tint);
            } else {
                graphics.fill((x + 107) * 2, (y + 15) * 2 + a * 16,
                        (x + 107) * 2 + 16, (y + 15) * 2 + a * 16 + 16, tint);
            }
        }
        graphics.pose().popMatrix();
    }

    private static TextureAtlasSprite fluidStillSprite(net.neoforged.neoforge.fluids.FluidStack stack) {
        try {
            Identifier still = fluidStillTexture(stack);
            if (still == null) {
                return null;
            }
            return net.minecraft.client.Minecraft.getInstance().getAtlasManager().get(
                    new net.minecraft.client.resources.model.sprite.SpriteId(
                            TextureAtlas.LOCATION_BLOCKS, still));
        } catch (Exception e) {
            return null;
        }
    }

    private static Identifier fluidStillTexture(net.neoforged.neoforge.fluids.FluidStack stack) {
        net.minecraft.world.level.material.Fluid fluid = stack.getFluid();
        if (fluid == net.minecraft.world.level.material.Fluids.WATER
                || fluid == arcaneresearch.init.ModFluids.PURIFYING_FLUID.get()) {
            return arcaneresearch.init.ModFluids.PURIFYING_STILL;
        }
        if (fluid == net.minecraft.world.level.material.Fluids.LAVA) {
            return Identifier.withDefaultNamespace("block/lava_still");
        }
        if (fluid == arcaneresearch.init.ModFluids.LIQUID_DEATH.get()) {
            return arcaneresearch.init.ModFluids.LIQUID_DEATH_STILL;
        }
        if (fluid == arcaneresearch.init.ModFluids.FLUX_GAS.get()) {
            return arcaneresearch.init.ModFluids.FLUX_GAS_STILL;
        }
        return null;
    }

    private static int fluidTint(net.neoforged.neoforge.fluids.FluidStack stack) {
        net.minecraft.world.level.material.Fluid fluid = stack.getFluid();
        if (fluid == arcaneresearch.init.ModFluids.LIQUID_DEATH.get()) {
            return arcaneresearch.init.ModFluids.LIQUID_DEATH_TINT;
        }
        if (fluid == arcaneresearch.init.ModFluids.PURIFYING_FLUID.get()) {
            return arcaneresearch.init.ModFluids.PURIFYING_TINT;
        }
        if (fluid == arcaneresearch.init.ModFluids.FLUX_GAS.get()) {
            return arcaneresearch.init.ModFluids.FLUX_GAS_TINT;
        }
        if (fluid == net.minecraft.world.level.material.Fluids.WATER) {
            return 0xFF3F76E4;
        }
        return 0xFFFFFFFF;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }
}