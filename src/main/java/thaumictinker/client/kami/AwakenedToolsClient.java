package thaumictinker.client.kami;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.TtCore;
import thaumictinker.kami.tool.AwakenedTool;
import thaumictinker.kami.tool.SoulHeartHandler;
import thaumictinker.kami.tool.ToolHandler;

 



 
@Mod.EventBusSubscriber(modid = TtCore.MODID, value = Dist.CLIENT)
public final class AwakenedToolsClient {

    private static final ResourceLocation SOUL_HEARTS =
            new ResourceLocation("thaumictinker", "textures/gui/soulhearts.png");
    private static final int TOOLTIP_TICKS = 400;
    private static final int TEXTURE_SIZE = 16;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SPACING = 8;
    private static final int HALF_HEART_WIDTH = 7;

    private static Component tooltip;
    private static int tooltipTicks;
    private static int elapsedTicks;
    private static Item lastItem;
    private static int lastMode;

    private AwakenedToolsClient() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        elapsedTicks++;
        if (tooltipTicks > 0) {
            tooltipTicks--;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            lastItem = null;
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof AwakenedTool tool)) {
            lastItem = null;
            return;
        }
        int mode = ToolHandler.getMode(held);
        if (lastItem == held.getItem() && lastMode != mode) {
            setTooltip(ToolHandler.modeName(tool.kind(), mode));
        }
        lastItem = held.getItem();
        lastMode = mode;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        ResourceLocation overlay = event.getOverlay().id();
        if (VanillaGuiOverlay.HOTBAR.id().equals(overlay)) {
            renderToolMode(event.getGuiGraphics());
        } else if (VanillaGuiOverlay.FOOD_LEVEL.id().equals(overlay)) {
            renderSoulHearts(event.getGuiGraphics());
        }
    }

    private static void setTooltip(Component text) {
        tooltip = text;
        tooltipTicks = TOOLTIP_TICKS;
    }

    private static void renderToolMode(GuiGraphics graphics) {
        if (tooltipTicks <= 0 || tooltip == null) {
            return;
        }
        int opacity = Math.min((int) (tooltipTicks * 256.0F / 10.0F), 160);
        if (opacity <= 0) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int x = (width - font.width(tooltip)) / 2;
        int y = height - 72;
        float hue = Mth.frac((float) Math.cos(elapsedTicks / 250.0D));
        int rgb = Mth.hsvToRgb(hue, 0.6F, 1.0F);
        int color = (opacity << 24) | (rgb & 0xFFFFFF);
        graphics.drawString(font, tooltip, x, y, color, true);
    }

    private static void renderSoulHearts(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        int hearts = SoulHeartHandler.getClientHP();
        if (hearts <= 0) {
            return;
        }
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int x = width / 2 + 10;
        int y = height - 29;
        int drawn = 0;
        for (int index = 0; index < hearts; index++) {
            boolean half = index == hearts - 1 && hearts % 2 != 0;
            if (half || index % 2 == 0) {
                int left = x + drawn * HEART_SPACING;
                if (half) {
                    graphics.blit(SOUL_HEARTS, left, y, HEART_SIZE, 0, HALF_HEART_WIDTH, HEART_SIZE,
                            TEXTURE_SIZE, TEXTURE_SIZE);
                } else {
                    graphics.blit(SOUL_HEARTS, left, y, 0, 0, HEART_SIZE, HEART_SIZE,
                            TEXTURE_SIZE, TEXTURE_SIZE);
                }
                drawn++;
            }
        }
    }
}
