package arcaneresearch.client.fx;

import arcaneresearch.Arcaneresearch;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** GUI layers that draw the ported screen-space effects. */
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class ArcaneFxLayers {

    private static final Identifier PORTAL_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/portal.png");
    private static final Identifier BLANK_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/blank.png");

    public static final GuiLayer PORTAL_WAVE_LAYER = (graphics, deltaTracker) -> {
        if (!PortalWaveFx.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        long elapsed = PortalWaveFx.elapsedNanos();
        float progress = Math.min(1.0F, elapsed / 1_500_000_000.0F);
        int alpha = ARGB.colorFromFloat(0.75F * (1.0F - progress), 1.0F, 1.0F, 1.0F);
        float u = (elapsed / 40_000_000.0F) % 1.0F;
        graphics.blit(ArcaneFxPipelines.PORTAL_WAVE, PORTAL_TEXTURE, 0, 0, u, 0.0F,
                w, h, 256, 256, alpha);
    };

    public static final GuiLayer SCAN_RIPPLE_LAYER = (graphics, deltaTracker) -> {
        if (!ScanRippleFx.isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        long elapsed = ScanRippleFx.elapsedNanos();
        float progress = Math.min(1.0F, elapsed / 1_000_000_000.0F);
        int alpha = ARGB.colorFromFloat(0.55F * (1.0F - progress), 0.35F, 0.85F, 1.0F);
        int radius = (int) (Math.min(w, h) * 0.45F * progress) + 2;
        int cx = w / 2;
        int cy = h / 2;
        int thickness = 3;
        graphics.blit(ArcaneFxPipelines.SCAN_RIPPLE, BLANK_TEXTURE,
                cx - radius, cy - radius, 0.0F, 0.0F, radius * 2, thickness, 1, 1, alpha);
        graphics.blit(ArcaneFxPipelines.SCAN_RIPPLE, BLANK_TEXTURE,
                cx - radius, cy + radius, 0.0F, 0.0F, radius * 2, thickness, 1, 1, alpha);
        graphics.blit(ArcaneFxPipelines.SCAN_RIPPLE, BLANK_TEXTURE,
                cx - radius, cy - radius, 0.0F, 0.0F, thickness, radius * 2, 1, 1, alpha);
        graphics.blit(ArcaneFxPipelines.SCAN_RIPPLE, BLANK_TEXTURE,
                cx + radius, cy - radius, 0.0F, 0.0F, thickness, radius * 2, 1, 1, alpha);
    };

    private ArcaneFxLayers() {
    }

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "portal_wave"), PORTAL_WAVE_LAYER);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_ripple"), SCAN_RIPPLE_LAYER);
    }
}
