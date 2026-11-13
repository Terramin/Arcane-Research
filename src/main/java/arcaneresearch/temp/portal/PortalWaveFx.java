package arcaneresearch.temp.portal;

import arcaneresearch.Arcaneresearch;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/**
 * Эффект экрана портала: полностью прозрачный квад в плоскости портала, из центра
 * которого раз в секунду расходится кольцевая волна, искажающая фон только по краям
 * кольца (как линза у чёрной дыры).
 *
 * Волна рисуется на поздней стадии кадра (после частиц): к этому моменту
 * фреймбуфер содержит ВЕСЬ мир (блоки, сущности, блочные сущности, воду, частицы),
 * поэтому копия фона, снятая прямо перед отрисовкой, искажает любые объекты
 * позади портала — включая те, что рисуются после сущностей (кровати,
 * тайловые сущности, эффекты и т.д.).
 *
 * Механика: раз в кадр цветовой буфер копируется в текстуру; шейдер portal_wave
 * сэмплирует её со смещением вдоль радиального направления у гребня волны.
 * Между волнами и вне кольца экран полностью прозрачен.
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

    /** Квад волны выталкивается к камере, если плоскость портала ближе этого к камере
     *  (больше near-plane главной камеры 0.05 — иначе квад срезается и волна пропадает). */
    private static final double QUAD_PUSH_MARGIN = 0.1D;
    /** Шаг проекции для перспективного масштаба искажения (мировые единицы). */
    private static final float SCALE_PROBE_STEP = 0.25F;
    /** Мировая единица = столько NDC; упаковка масштаба в байт цвета (см. шейдер). */
    private static final float MAX_SCALE_PACKED = 2.0F;

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

    /** GL-id текстуры с копией фона (0 — нет). */
    private static int getBackgroundTextureId() {
        return backgroundTexture == -1 ? 0 : backgroundTexture;
    }

    /** Ленивое копирование фона: один раз за кадр, до отрисовки первой волны. */
    private static void ensureBackgroundCopied() {
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
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            copiedThisFrame = false;
            updateUniforms();
            return;
        }
        // рисуем волны после частиц: в буфере уже весь мир, копия фона полная
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || !isActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ensureBackgroundCopied();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof PortalEntity portal) {
                drawWave(portal, event.getPartialTick(), event.getCamera(), bufferSource);
            }
        }
        bufferSource.endBatch(WAVE_TYPE);
    }

    /**
     * Квад волны в плоскости портала (границы — на 1px уже золотой полосы рамки,
     * пересчёт по анимации открытия). Квад полностью прозрачный: рисуется только
     * кольцо волны. UV хранит локальные координаты от центра волны (мировые
     * единицы), цвет — экранные направление и перспективный масштаб искажения
     * для этого угла (шейдер сэмплирует копию фона со смещением).
     */
    private static void drawWave(PortalEntity entity, float partialTicks, Camera camera,
                                 MultiBufferSource.BufferSource bufferSource) {
        double[] contour = entity.getContourBounds(partialTicks);
        double baseY = Mth.lerp((double) partialTicks, entity.yo, entity.getY());
        double halfWidth = contour[2] - 1.0 / 16.0; // на 1px уже золотой полосы
        double y0 = contour[1] - baseY;
        double y1 = contour[0] - baseY;
        if (y1 - y0 < 0.05D) {
            return; // портал ещё не раскрылся
        }
        double centerY = (y0 + y1) * 0.5;
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());

        Vec3 camPos = camera.getPosition();
        Vec3 pos = entity.getPortalPlanePos(partialTicks);

        PoseStack poseStack = new PoseStack();
        // тот же поворот камеры, что в мировом poseStack LevelRenderer (в проходе
        // сущностей он уже входит в CPU-трансформ вершин; ModelViewMat = identity)
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        poseStack.translate(
                (float) (pos.x - camPos.x),
                (float) (pos.y - camPos.y),
                (float) (pos.z - camPos.z));
        // тот же поворот, что и у модели портала (180 - yaw)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f - yaw));
        // выталкивание квада к камере у near-plane (позиция квада не влияет на UV/цвет)
        pushQuadTowardCamera(entity, partialTicks, poseStack);

        // мировые координаты -> clip (проекция + поворот камеры + перенос)
        PoseStack projViewPose = new PoseStack();
        projViewPose.mulPoseMatrix(new Matrix4f(RenderSystem.getProjectionMatrix()));
        projViewPose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        projViewPose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        projViewPose.translate((float) -camPos.x, (float) -camPos.y, (float) -camPos.z);
        Matrix4f projView = projViewPose.last().pose();

        // мировые направления -> пространство камеры (только поворот)
        PoseStack worldToCamPose = new PoseStack();
        worldToCamPose.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        worldToCamPose.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
        Matrix4f worldToCam = worldToCamPose.last().pose();

        // поворот портала: локальные координаты -> мировые
        Matrix4f portalRot = new Matrix4f().rotationY((float) Math.toRadians(180f - yaw));

        VertexConsumer buf = bufferSource.getBuffer(WAVE_TYPE);
        Matrix4f matrix = poseStack.last().pose();

        float fx0 = (float) -halfWidth;
        float fx1 = (float) halfWidth;
        float fy0 = (float) y0;
        float fy1 = (float) y1;
        float[][] corners = {
                {fx0, fy0},
                {fx1, fy0},
                {fx1, fy1},
                {fx0, fy1}
        };
        for (float[] corner : corners) {
            // радиальный луч из центра волны (в плоскости портала, мировые единицы)
            float rx = corner[0];
            float ry = corner[1] - (float) centerY;
            float len = (float) Math.sqrt(rx * rx + ry * ry);
            Vector3f localDir = len > 1.0E-4F ? new Vector3f(rx / len, ry / len, 0f) : new Vector3f(0f, 1f, 0f);

            Vector3f worldDir = new Vector3f(localDir);
            portalRot.transformPosition(worldDir);

            // направление луча на экране (NDC)
            Vector3f camDir = new Vector3f(worldDir);
            worldToCam.transformDirection(camDir);
            Vector4f clipDir = new Vector4f(camDir.x, camDir.y, camDir.z, 0f);
            projView.transform(clipDir);
            float dirX = 0f;
            float dirY = 1f;
            if (Math.abs(clipDir.w) > 1.0E-4F) {
                dirX = clipDir.x / clipDir.w;
                dirY = clipDir.y / clipDir.w;
                float l = (float) Math.sqrt(dirX * dirX + dirY * dirY);
                if (l > 1.0E-5F) {
                    dirX /= l;
                    dirY /= l;
                } else {
                    dirX = 0f;
                    dirY = 1f;
                }
            }

            // перспективный масштаб: NDC-сдвиг на мировую единицу у этого угла
            Vector3f cornerWorld = new Vector3f(corner[0], corner[1], 0f);
            portalRot.transformPosition(cornerWorld);
            Vector3f p0 = new Vector3f(
                    (float) pos.x + cornerWorld.x,
                    (float) pos.y + cornerWorld.y,
                    (float) pos.z + cornerWorld.z);
            Vector3f p1 = new Vector3f(p0).add(
                    worldDir.x * SCALE_PROBE_STEP,
                    worldDir.y * SCALE_PROBE_STEP,
                    worldDir.z * SCALE_PROBE_STEP);
            Vector4f clipP0 = new Vector4f(p0.x, p0.y, p0.z, 1f);
            Vector4f clipP1 = new Vector4f(p1.x, p1.y, p1.z, 1f);
            projView.transform(clipP0);
            projView.transform(clipP1);
            float scale = 0f;
            if (clipP0.w > 1.0E-4F && clipP1.w > 1.0E-4F) {
                float ndcDx = clipP1.x / clipP1.w - clipP0.x / clipP0.w;
                float ndcDy = clipP1.y / clipP1.w - clipP0.y / clipP0.w;
                scale = (float) Math.sqrt(ndcDx * ndcDx + ndcDy * ndcDy) / SCALE_PROBE_STEP;
            }

            buf.vertex(matrix, corner[0], corner[1], 0f)
                    .color(dirX * 0.5f + 0.5f, dirY * 0.5f + 0.5f,
                            Mth.clamp(scale / MAX_SCALE_PACKED, 0f, 1f), 1f)
                    .uv(corner[0], corner[1] - (float) centerY)
                    .endVertex();
        }
    }

    /**
     * Сдвиг квада к камере, когда плоскость портала ближе QUAD_PUSH_MARGIN:
     * near-plane главной камеры (0.05) вплотную срезает квад в плоскости портала.
     * Сдвиг по локальной Z pose (после поворота 180-yaw локальный +Z направлен
     * ПРОТИВ нормали портала). На UV/цвет волны не влияет.
     */
    private static void pushQuadTowardCamera(PortalEntity entity, float partialTicks, PoseStack poseStack) {
        Vec3 normal = entity.getFacingNormal(partialTicks);
        double along = cameraPos().subtract(entity.getPortalPlanePos(partialTicks)).dot(normal);
        double absAlong = Math.abs(along);
        if (absAlong >= QUAD_PUSH_MARGIN) {
            return;
        }
        double push = (QUAD_PUSH_MARGIN - absAlong) * (along >= 0.0D ? 1.0D : -1.0D);
        // локальная Z = -нормаль, значит сдвиг "к камере" (по нормали) — это локальный -Z * sign
        poseStack.translate(0.0F, 0.0F, (float) (-push));
    }

    private static Vec3 cameraPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
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
