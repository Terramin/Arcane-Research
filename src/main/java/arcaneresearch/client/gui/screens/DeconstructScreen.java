package arcaneresearch.client.gui.screens;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.menu.DeconstructMenu;
import arcaneresearch.init.ModSounds;
@OnlyIn(Dist.CLIENT)
public class DeconstructScreen extends AbstractContainerScreen<DeconstructMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Arcaneresearch.MODID, "textures/gui/gui_decontable.png");
    private static final int ASPECT_X = 64;
    private static final int ASPECT_Y = 48;
    public DeconstructScreen(DeconstructMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
    private boolean overAspect(double mx, double my) {
        double dx = mx - (leftPos + ASPECT_X);
        double dy = my - (topPos + ASPECT_Y);
        return dx >= 0.0 && dy >= 0.0 && dx < 16.0 && dy < 16.0;
    }
    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mx, int my) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.breaktime() > 0) {
            int scaled = menu.breaktimeScaled(46);
            graphics.blit(TEXTURE, leftPos + 93, topPos + 15 + 46 - scaled,
                    176, 46 - scaled, 9, scaled, 256, 256);
        }
        Aspect aspect = menu.currentAspect();
        if (aspect != null) {
            AspectRenderer.drawAspect(graphics, leftPos + ASPECT_X, topPos + ASPECT_Y, aspect);
        }
    }
    @Override
    protected void renderLabels(GuiGraphics graphics, int mx, int my) {
    }
    @Override
    public void render(GuiGraphics graphics, int mx, int my, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mx, my, partialTick);
        renderTooltip(graphics, mx, my);
        Aspect aspect = menu.currentAspect();
        if (aspect != null && overAspect(mx, my)) {
            graphics.renderTooltip(font,
                    List.of(Component.literal(aspect.getName()),
                            Component.literal(aspect.getLocalizedDescription())),
                    Optional.empty(), mx, my - 8);
        }
    }
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (menu.currentAspect() != null && overAspect(mx, my)
                && minecraft != null && minecraft.gameMode != null && minecraft.level != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, DeconstructMenu.BUTTON_COLLECT);
            Player player = minecraft.player;
            if (player != null && ModSounds.HH_OFF.get() != null) {
                minecraft.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        ModSounds.HH_OFF.get(), SoundSource.PLAYERS, 0.2F,
                        1.0F + minecraft.level.random.nextFloat() * 0.1F, false);
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
