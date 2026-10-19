package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.GrapplerModel;
import arcaneresearch.common.entities.projectile.EntityGrapple;
import arcaneresearch.lib.CoreGLE;
import arcaneresearch.lib.GLEEmitter;

// NEOFORGE_PORT (26.3): head prop via seeded model emission, glow billboard
// via camera orientation, rope tube via CoreGLE/GLEEmitter (chain already
// ported) inside submitCustomGeometry. Rope points computed eagerly in submit
// from the state (thrower/grapple interp positions); camera first-person
// branch preserved.
public class GrappleRenderer extends EntityRenderer<EntityGrapple, GrappleRenderer.State> {

    public static class State extends EntityRenderState {
        public float yaw;
        public float pitch;
        public float bobSeed;
        public float glowFrame;
        public double[][] ropePoints;
        public float[][] ropeColours;
        public double[] ropeRadii;
        public float ropeLength;
        public float ropeTail;
        public boolean hasRope;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/grappler.png");
    private static final Identifier ROPE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/rope.png");
    private static final Identifier PARTICLES =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");

    private final GrapplerModel model;
    private final CoreGLE gle = new CoreGLE();

    public GrappleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(GrapplerModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityGrapple entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks - 90.0F;
        state.pitch = entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks;
        state.bobSeed = entity.tickCount;
        state.glowFrame = 1 + entity.tickCount % 6;
        computeRope(entity, state, partialTicks);
    }

    // NEOFORGE_PORT: rope tube points computed eagerly here (both entities
    // available); emission happens in submit from the baked arrays.
    private static void computeRope(EntityGrapple grapple, State state, float partialTicks) {
        state.hasRope = false;
        Entity owner = grapple.getOwner();
        if (!(owner instanceof LivingEntity thrower)) {
            return;
        }
        double tx = Mth.lerp(partialTicks, thrower.xo, thrower.getX());
        double ty = Mth.lerp(partialTicks, thrower.yo, thrower.getY());
        double tz = Mth.lerp(partialTicks, thrower.zo, thrower.getZ());
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            double yy = Mth.lerp(partialTicks, thrower.yRotO, thrower.getYRot());
            double px = -Mth.cos((float) ((yy - 0.5) / 180.0 * Math.PI)) * 0.1
                    * (grapple.getHand() == InteractionHand.MAIN_HAND ? 1 : -1);
            double pz = -Mth.sin((float) ((yy - 0.5) / 180.0 * Math.PI)) * 0.1
                    * (grapple.getHand() == InteractionHand.MAIN_HAND ? 1 : -1);
            Vec3 look = thrower.getLookAngle();
            tx += px + look.x / 5.0;
            ty += thrower.getEyeHeight() / 2.6 + look.y / 5.0;
            tz += pz + look.z / 5.0;
        }
        double gx = Mth.lerp(partialTicks, grapple.xo, grapple.getX());
        double gy = Mth.lerp(partialTicks, grapple.yo, grapple.getY());
        double gz = Mth.lerp(partialTicks, grapple.zo, grapple.getZ());

        double ex = tx - gx;
        double ey = ty - gy + thrower.getBbHeight() / 2.0;
        double ez = tz - gz;
        float length = (float) (Math.sqrt(ex * ex + ey * ey + ez * ez) * 5.0);
        int steps = (int) length;
        if (steps < 3) {
            return;
        }

        java.util.ArrayList<double[]> points = new java.util.ArrayList<>();
        points.add(new double[]{0.0, 0.0, 0.0});
        for (int a = 1; a < steps - 1; ++a) {
            float dist = a * (length / steps);
            float ss = 1.0F - a / (steps * 1.25F);
            double dx = ex / steps * a + Mth.sin(dist / 10.0F) * grapple.ampl * ss;
            double dy = ey / steps * a + Mth.sin(dist / 8.0F) * grapple.ampl * ss;
            double dz = ez / steps * a + Mth.sin(dist / 6.0F) * grapple.ampl * ss;
            points.add(new double[]{dx, dy, dz});
        }
        points.add(new double[]{ex, ey, ez});
        points.add(new double[]{ex, ey, ez});
        if (points.size() <= 2) {
            return;
        }
        int n = points.size();
        state.ropePoints = new double[n][3];
        state.ropeColours = new float[n][4];
        state.ropeRadii = new double[n];
        for (int a = 0; a < n; ++a) {
            state.ropePoints[a][0] = points.get(a)[0];
            state.ropePoints[a][1] = points.get(a)[1];
            state.ropePoints[a][2] = points.get(a)[2];
            state.ropeColours[a][0] = 1.0F;
            state.ropeColours[a][1] = 1.0F;
            state.ropeColours[a][2] = 1.0F;
            state.ropeColours[a][3] = 1.0F;
            state.ropeRadii[a] = 0.025;
        }
        state.ropeLength = length;
        state.ropeTail = length - points.size();
        state.hasRope = true;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(state.yaw));
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.pitch));

        RenderType renderType = RenderTypes.entityCutout(TEXTURE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            this.model.renderToBuffer(work, consumer, light,
                    OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        });
        poseStack.popPose();

        renderGlow(state, poseStack, collector, camera);

        if (state.hasRope) {
            poseStack.pushPose();
            RenderType ropeType = RenderTypes.entityTranslucent(ROPE_TEXTURE);
            collector.submitCustomGeometry(poseStack, ropeType, (pose, consumer) -> {
                PoseStack work = new PoseStack();
                work.last().pose().set(pose.pose());
                work.last().normal().set(pose.normal());
                gle.set_POLYCYL_TESS(4);
                gle.gleSetJoinStyle(1042);
                gle.setEmitter(new GLEEmitter(work, consumer, 0xF000F0));
                gle.glePolyCone(state.ropePoints.length, state.ropePoints,
                        state.ropeColours, state.ropeRadii, 2.0F, state.ropeTail);
            });
            poseStack.popPose();
        }

        super.submit(state, poseStack, collector, camera);
    }

    private void renderGlow(State state, PoseStack poseStack, SubmitNodeCollector collector,
                            CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        float bob = Mth.sin(state.bobSeed / 5.0F) * 0.2F + 0.2F;
        poseStack.scale(1.0F + bob, 1.0F + bob, 1.0F + bob);

        float f2 = state.glowFrame / 32.0F;
        float f3 = f2 + 0.03125F;
        float f4 = 0.21875F;
        float f5 = f4 + 0.03125F;
        float half = 0.5F;

        RenderType glowType = RenderTypes.entityTranslucent(PARTICLES);
        collector.submitCustomGeometry(poseStack, glowType, (pose, consumer) -> {
            quad(consumer, pose, -half, -half, f2, f5);
            quad(consumer, pose, half, -half, f3, f5);
            quad(consumer, pose, half, half, f3, f4);
            quad(consumer, pose, -half, half, f2, f4);
        });
        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0)
                .setColor(1.0F, 1.0F, 1.0F, 0.21F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
    }

}