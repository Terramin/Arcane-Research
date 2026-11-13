package arcaneresearch.temp.portal;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/**
 * Эффект экрана портала: полностью прозрачный квад в плоскости портала, из центра
 * которого раз в секунду расходится кольцевая волна. Волна искажает фон (как линза
 * у чёрной дыры) только по краям кольца.
 *
 * Механика: раз в кадр (до отрисовки портала) цветовой буфер основного прохода
 * копируется в текстуру; шейдер portal_wave сэмплирует её со смещением вдоль
 * радиального направления у гребня волны — так "искажается" уже нарисованное
 * пространство. Между волнами экран полностью прозрачен.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PortalWaveFx {

    /** Доступ к protected-состояниям RenderStateShard для сборки собственного RenderType. */
    private static class Shards extends RenderStateShard {
        private Shards() {
            super("portal_wave_shards", () -> {
            }, () -> {
            });
        }

        static final ShaderStateShard SHADER = new ShaderStateShard(() -> PortalWaveShaders.portalWave);

        /** Текстура-шард: биндит копию цветового буфера (не ResourceLocation). */
        static final EmptyTextureStateShard BACKGROUND = new EmptyTextureStateShard(() -> {
            RenderSystem.setShaderTexture(0, getBackgroundTextureId());
        }, () -> {
        });

        static final TransparencyStateShard TRANSLUCENT = TRANSLUCENT_TRANSPARENCY;
        static final CullStateShard SHARD_NO_CULL = NO_CULL;
        /** Без записи глубины: волна не должна перекрывать то, что рисуется после неё. */
        static final WriteMaskStateShard COLOR_ONLY = COLOR_WRITE;
    }

    private static final RenderType WAVE_TYPE = RenderType.create(
            "portal_wave", DefaultVertexFormat.POSITION_COLOR_TEX, VertexFormat.Mode.QUADS,
            256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(Shards.SHADER)
                    .setTextureState(Shards.BACKGROUND)
                    .setTransparencyState(Shards.TRANSLUCENT)
                    .setCullState(Shards.SHARD_NO_CULL)
                    .setWriteMaskState(Shards.COLOR_ONLY)
                    .createCompositeState(false));

    /** Копия цветового буфера основного прохода (id GL-текстуры, -1 — нет). */
    private static int backgroundTexture = -1;
    private static int backgroundWidth;
    private static int backgroundHeight;
    /** Скопирован ли фон в этом кадре (несколько порталов — одна копия). */
    private static boolean copiedThisFrame;

    private PortalWaveFx() {
    }

    /** Эффект активен: шейдер загружен и график не Fabulous (там вложенные фреймбуферы). */
    static boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        return PortalWaveShaders.portalWave != null
                && mc.level != null
                && mc.options.graphicsMode().get() != GraphicsStatus.FABULOUS;
    }

    static RenderType getWaveType() {
        return WAVE_TYPE;
    }

    /** GL-id текстуры с копией фона (0 — нет). */
    static int getBackgroundTextureId() {
        return backgroundTexture == -1 ? 0 : backgroundTexture;
    }

    /** Ленивое копирование фона: один раз за кадр, до отрисовки первого портала. */
    static void ensureBackgroundCopied() {
        if (copiedThisFrame) {
            return;
        }
        copiedThisFrame = true;
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        if (main == null || main.width <= 0 || main.height <= 0) {
            return;
        }
        if (backgroundTexture == -1 || backgroundWidth != main.width || backgroundHeight != main.height) {
            createTexture(main.width, main.height);
        }
        // основной фреймбуфер сейчас привязан и на чтение, и на запись — копируем из него
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
        // clamp: UV со смещением может выйти за границы кадра
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        backgroundWidth = width;
        backgroundHeight = height;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        copiedThisFrame = false;
        updateUniforms();
    }

    /** Глобальные uniforms эффекта (не зависят от портала). */
    private static void updateUniforms() {
        ShaderInstance shader = PortalWaveShaders.portalWave;
        if (shader == null) {
            return;
        }
        Uniform time = shader.getUniform("Time");
        if (time != null) {
            // секунды; fract в шейдере задаёт период волны
            time.set((float) (Util.getMillis() % 1000000L) / 1000.0F);
        }
        Uniform screenSize = shader.getUniform("ScreenSize");
        if (screenSize != null) {
            Window window = Minecraft.getInstance().getWindow();
            screenSize.set((float) window.getWidth(), (float) window.getHeight());
        }
    }
}
