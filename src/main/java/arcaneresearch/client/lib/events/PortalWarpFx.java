package arcaneresearch.client.lib.events;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.shaders.AbstractUniform;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.ProfileCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.invscan.mixin.GameRendererAccessor;
import arcaneresearch.invscan.mixin.PostChainAccessor;

@OnlyIn(Dist.CLIENT)
public final class PortalWarpFx {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.RENDER);
    private static final ResourceLocation SHADER = ResourceLocation.fromNamespaceAndPath(
            Arcaneresearch.MODID, "shaders/post/teleport_warp.json");
    private static final long RAMP_IN_MS = 150L;
    private static final long HOLD_MS = 900L;
    private static final long RAMP_OUT_MS = 700L;
    private static final long TOTAL_MS = RAMP_IN_MS + HOLD_MS + RAMP_OUT_MS;

    private static boolean active;
    private static long startMs;

    private PortalWarpFx() {
    }

    public static void start() {
        @NotNull Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (ModConfig.CONFIG_GRAPHICS.disableShaders) {
            return;
        }
        if (active) {
            return; // already warping, restart is unnecessary
        }
        try {
            mc.gameRenderer.loadEffect(SHADER);
            if (mc.gameRenderer.currentEffect() == null
                    || !((GameRendererAccessor) mc.gameRenderer).arcaneresearch$isEffectActive()) {
                LOGGER.warn("[Arcaneresearch] teleport warp shader could not be applied");
                active = false;
                return;
            }
            active = true;
            startMs = Util.getMillis();
        } catch (Exception e) {
            LOGGER.warn("[Arcaneresearch] failed to load teleport warp shader", e);
            active = false;
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !active) {
            return;
        }
        @NotNull Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            active = false;
            return;
        }
        PostChain chain = mc.gameRenderer.currentEffect();
        if (!(chain instanceof PostChainAccessor accessor)) {
            active = false;
            return;
        }
        long elapsed = Util.getMillis() - startMs;
        if (elapsed >= TOTAL_MS) {
            mc.gameRenderer.shutdownEffect();
            active = false;
            return;
        }
        float time = (Util.getMillis() % 1000000000L) / 1000.0F;
        float strength = strengthAt(elapsed);
        for (PostPass pass : accessor.arcaneresearch$getPasses()) {
            @NotNull EffectInstance effect = pass.getEffect();
            AbstractUniform warpTime = effect.safeGetUniform("WarpTime");
            warpTime.set(time);
            AbstractUniform warpStrength = effect.safeGetUniform("WarpStrength");
            warpStrength.set(strength);
        }
    }

    private static float strengthAt(long elapsed) {
        float value;
        if (elapsed <= RAMP_IN_MS) {
            value = elapsed / (float) RAMP_IN_MS;
        } else {
            elapsed -= RAMP_IN_MS;
            if (elapsed <= HOLD_MS) {
                value = 1.0F;
            } else {
                value = 1.0F - (elapsed - HOLD_MS) / (float) RAMP_OUT_MS;
            }
        }
        value = Math.max(0.0F, Math.min(1.0F, value));
        return value * value * (3.0F - 2.0F * value);
    }
}
