package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;

import java.util.Random;

// NEOFORGE_PORT (26.3): HumanoidMobRenderer + State (ritualist bob + tether
// endpoints, oracle CultistClericRenderer shape). Tether line math 1:1 from
// Forge, drawn via submitCustomGeometry (new vertex chain, no endVertex).
public class CultistRenderer extends HumanoidMobRenderer<EntityCultist, CultistRenderer.State, HumanoidModel<CultistRenderer.State>> {

    public static class State extends HumanoidRenderState {
        public boolean ritualist;
        public float bob;
        public float lineStartY;
        public Vec3 lineTo = Vec3.ZERO;
        public float lineFade;
        public Vec3 worldPos = Vec3.ZERO;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/cultist.png");
    private static final Identifier TETHER_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/wispy.png");

    public CultistRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new),
                context.getEquipmentRenderer()));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityCultist entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.ritualist = entity instanceof EntityCultistCleric cleric && cleric.isRitualist();
        if (!state.ritualist) {
            return;
        }
        EntityCultistCleric cleric = (EntityCultistCleric) entity;
        int phase = new Random(entity.getId()).nextInt(1000);
        float cycle = entity.tickCount + partialTicks + phase;
        state.bob = Mth.sin(cycle / 9.0F) * 0.1F + 0.21F;
        state.lineStartY = entity.getEyeHeight() * 1.2F;
        // NEOFORGE_PORT: Mob.getRestrictCenter gone -> cultist homePos (same anchor).
        BlockPos anchor = cleric.getHomePos();
        if (anchor == null) {
            state.ritualist = false;
            return;
        }
        Vec3 position = entity.getPosition(partialTicks);
        state.lineTo = new Vec3(anchor.getX() + 0.5 - position.x,
                anchor.getY() + 1.5 - state.bob - position.y,
                anchor.getZ() + 0.5 - position.z);
        state.lineFade = Math.min(entity.tickCount, 10) / 10.0F;
        state.worldPos = entity.position();
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        if (state.ritualist) {
            poseStack.translate(0.0, state.bob, 0.0);
        }
        super.submit(state, poseStack, collector, camera);
        if (state.ritualist) {
            // NEOFORGE_PORT (26.3): Forge started the tether at (0, eye*1.2, 0) in the bobbed
            // frame without translating (a translate also lifted the altar end); the wobble
            // phase uses the cleric's world position.
            Vec3 from = new Vec3(0.0, state.lineStartY, 0.0);
            Vec3 to = state.lineTo;
            float fade = state.lineFade;
            Vec3 world = state.worldPos;
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TETHER_TEXTURE),
                    (pose, consumer) -> drawTether(pose, consumer, world, from, to, fade));
        }
        poseStack.popPose();
    }

    private static void drawTether(PoseStack.Pose pose, VertexConsumer consumer,
                                   Vec3 world, Vec3 from, Vec3 to, float fade) {
        // NEOFORGE_PORT: Forge drawTether math preserved (wobbly two-pass ribbon,
        // color 1114129, speed -0.03, width 0.25); only the vertex sink changed.
        double sx = from.x, sy = from.y, sz = from.z;
        double ex = to.x, ey = to.y, ez = to.z;
        double dx = ex - sx, dy = ey - sy, dz = ez - sz;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int color = 1114129;
        float speed = -0.03F, width = 0.25F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float t = System.nanoTime() / 30000000.0F;

        double dcx = sx - ex, dcy = sy - ey, dcz = sz - ez;
        float blocks = Math.round(dist);
        float length = blocks * 6.0F;
        int segments = Math.max(1, (int) (length * fade));
        double wx = world.x, wy = world.y, wz = world.z;

        for (int pass = 0; pass < 2; pass++) {
            float[] ax = new float[segments + 1];
            float[] ay = new float[segments + 1];
            float[] az = new float[segments + 1];
            float[] au = new float[segments + 1];
            for (int i = 0; i <= segments; i++) {
                float f11 = i / length;
                float f12 = 1.0F - Math.abs(i - length / 2.0F) / (length / 2.0F);
                double wobx = dcx + Math.sin((wz % 16.0 + dist * (1.0F - f11) * 6.0F - t % 32767.0F / 5.0F) / 4.0) * 0.5 * f12;
                double woby = dcy + Math.sin((wx % 16.0 + dist * (1.0F - f11) * 6.0F - t % 32767.0F / 5.0F) / 3.0) * 0.5 * f12;
                double wobz = dcz + Math.sin((wy % 16.0 + dist * (1.0F - f11) * 6.0F - t % 32767.0F / 5.0F) / 2.0) * 0.5 * f12;
                ax[i] = (float) (ex + wobx * f11);
                ay[i] = (float) (ey + woby * f11);
                az[i] = (float) (ez + wobz * f11);
                au[i] = (1.0F - f11) * (float) dist - t * speed;
            }
            for (int i = 1; i <= segments; i++) {
                float px0, py0, pz0, qx0, qy0, qz0, px1, py1, pz1, qx1, qy1, qz1;
                if (pass == 0) {
                    px0 = ax[i - 1]; py0 = ay[i - 1] - width; pz0 = az[i - 1];
                    qx0 = ax[i - 1]; qy0 = ay[i - 1] + width; qz0 = az[i - 1];
                    px1 = ax[i]; py1 = ay[i] - width; pz1 = az[i];
                    qx1 = ax[i]; qy1 = ay[i] + width; qz1 = az[i];
                } else {
                    px0 = ax[i - 1] - width; py0 = ay[i - 1]; pz0 = az[i - 1];
                    qx0 = ax[i - 1] + width; qy0 = ay[i - 1]; qz0 = az[i - 1];
                    px1 = ax[i] - width; py1 = ay[i]; pz1 = az[i];
                    qx1 = ax[i] + width; qy1 = ay[i]; qz1 = az[i];
                }
                quad(consumer, pose, px0, py0, pz0, au[i - 1], qx0, qy0, qz0, au[i - 1],
                        qx1, qy1, qz1, au[i], px1, py1, pz1, au[i], r, g, b);
            }
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x0, float y0, float z0, float u0,
                             float x1, float y1, float z1, float u1,
                             float x2, float y2, float z2, float u2,
                             float x3, float y3, float z3, float u3,
                             float r, float g, float b) {
        consumer.addVertex(pose, x0, y0, z0).setColor(r, g, b, 0.8F).setUv(u0, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, 0.8F).setUv(u1, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, 0.8F).setUv(u2, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, 0.8F).setUv(u3, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0, 1, 0);
    }
}