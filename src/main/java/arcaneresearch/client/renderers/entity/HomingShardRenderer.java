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
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.common.entities.projectile.EntityHomingShard;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;





public class HomingShardRenderer extends EntityRenderer<EntityHomingShard, HomingShardRenderer.State> {

    public static class State extends EntityRenderState {
        public float headScale;
        public float headU0;
        public float headU1;
        public float headV0;
        public float headV1;
        public Vec3 cameraPos;
        public Vec3 current;
        public List<Vec3> trailPoints = List.of();
    }

    private static final Identifier PARTICLES =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");
    private static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/beaml.png");

    private static final int TRAIL_LENGTH = 14;
    private static final Map<Integer, Deque<Vec3>> TRAILS = new ConcurrentHashMap<>();

    public HomingShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityHomingShard entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float s = 0.4F + 0.1F * entity.getStrength();
        state.headScale = s;
        state.headU0 = (8 + entity.tickCount % 8) / 64.0F;
        state.headU1 = state.headU0 + 0.015625F;
        state.headV0 = 0.0625F;
        state.headV1 = state.headV0 + 0.015625F;

        Vec3 current = new Vec3(
                Mth.lerp(partialTicks, entity.xo, entity.getX()),
                Mth.lerp(partialTicks, entity.yo, entity.getY()),
                Mth.lerp(partialTicks, entity.zo, entity.getZ()));
        state.current = current;

        Deque<Vec3> trail = TRAILS.computeIfAbsent(entity.getId(), id -> new ArrayDeque<>());
        if (!entity.isAlive()) {
            TRAILS.remove(entity.getId());
            return;
        }
        if (!trail.isEmpty() && trail.getLast().distanceToSqr(current) > 256.0D) {
            trail.clear();
        }
        if (trail.isEmpty() || trail.getLast().distanceToSqr(current) > 1.0E-6D) {
            trail.addLast(current);
            while (trail.size() > TRAIL_LENGTH) {
                trail.removeFirst();
            }
        }
        if (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
        state.trailPoints = new ArrayList<>(trail);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            state.cameraPos = new Vec3(
                    Mth.lerp(partialTicks, player.xo, player.getX()),
                    Mth.lerp(partialTicks, player.yo, player.getY()),
                    Mth.lerp(partialTicks, player.zo, player.getZ()));
        } else {
            state.cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(camera.orientation);
        poseStack.scale(state.headScale, state.headScale, state.headScale);
        RenderType headType = RenderTypes.entityTranslucent(PARTICLES);
        collector.submitCustomGeometry(poseStack, headType,
                (pose, consumer) -> quad(consumer, pose, state));
        poseStack.popPose();

        renderTrail(state, poseStack, collector);
        super.submit(state, poseStack, collector, camera);
    }

    private void renderTrail(State state, PoseStack poseStack, SubmitNodeCollector collector) {
        List<Vec3> points = state.trailPoints;
        if (points.size() < 2) {
            return;
        }
        Vec3 current = state.current;
        Vec3 playerPos = state.cameraPos;
        UtilsFX.Vector p = new UtilsFX.Vector(
                (float) (playerPos.x - current.x),
                (float) (playerPos.y - current.y),
                (float) (playerPos.z - current.z));

        RenderType beamType = RenderTypes.entityTranslucent(BEAM_TEXTURE);
        collector.submitCustomGeometry(poseStack, beamType, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            int size = points.size();
            int c = size;
            Vec3 prev = points.get(0);
            for (int i = 1; i < size; i++) {
                Vec3 next = points.get(i);
                UtilsFX.Vector vs = new UtilsFX.Vector(
                        (float) (prev.x - current.x),
                        (float) (prev.y - current.y),
                        (float) (prev.z - current.z));
                UtilsFX.Vector nv = new UtilsFX.Vector(
                        (float) (next.x - current.x),
                        (float) (next.y - current.y),
                        (float) (next.z - current.z));
                UtilsFX.drawBeam(work, consumer, vs, nv, p,
                        0.25F * (c / (float) size), 0xF000F0, 0.405F, 0.075F, 0.525F, 0.5F);
                prev = next;
                c--;
            }
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, State state) {
        float half = 0.5F;
        float height = 1.0F;
        float r = 0.9F, g = 0.075F, b = 0.9525F;
        v(consumer, pose, -half, -half, state.headU0, state.headV1, r, g, b);
        v(consumer, pose, height - half, -half, state.headU1, state.headV1, r, g, b);
        v(consumer, pose, height - half, height - half, state.headU1, state.headV0, r, g, b);
        v(consumer, pose, -half, height - half, state.headU0, state.headV0, r, g, b);
    }

    private static void v(VertexConsumer consumer, PoseStack.Pose pose,
                          float x, float y, float u, float v, float r, float g, float b) {
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(r, g, b, 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}