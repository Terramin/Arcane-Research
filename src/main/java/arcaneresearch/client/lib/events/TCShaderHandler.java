package arcaneresearch.client.lib.events;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModEffects;
import arcaneresearch.invscan.mixin.GameRendererAccessor;
import arcaneresearch.invscan.mixin.GameRendererAccessor;

@OnlyIn(Dist.CLIENT)
public final class TCShaderHandler {
    public static final int SHADER_DESAT = 0;
    public static final int SHADER_BLUR = 1;
    public static final int SHADER_HUNGER = 2;
    public static final int SHADER_SUNSCORNED = 3;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation[] SHADERS = {
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "shaders/post/desaturatetc.json"),
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "shaders/post/blurtc.json"),
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "shaders/post/hunger.json"),
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "shaders/post/sunscorned.json")
    };
    static ResourceLocation active = null;
    private static ResourceLocation failed = null;

    static boolean isBlurActive() {
        return active != null && active.getPath().endsWith("blurtc.json");
    }

    private TCShaderHandler() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.disableShaders) {
            stop(mc);
            return;
        }
        Player player = mc.player;
        if (player == null || mc.level == null || mc.gameRenderer == null) {
            stop(mc);
            return;
        }
        ResourceLocation want = desiredShader(player);
        if (want == null) {
            stop(mc);
            return;
        }
        if (want.equals(active) && isEffectAlive(mc)) {
            return;
        }
        if (want.equals(failed)) {
            return;
        }
        apply(mc, want);
    }

    private static ResourceLocation desiredShader(Player player) {
        if (player.hasEffect(ModEffects.DEATH_GAZE.get())) {
            return SHADERS[SHADER_DESAT];
        } else if (player.hasEffect(ModEffects.BLURRED_VISION.get())) {
            return SHADERS[SHADER_BLUR];
        } else if (player.hasEffect(ModEffects.SUN_SCORNED.get())) {
            return SHADERS[SHADER_SUNSCORNED];
        }
        return null;
    }

    private static boolean isEffectAlive(Minecraft mc) {
        if (mc.gameRenderer.currentEffect() == null) return false;
        try {
            return ((GameRendererAccessor) mc.gameRenderer).arcaneresearch$isEffectActive();
        } catch (Throwable t) {
            return true;
        }
    }

    private static void apply(Minecraft mc, ResourceLocation want) {
        active = null;
        failed = null;
        try {
            mc.gameRenderer.loadEffect(want);
            if (mc.gameRenderer.currentEffect() == null || !((GameRendererAccessor) mc.gameRenderer).arcaneresearch$isEffectActive()) {
                LOGGER.warn("[Arcaneresearch] post shader {} could not be applied", want);
                failed = want;
                return;
            }
            active = want;
            LOGGER.info("[Arcaneresearch] post shader on: {}", want);
        } catch (Exception e) {
            LOGGER.warn("Failed to load Arcaneresearch post shader {}", want, e);
            failed = want;
        }
    }

    private static void stop(Minecraft mc) {
        if (active != null && mc.gameRenderer != null) {
            mc.gameRenderer.shutdownEffect();
        }
        active = null;
        failed = null;
    }
}