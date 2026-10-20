package arcaneresearch.client.gui.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor; // NEOFORGE_PORT: was GuiGraphicsExtractor
import net.minecraft.client.renderer.RenderPipelines; // NEOFORGE_PORT: blit needs a pipeline
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
import arcaneresearch.common.menu.ArcaneBoreMenu;

public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/gui/gui_arcanebore.png");

    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 175, 232);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

        EntityArcaneBore bore = this.menu.getBore();

        int hp = (int) (39.0f * (bore.getHealth() / bore.getMaxHealth()));
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 68, y + 59, 192, 48, hp, 6, 256, 256);

        ItemStack held = bore.getMainHandItem();
        if (!held.isEmpty() && held.getDamageValue() + 1 >= held.getMaxDamage()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 80, y + 29, 240, 0, 16, 16, 256, 256);
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 124, y + 18);
        graphics.pose().scale(0.5f, 0.5f);
        graphics.text(this.font, "Width: " + (1 + bore.getDigRadius() * 2), 0, 0, 0xFFFFFFFF, false);
        graphics.text(this.font, "Depth: " + bore.getDigDepth(), 64, 0, 0xFFFFFFFF, false);
        graphics.text(this.font, "Speed: +" + bore.getDigSpeed(Blocks.STONE.defaultBlockState()), 0, 10, 0xFFFFFFFF, false);
        int base = 0;
        int refining = bore.getRefining();
        int fortune = bore.getFortune();
        if (bore.hasSilkTouch() || refining > 0 || fortune > 0) {
            graphics.text(this.font, "Other properties:", 0, 24, 0xFFFFFFFF, false);
        }
        if (refining > 0) {

            graphics.text(this.font, "Refining " + refining, 4, 34 + base, 0xFFC0C0C0, false);
            base += 9;
        }
        if (fortune > 0) {

            graphics.text(this.font, "Fortune " + fortune, 4, 34 + base, 0xFFEEC64A, false);
            base += 9;
        }
        if (bore.hasSilkTouch()) {

            graphics.text(this.font, "Silk Touch", 4, 34 + base, 0xFF8080FF, false);
        }
        graphics.pose().popMatrix();
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {

    }
}