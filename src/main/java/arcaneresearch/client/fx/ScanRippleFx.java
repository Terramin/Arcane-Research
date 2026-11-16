package arcaneresearch.client.fx;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ScanRippleFx {

    public static final float END = 0.85F;
    private static final long DURATION_MS = 900L;

    private static final ResourceLocation SCANSCREEN =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/item/scanscreen.png");

    private static long startMs = -1L;

    private static int backgroundTexture = -1;
    private static int backgroundWidth;
    private static int backgroundHeight;
    private static boolean copiedThisFrame;

    private ScanRippleFx() {
    }

    public static void trigger() {
        startMs = Util.getMillis();
    }

    public static boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return startMs >= 0L
                && Util.getMillis() - startMs < DURATION_MS
                && mc.level != null
                && FxWaveShaders.scanRipple != null
                && mc.options.graphicsMode().get() != GraphicsStatus.FABULOUS;
    }

    private static float progress() {
        float p = Math.min((Util.getMillis() - startMs) / (float) DURATION_MS, 1.0F);
        return END * (p * p * (3.0F - 2.0F * p));
    }

    private static class Shards extends RenderStateShard {
        private Shards() {
            super("scan_ripple_shards", () -> {
            }, () -> {
            });
        }

        static final ShaderStateShard SHADER = new ShaderStateShard(() -> FxWaveShaders.scanRipple);

        static final EmptyTextureStateShard BACKGROUND = new EmptyTextureStateShard(() -> {

            RenderSystem.setShaderTexture(0, backgroundTexture == -1 ? 0 : backgroundTexture);
            RenderSystem.setShaderTexture(1, SCANSCREEN);
        }, () -> {
        });

        static final TransparencyStateShard TRANSLUCENT = TRANSLUCENT_TRANSPARENCY;
        static final CullStateShard SHARD_NO_CULL = NO_CULL;
        static final WriteMaskStateShard COLOR_ONLY = COLOR_WRITE;
    }

    private static final RenderType TYPE = RenderType.create(
            "scan_ripple", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shards.SHADER)
                    .setTextureState(Shards.BACKGROUND)
                    .setTransparencyState(Shards.TRANSLUCENT)
                    .setCullState(Shards.SHARD_NO_CULL)
                    .setWriteMaskState(Shards.COLOR_ONLY)
                    .createCompositeState(false));

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            copiedThisFrame = false;
            ShaderInstance shader = FxWaveShaders.scanRipple;
            if (shader != null) {
                Window window = Minecraft.getInstance().getWindow();
                shader.safeGetUniform("ScreenSize").set((float) window.getWidth(), (float) window.getHeight());
            }
        }
    }

    private static void ensureBackgroundCopied() {
        if (copiedThisFrame) {
            return;
        }
        copiedThisFrame = true;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        if (main.width <= 0 || main.height <= 0) {
            return;
        }
        if (backgroundTexture == -1 || backgroundWidth != main.width || backgroundHeight != main.height) {
            createTexture(main.width, main.height);
        }

        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(backgroundTexture);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, main.width, main.height);
    }

    private static void createTexture(int width, int height) {
        if (backgroundTexture != -1) {
            GlStateManager._deleteTexture(backgroundTexture);
        }
        backgroundTexture = GL11.glGenTextures();
        GlStateManager._activeTexture(GL13.GL_TEXTURE0);
        GlStateManager._bindTexture(backgroundTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        backgroundWidth = width;
        backgroundHeight = height;
    }

    public static boolean drawScreen(PoseStack pose, MultiBufferSource buffer,
                                     float x0, float y0, float x1, float y1, float z) {
        if (!active()) {
            return false;
        }
        ensureBackgroundCopied();
        VertexConsumer vc = buffer.getBuffer(TYPE);

        FxWaveShaders.scanRipple.safeGetUniform("Progress").set(progress());
        var m = pose.last().pose();
        vc.vertex(m, x0, y1, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(0.0F, 1.0F).endVertex();
        vc.vertex(m, x1, y1, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(1.0F, 1.0F).endVertex();
        vc.vertex(m, x1, y0, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(1.0F, 0.0F).endVertex();
        vc.vertex(m, x0, y0, z).color(1.0F, 1.0F, 1.0F, 1.0F).uv(0.0F, 0.0F).endVertex();
        return true;
    }
}
