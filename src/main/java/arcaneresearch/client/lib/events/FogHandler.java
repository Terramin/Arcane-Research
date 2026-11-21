package arcaneresearch.client.lib.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class FogHandler {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.RENDER);
    public static boolean fogFiddled = false;
    public static float fogTarget = 0.0f;
    public static int fogDuration = 0;

    public static void tick() {
        if (!fogFiddled) {
            return;
        }
        if (fogDuration < 100) {
            fogTarget = 0.1f * (fogDuration / 100.0f);
        } else if (fogTarget < 0.1f) {
            fogTarget += 0.001f;
        }
        --fogDuration;
        if (fogDuration < 0) {
            fogFiddled = false;
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!fogFiddled || fogTarget <= 0.0f) {
            return;
        }
        
        
        
        
        
        
        
        
        
        
        float z90 = 1.5174f / Math.max(fogTarget, 0.001f);
        float far = Math.min(event.getFarPlaneDistance(), z90);
        float near = Math.min(event.getNearPlaneDistance(), far * 0.15f);
        event.setNearPlaneDistance(near);
        event.setFarPlaneDistance(far);
        LOGGER.once("render.fog", LogLevel.DEBUG,
                "Overriding fog planes: near={}, far={}, target={}", near, far, fogTarget);
    }

    public static void triggerLongFog() {
        LOGGER.debug("Triggering long fog");
        fogFiddled = true;
        fogDuration = 2400;
    }

    public static void triggerShortFog() {
        LOGGER.debug("Triggering short fog");
        fogFiddled = true;
        if (fogDuration < 200) {
            fogDuration = 200;
        }
    }
}