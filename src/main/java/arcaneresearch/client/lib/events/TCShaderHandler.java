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
@OnlyIn(Dist.CLIENT)
public final class TCShaderHandler {
    public static final int SHADER_DESAT = 0;
    public static final int SHADER_BLUR = 1;
    public static final int SHADER_HUNGER = 2;
    public static final int SHADER_SUNSCORNED = 3;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation[] SHADERS = {
            new ResourceLocation(Arcaneresearch.MODID, "shaders/post/desaturatetc.json"),
            new ResourceLocation(Arcaneresearch.MODID, "shaders/post/blurtc.json"),
            new ResourceLocation(Arcaneresearch.MODID, "shaders/post/hunger.json"),
            new ResourceLocation(Arcaneresearch.MODID, "shaders/post/sunscorned.json")
    };
    private static ResourceLocation active = null;
    private TCShaderHandler() {
    }
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.disableShaders) {
            if (active != null) {
                mc.gameRenderer.shutdownEffect();
                active = null;
            }
            return;
        }
        Player player = mc.player;
        if (player == null || mc.level == null || mc.gameRenderer == null) {
            if (active != null) {
                mc.gameRenderer.shutdownEffect();
                active = null;
            }
            return;
        }
        ResourceLocation want = null;
        if (player.hasEffect(ModEffects.DEATH_GAZE.get())) {
            want = SHADERS[SHADER_DESAT];
        } else if (player.hasEffect(ModEffects.BLURRED_VISION.get())) {
            want = SHADERS[SHADER_BLUR];
        } else if (player.hasEffect(ModEffects.UNNATURAL_HUNGER.get())) {
            want = SHADERS[SHADER_HUNGER];
        } else if (player.hasEffect(ModEffects.SUN_SCORNED.get())) {
            want = SHADERS[SHADER_SUNSCORNED];
        }
        if (want == null ? active != null : !want.equals(active)) {
            if (want == null) {
                LOGGER.info("[Arcaneresearch] post shader off (was {})", active);
                mc.gameRenderer.shutdownEffect();
            } else {
                try {
                    mc.gameRenderer.loadEffect(want);
                    if (mc.gameRenderer.currentEffect() == null) {
                        LOGGER.warn("[Arcaneresearch] post shader {} loaded nothing, staying off", want);
                        want = null;
                    } else {
                        LOGGER.info("[Arcaneresearch] post shader on: {}", want);
                        try {
                            net.minecraft.client.renderer.PostChain chain =
                                    mc.gameRenderer.currentEffect();
                            com.mojang.blaze3d.pipeline.RenderTarget main =
                                    mc.getMainRenderTarget();
                            com.mojang.blaze3d.pipeline.RenderTarget swap = null;
                            try {
                                swap = chain.getTempTarget("swap");
                            } catch (Exception ignored) {
                            }
                            LOGGER.info("[Arcaneresearch] post chain '{}': main={}x{}, swap={}x{}",
                                    chain.getName(), main.width, main.height,
                                    swap == null ? -1 : swap.width,
                                    swap == null ? -1 : swap.height);
                        } catch (Exception e) {
                            LOGGER.warn("[Arcaneresearch] post chain introspection failed", e);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to load Arcaneresearch post shader {}", want, e);
                    want = null;
                }
            }
            active = want;
        }
    }
}
