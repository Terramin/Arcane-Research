package arcaneresearch.client.gui.screens;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
import arcaneresearch.common.menu.ArcaneBoreMenu;
@OnlyIn(Dist.CLIENT)
public class ArcaneBoreScreen extends AbstractContainerScreen<ArcaneBoreMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_arcanebore.png");
    public ArcaneBoreScreen(ArcaneBoreMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 175;
        this.imageHeight = 232;
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
        EntityArcaneBore bore = this.menu.getBore();
        int hp = (int) (39.0f * (bore.getHealth() / bore.getMaxHealth()));
        graphics.blit(TEXTURE, x + 68, y + 59, 192, 48, hp, 6);
        ItemStack held = bore.getMainHandItem();
        if (!held.isEmpty() && held.getDamageValue() + 1 >= held.getMaxDamage()) {
            graphics.blit(TEXTURE, x + 80, y + 29, 240, 0, 16, 16);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x + 124, y + 18, 0);
        graphics.pose().scale(0.5f, 0.5f, 1.0f);
        graphics.drawString(this.font, "Width: " + (1 + bore.getDigRadius() * 2), 0, 0, 0xFFFFFF, false);
        graphics.drawString(this.font, "Depth: " + bore.getDigDepth(), 64, 0, 0xFFFFFF, false);
        graphics.drawString(this.font, "Speed: +" + bore.getDigSpeed(Blocks.STONE.defaultBlockState()), 0, 10, 0xFFFFFF, false);
        int base = 0;
        int refining = bore.getRefining();
        int fortune = bore.getFortune();
        if (bore.hasSilkTouch() || refining > 0 || fortune > 0) {
            graphics.drawString(this.font, "Other properties:", 0, 24, 0xFFFFFF, false);
        }
        if (refining > 0) {
            graphics.drawString(this.font, "Refining " + refining, 4, 34 + base, 0xC0C0C0, false);
            base += 9;
        }
        if (fortune > 0) {
            graphics.drawString(this.font, "Fortune " + fortune, 4, 34 + base, 0xEEC64A, false);
            base += 9;
        }
        if (bore.hasSilkTouch()) {
            graphics.drawString(this.font, "Silk Touch", 4, 34 + base, 0x8080FF, false);
        }
        graphics.pose().popPose();
    }
    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
