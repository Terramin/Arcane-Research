package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.ArcaneBoreModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.construct.EntityArcaneBore;

// NEOFORGE_PORT (26.3): digging-beam inputs via State; beam/glow quads via
// submitCustomGeometry (new vertex chain); yBodyRot zeroing moved to extract.
// shouldShowName hook now takes (entity, distance).
public class ArcaneBoreRenderer extends MobRenderer<EntityArcaneBore, ArcaneBoreRenderer.State, ArcaneBoreModel> {

    public static class State extends net.minecraft.client.renderer.entity.state.LivingEntityRenderState {
        public boolean digging;
        public float yaw;
        public float pitch;
        public long gameTime;
        public float beamTime;
        public double eyeHeight;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/arcanebore.png");

    private static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/beam1.png");

    private static final Identifier NODE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/auranodes.png");

    public ArcaneBoreRenderer(EntityRendererProvider.Context context) {
        super(context, new ArcaneBoreModel(context.bakeLayer(ArcaneBoreModel.LAYER_LOCATION)), 0.5F);
    }

    private static Vec3 rotateAroundY(Vec3 vec, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vec.x * cos + vec.z * sin;
        double z = -vec.x * sin + vec.z * cos;
        return new Vec3(x, vec.y, z);
    }

    private static Vec3 rotateAroundZ(Vec3 vec, float angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = vec.x * cos - vec.y * sin;
        double y = vec.x * sin + vec.y * cos;
        return new Vec3(x, y, vec.z);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityArcaneBore entity, State state, float partialTicks) {
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        super.extractRenderState(entity, state, partialTicks);
        state.digging = entity.isClientDigging() && entity.isActive() && entity.hasValidInventory();
        state.yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.gameTime = entity.level().getGameTime();
        state.beamTime = entity.tickCount + partialTicks;
        state.eyeHeight = entity.getEyeHeight();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(State state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.digging) {
            return;
        }
        Vec3 offset = new Vec3(0.5, 0.075, 0.0);
        offset = rotateAroundZ(offset, state.pitch * Mth.DEG_TO_RAD);
        offset = rotateAroundY(offset, -((state.yaw + 90.0F) * Mth.DEG_TO_RAD));

        // NEOFORGE_PORT: glow sprite is camera-facing (old rotateToPlayer),
        // the beam stays directional: separate pose pushes.
        poseStack.pushPose();
        poseStack.translate(offset.x, state.eyeHeight + offset.y, offset.z);
        poseStack.rotate(camera.orientation);

        int frame = 96 + (int) (state.beamTime) % 32;
        RenderType glowType = TCRenderTypes.additiveTextured(NODE_TEXTURE);
        collector.submitCustomGeometry(poseStack, glowType,
                (pose, consumer) -> renderGlowQuad(pose, consumer, 0.5F, 32, 32, frame));
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(offset.x, state.eyeHeight + offset.y, offset.z);
        renderBeam(poseStack, collector, state);
        poseStack.popPose();
    }

    private static void renderGlowQuad(PoseStack.Pose pose, VertexConsumer consumer,
                                       float size, int grid, int gridH, int frame) {
        int xm = frame % grid;
        int ym = frame / grid;
        float u0 = xm / (float) grid;
        float u1 = u0 + 1.0F / grid;
        float v0 = ym / (float) gridH;
        float v1 = v0 + 1.0F / gridH;
        int light = 0xF000F0;
        consumer.addVertex(pose, -size, -size, 0.0F)
                .setColor(0.0F, 1.0F, 0.4F, 0.8F)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, size, -size, 0.0F)
                .setColor(0.0F, 1.0F, 0.4F, 0.8F)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, size, size, 0.0F)
                .setColor(0.0F, 1.0F, 0.4F, 0.8F)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, -size, size, 0.0F)
                .setColor(0.0F, 1.0F, 0.4F, 0.8F)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    private static void renderBeam(PoseStack poseStack, SubmitNodeCollector collector, State state) {
        float beamLength = 5.0F;
        float beamWidth = 0.15F;
        float opacity = 0.4F;

        float rotation = (state.gameTime % 72L) * 5L + 5.0F * (state.beamTime - (int) state.beamTime);
        float time = state.beamTime;
        float scroll = time * 0.2F - Mth.floor(time * 0.1F);

        RenderType beamType = TCRenderTypes.additiveTextured(BEAM_TEXTURE);
        int beamLight = 0xC0;
        int r = 0, g = 255, b = 102, a = (int) (opacity * 255);

        for (int i = 0; i < 3; i++) {
            final int pass = i;
            collector.submitCustomGeometry(poseStack, beamType, (pose, consumer) -> {
                PoseStack work = new PoseStack();
                work.last().pose().set(pose.pose());
                work.last().normal().set(pose.normal());
                // NEOFORGE_PORT: same three crossed planes as Forge (rotations
                // applied per pass on the working stack).
                work.rotate(com.mojang.math.Axis.XN.rotationDegrees(90.0F));
                work.rotate(com.mojang.math.Axis.ZN.rotationDegrees(180.0F + state.yaw));
                work.rotate(com.mojang.math.Axis.XN.rotationDegrees(state.pitch));
                work.rotate(com.mojang.math.Axis.YP.rotationDegrees(rotation));
                for (int k = 0; k <= pass; k++) {
                    work.rotate(com.mojang.math.Axis.YP.rotationDegrees(60.0F));
                }
                float u0 = 0.0F;
                float u1 = 1.0F;
                float v0 = -1.0F + scroll + pass / 3.0F;
                float v1 = beamLength + v0;
                PoseStack.Pose pp = work.last();
                consumer.addVertex(pp, 0.0F, beamLength, 0.0F)
                        .setColor(r, g, b, a).setUv(u1, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(beamLight)
                        .setNormal(pp, 0.0F, 1.0F, 0.0F);
                consumer.addVertex(pp, -beamWidth, 0.0F, 0.0F)
                        .setColor(r, g, b, a).setUv(u1, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(beamLight)
                        .setNormal(pp, 0.0F, 1.0F, 0.0F);
                consumer.addVertex(pp, beamWidth, 0.0F, 0.0F)
                        .setColor(r, g, b, a).setUv(u0, v0)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(beamLight)
                        .setNormal(pp, 0.0F, 1.0F, 0.0F);
                consumer.addVertex(pp, 0.0F, beamLength, 0.0F)
                        .setColor(r, g, b, a).setUv(u0, v1)
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(beamLight)
                        .setNormal(pp, 0.0F, 1.0F, 0.0F);
            });
        }
    }

    @Override
    protected boolean shouldShowName(EntityArcaneBore entity, double distance) {
        return false;
    }
}