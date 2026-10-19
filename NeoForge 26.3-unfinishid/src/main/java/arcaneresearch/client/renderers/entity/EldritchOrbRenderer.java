package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.projectile.EntityEldritchOrb;

// NEOFORGE_PORT (26.3): tendril fan + billboard sprite via submitCustomGeometry.
// Seeded-random rotation sequence preserved 1:1 (each tendril gets its own
// emission with the pose baked eagerly, since snapshots are per-submit).
public class EldritchOrbRenderer extends EntityRenderer<EntityEldritchOrb, EldritchOrbRenderer.State> {

    public static class State extends EntityRenderState {
        public float age;
        public float growScale;
        public int frame;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");

    private final RandomSource random = RandomSource.create();

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityEldritchOrb entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.age = entity.tickCount + partialTicks;
        state.growScale = Math.min(entity.tickCount, 10) / 10.0F;
        state.frame = entity.tickCount % 13;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        random.setSeed(187L);
        float age = state.age;
        float scale = state.growScale;

        RenderType tendrils = TCRenderTypes.orbTendrils();
        float spin = age / 80.0F * 360.0F;
        for (int i = 0; i < 12; i++) {
            float rx1 = random.nextFloat() * 360.0F;
            float ry1 = random.nextFloat() * 360.0F;
            float rz1 = random.nextFloat() * 360.0F;
            float rx2 = random.nextFloat() * 360.0F;
            float ry2 = random.nextFloat() * 360.0F;
            float rz2 = random.nextFloat() * 360.0F + spin;
            float length = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * scale;
            float width = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * scale;

            poseStack.pushPose();
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(rx1));
            poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(ry1));
            poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(rz1));
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(rx2));
            poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(ry2));
            poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(rz2));
            collector.submitCustomGeometry(poseStack, tendrils,
                    (pose, consumer) -> renderTendril(pose, consumer, length, width));
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        RenderType sprite = RenderTypes.entityTranslucentEmissive(TEXTURE);
        int frame = state.frame;
        collector.submitCustomGeometry(poseStack, sprite,
                (pose, consumer) -> renderSprite(pose, consumer, frame));
        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }

    private static void renderTendril(PoseStack.Pose pose, VertexConsumer consumer, float length, float width) {
        consumer.addVertex(pose, 0, 0, 0).setColor(255, 255, 255, 255);
        consumer.addVertex(pose, (float) (-0.866 * width), length, -0.5F * width).setColor(64, 64, 64, 255);
        consumer.addVertex(pose, (float) (0.866 * width), length, -0.5F * width).setColor(64, 64, 64, 255);
        consumer.addVertex(pose, 0, length, 1.0F * width).setColor(64, 64, 64, 255);
        consumer.addVertex(pose, (float) (-0.866 * width), length, -0.5F * width).setColor(64, 64, 64, 255);
    }

    private static void renderSprite(PoseStack.Pose pose, VertexConsumer consumer, int frame) {
        float u0 = frame / 64.0F;
        float u1 = u0 + 1.0F / 64.0F;
        float v0 = 3.0F / 64.0F;
        float v1 = v0 + 1.0F / 64.0F;
        float size = 0.5F;
        quad(consumer, pose, -size, -size, u0, v1);
        quad(consumer, pose, size, -size, u1, v1);
        quad(consumer, pose, size, size, u1, v0);
        quad(consumer, pose, -size, size, u0, v0);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
    }
}