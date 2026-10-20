package arcaneresearch.client.gui.screens;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.menu.DeconstructMenu;
import arcaneresearch.init.ModSounds;

/**
 * Порт {@code thaumcraft.client.screen.DeconstructionTableScreen} (reforged):
 * фон {@code gui_decontable.png}, вертикальная полоса прогресса,
 * выпавший аспект (клик — забрать, паритет TC4), тултип аспекта.
 * Без подписей (паритет reforged).
 *
 * <p>NEOFORGE_PORT (Forge e45d90b dissolved temp.decon ->
 * client/gui/screens/DeconstructScreen.java, typed to DeconstructMenu):
 * Identifier -&gt; Identifier.fromNamespaceAndPath;
 * GuiGraphics -&gt; GuiGraphicsExtractor; blit с RenderPipelines.GUI_TEXTURED;
 * render()/renderLabels -&gt; extractBackground()/extractLabels (тултипы через
 * setComponentTooltipForNextFrame); mouseClicked(MouseButtonEvent, boolean);
 * level.random -&gt; getRandom(); размеры 176x166 в конструктор.</p>
 */
@OnlyIn(Dist.CLIENT)
public class DeconstructScreen extends AbstractContainerScreen<DeconstructMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Arcaneresearch.MODID, "textures/gui/gui_decontable.png");
    private static final int ASPECT_X = 64;
    private static final int ASPECT_Y = 48;

    public DeconstructScreen(DeconstructMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    private boolean overAspect(double mx, double my) {
        double dx = mx - (leftPos + ASPECT_X);
        double dy = my - (topPos + ASPECT_Y);
        return dx >= 0.0 && dy >= 0.0 && dx < 16.0 && dy < 16.0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mx, int my, float partialTick) {
        super.extractBackground(graphics, mx, my, partialTick);
        // NEOFORGE_PORT (26.3): no extra dim - super.extractBackground already draws the in-game gradient (was drawn twice).
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0,
                imageWidth, imageHeight, 256, 256);
        if (menu.breaktime() > 0) {
            int scaled = menu.breaktimeScaled(46);
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos + 93, topPos + 15 + 46 - scaled,
                    176, 46 - scaled, 9, scaled, 256, 256);
        }
        Aspect aspect = menu.currentAspect();
        if (aspect != null) {
            AspectRenderer.drawAspect(graphics, leftPos + ASPECT_X, topPos + ASPECT_Y, aspect);
            if (overAspect(mx, my)) {
                graphics.setComponentTooltipForNextFrame(font,
                        List.of(Component.literal(aspect.getName()),
                                Component.literal(aspect.getLocalizedDescription())),
                        mx, my - 8);
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mx, int my) {
    }

    /**
     * Паритет TC4 {@code mouseClicked}: клик ЛЮБОЙ кнопкой по аспекту
     * забирает его + {@code playButtonAspect} ({@code hhoff}).
     */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        boolean result;
        if (menu.currentAspect() != null && overAspect(mx, my)
                && minecraft != null && minecraft.gameMode != null && minecraft.level != null) {
            // Forge order: collect first (before slot interaction in super).
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, DeconstructMenu.BUTTON_COLLECT);
            Player player = minecraft.player;
            if (player != null && ModSounds.HH_OFF.get() != null) {
                minecraft.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        ModSounds.HH_OFF.get(), SoundSource.PLAYERS, 0.2F,
                        1.0F + minecraft.level.getRandom().nextFloat() * 0.1F, false);
            }
        }
        result = super.mouseClicked(event, doubleClick);
        return result;
    }
}
