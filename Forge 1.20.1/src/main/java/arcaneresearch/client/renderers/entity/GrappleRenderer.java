package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.GrapplerModel;
import arcaneresearch.common.entities.projectile.EntityGrapple;
@OnlyIn(Dist.CLIENT)
public class GrappleRenderer extends EntityRenderer<EntityGrapple> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/grappler.png");
    private static final ResourceLocation ROPE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/rope.png");
    private static final ResourceLocation PARTICLES =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particles.png");
    private final GrapplerModel model;
    private final arcaneresearch.lib.CoreGLE gle = new arcaneresearch.lib.CoreGLE();
    public GrappleRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.model = new GrapplerModel(context.bakeLayer(GrapplerModel.LAYER_LOCATION));
    }
    @Override
    public ResourceLocation getTextureLocation(EntityGrapple entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityGrapple entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.xRotO + (entity.getXRot() - entity.xRotO) * partialTicks));
        RenderType renderType = RenderType.entityCutout(TEXTURE);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        renderGlow(entity, poseStack, buffer);
        Entity owner = entity.getOwner();
        if (owner instanceof LivingEntity thrower) {
            renderRope(entity, thrower, partialTicks, poseStack, buffer);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderGlow(EntityGrapple entity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float bob = Mth.sin(entity.tickCount / 5.0F) * 0.2F + 0.2F;
        poseStack.scale(1.0F + bob, 1.0F + bob, 1.0F + bob);
        float f2 = (1 + entity.tickCount % 6) / 32.0F;
        float f3 = f2 + 0.03125F;
        float f4 = 0.21875F;
        float f5 = f4 + 0.03125F;
        float half = 0.5F;
        VertexConsumer glowBuffer = buffer.getBuffer(RenderType.entityTranslucent(PARTICLES));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int light = 0xF000F0;
        glowBuffer.vertex(matrix, -half, -half, 0).color(1.0F, 1.0F, 1.0F, 0.21F)
                .uv(f2, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 1, 0).endVertex();
        glowBuffer.vertex(matrix, half, -half, 0).color(1.0F, 1.0F, 1.0F, 0.21F)
                .uv(f3, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 1, 0).endVertex();
        glowBuffer.vertex(matrix, half, half, 0).color(1.0F, 1.0F, 1.0F, 0.21F)
                .uv(f3, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 1, 0).endVertex();
        glowBuffer.vertex(matrix, -half, half, 0).color(1.0F, 1.0F, 1.0F, 0.21F)
                .uv(f2, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0, 1, 0).endVertex();
        poseStack.popPose();
    }
    private void renderRope(EntityGrapple grapple, LivingEntity thrower, float partialTicks,
                            PoseStack poseStack, MultiBufferSource buffer) {
        final float ropeDz = arcaneresearch.client.lib.events.WandTipHelper.TIP_DZ_SIDE;
        double tx = Mth.lerp(partialTicks, thrower.xo, thrower.getX());
        double ty = Mth.lerp(partialTicks, thrower.yo, thrower.getY());
        double tz = Mth.lerp(partialTicks, thrower.zo, thrower.getZ());
        boolean tipBound = false;
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            double yy = Mth.lerp(partialTicks, thrower.yRotO, thrower.getYRot());
            double px = -Mth.cos((float) ((yy - 0.5) / 180.0 * Math.PI)) * 0.1
                    * (grapple.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? 1 : -1);
            double pz = -Mth.sin((float) ((yy - 0.5) / 180.0 * Math.PI)) * 0.1
                    * (grapple.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? 1 : -1);
            Vec3 look = thrower.getLookAngle();
            tx += px + look.x / 5.0;
            ty += thrower.getEyeHeight() / 2.6 + look.y / 5.0;
            tz += pz + look.z / 5.0;
            if (thrower instanceof net.minecraft.world.entity.player.Player player
                    && Minecraft.getInstance().cameraEntity == thrower
                    && player.getItemInHand(grapple.getHand()).getItem()
                            instanceof arcaneresearch.api.wands.IWand) {
                try {
                    Vec3 tip = arcaneresearch.client.lib.events.WandTipHelper.tipWorld(
                            player, partialTicks, 0.0F, 0.0F, ropeDz);
                    if (tip != null) {
                        tx = tip.x;
                        ty = tip.y;
                        tz = tip.z;
                        tipBound = true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        double gx = Mth.lerp(partialTicks, grapple.xo, grapple.getX());
        double gy = Mth.lerp(partialTicks, grapple.yo, grapple.getY());
        double gz = Mth.lerp(partialTicks, grapple.zo, grapple.getZ());
        double ex = tx - gx;
        double ey = ty - gy + (tipBound ? 0.0 : thrower.getBbHeight() / 2.0);
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
        poseStack.pushPose();
        int n = points.size();
        double[][] pp = new double[n][3];
        float[][] colours = new float[n][4];
        double[] radii = new double[n];
        for (int a = 0; a < n; ++a) {
            pp[a][0] = points.get(a)[0];
            pp[a][1] = points.get(a)[1];
            pp[a][2] = points.get(a)[2];
            colours[a][0] = 1.0F;
            colours[a][1] = 1.0F;
            colours[a][2] = 1.0F;
            colours[a][3] = 1.0F;
            radii[a] = 0.025;
        }
        VertexConsumer ropeBuffer = buffer.getBuffer(RenderType.entityTranslucent(ROPE_TEXTURE));
        gle.set_POLYCYL_TESS(4);
        gle.gleSetJoinStyle(1042);
        gle.setEmitter(new arcaneresearch.lib.GLEEmitter(poseStack, ropeBuffer, 0xF000F0));
        gle.glePolyCone(pp.length, pp, colours, radii, 2.0F, length - points.size());
        poseStack.popPose();
    }
}
