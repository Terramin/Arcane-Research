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
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (26.3): camera-facing spinning tinted billboard (Forge body
// preserved); emissive variants fullbright, others use entity light.
public class ArcaneresearchProjectileRenderer<T extends Entity> extends EntityRenderer<T, ArcaneresearchProjectileRenderer.State> {

    public static class State extends EntityRenderState {
        public float spin;
    }

    private final Identifier texture;
    private final float size;
    private final int color;
    private final boolean emissive;

    public ArcaneresearchProjectileRenderer(EntityRendererProvider.Context context,
                                        Identifier texture, float size, int color, boolean emissive) {
        super(context);
        this.texture = texture;
        this.size = size;
        this.color = color;
        this.emissive = emissive;
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.spin = (entity.tickCount + partialTicks) * 10.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.rotate(cameraState.orientation);
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.spin));

        RenderType renderType = this.emissive
                ? RenderTypes.entityTranslucentEmissive(this.texture)
                : RenderTypes.entityTranslucent(this.texture);
        int light = this.emissive ? 0xF000F0 : state.lightCoords;
        int r = (this.color >> 16) & 0xFF;
        int g = (this.color >> 8) & 0xFF;
        int b = this.color & 0xFF;
        float size = this.size;
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            quad(consumer, pose, -size, -size, 0.0F, 1.0F, r, g, b, light);
            quad(consumer, pose, size, -size, 1.0F, 1.0F, r, g, b, light);
            quad(consumer, pose, size, size, 1.0F, 0.0F, r, g, b, light);
            quad(consumer, pose, -size, size, 0.0F, 0.0F, r, g, b, light);
        });
        poseStack.popPose();
        super.submit(state, poseStack, collector, cameraState);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float u, float v, int r, int g, int b, int light) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(r, g, b, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    public static class Factory {
        private static final Identifier ORB_TEXTURE =
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/orb.png");
        private static final Identifier DART_TEXTURE =
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/dart.png");

        public static <T extends Entity> ArcaneresearchProjectileRenderer<T> orb(
                EntityRendererProvider.Context context, int color) {
            return new ArcaneresearchProjectileRenderer<>(context, ORB_TEXTURE, 0.25F, color, true);
        }

        public static <T extends Entity> ArcaneresearchProjectileRenderer<T> dart(
                EntityRendererProvider.Context context) {
            return new ArcaneresearchProjectileRenderer<>(context, DART_TEXTURE, 0.15F, 0xFFFFFF, false);
        }

        public static <T extends Entity> ArcaneresearchProjectileRenderer<T> magic(
                EntityRendererProvider.Context context, int color) {
            return new ArcaneresearchProjectileRenderer<>(context, ORB_TEXTURE, 0.2F, color, true);
        }
    }
}