package arcaneresearch.client.fx;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FxWaveShaders {
    public static ShaderInstance portalWave;
    public static ShaderInstance playerGlitch;
    public static ShaderInstance scanRipple;
    private static RenderType playerGlitchType;

    private FxWaveShaders() {
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                "arcaneresearch:portal_wave", DefaultVertexFormat.POSITION_COLOR_TEX),
                shader -> portalWave = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "player_glitch"),
                DefaultVertexFormat.NEW_ENTITY),
                shader -> playerGlitch = shader);
        event.registerShader(new ShaderInstance(event.getResourceProvider(),
                ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_ripple"),
                DefaultVertexFormat.POSITION_COLOR_TEX),
                shader -> scanRipple = shader);
    }

    public static RenderType playerGlitchType() {
        if (playerGlitchType == null) {
            playerGlitchType = PlayerGlitchHolder.make();
        }
        return playerGlitchType;
    }

    private static final class PlayerGlitchHolder extends RenderStateShard {
        private PlayerGlitchHolder() {
            super("arcaneresearch_player_glitch_holder", () -> {
            }, () -> {
            });
        }

        private static RenderType make() {
            return RenderType.create("arcaneresearch_player_glitch",
                    DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new ShaderStateShard(() -> playerGlitch))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_WRITE)
                            .setOverlayState(OVERLAY)
                            .setLightmapState(LIGHTMAP)
                            .createCompositeState(false));
        }
    }
}
