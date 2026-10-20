package arcaneresearch.client.lib.events;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FogHandler {
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
        event.setCanceled(true);
        float z90 = 1.5174f / Math.max(fogTarget, 0.001f);
        float far = Math.min(event.getFarPlaneDistance(), z90);
        float near = Math.min(event.getNearPlaneDistance(), far * 0.15f);
        event.setNearPlaneDistance(near);
        event.setFarPlaneDistance(far);
    }
    public static void triggerLongFog() {
        fogFiddled = true;
        fogDuration = 2400;
    }
    public static void triggerShortFog() {
        fogFiddled = true;
        if (fogDuration < 200) {
            fogDuration = 200;
        }
    }
}
