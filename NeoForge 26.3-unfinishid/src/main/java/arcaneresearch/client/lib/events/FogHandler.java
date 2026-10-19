package arcaneresearch.client.lib.events;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod
import arcaneresearch.Arcaneresearch;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
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
        // NEOFORGE_PORT (26.x): ViewportEvent.RenderFog is not cancellable anymore —
        // NeoForge applies the distances set here directly (the Forge 1.20.1
        // "must cancel to apply" rule does not exist here).
        // Паритет оригинала: 1.12.2/1.7.10 применяли GL_EXP с плотностью fogTarget
        // (glFogi(GL_FOG_MODE, GL_EXP) + glFogf(GL_FOG_DENSITY, target)). Экспонента
        // EXP: фактор тумана = 1 - exp(-(density*dist)^2), т.е. 90% тумана на
        // dist = ln(10)/density. Линейные плоскости 1.20.1 сводим к этому срезу:
        // дальняя плоскость = z90, ближняя = 15% от неё (гладкий старт у камеры).
        // Итог на полной плотности (target=0.1): ~50% на ~8 блоках, 90% на ~15,
        // почти прозрачный в 3-5 блоках — как в TC4.
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