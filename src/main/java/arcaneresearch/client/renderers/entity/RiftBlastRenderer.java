package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.projectile.EntityRiftBlast;
import arcaneresearch.lib.CoreGLE;
import arcaneresearch.lib.GLEEmitter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public class RiftBlastRenderer extends EntityRenderer<EntityRiftBlast, RiftBlastRenderer.State> {

    public static class State extends EntityRenderState {
        public List<Vec3> trail = List.of();
        public Vec3 current = Vec3.ZERO;
        public int tickCount;
    }

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/end_portal/end_portal.png"); 

    private static final int TRAIL_LENGTH = 9;
    private static final Map<Integer, Deque<Vec3>> TRAILS = new ConcurrentHashMap<>();

    private final CoreGLE gle = new CoreGLE();

    public RiftBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityRiftBlast entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tickCount = entity.tickCount;
        Vec3 current = new Vec3(
                Mth.lerp(partialTicks, entity.xo, entity.getX()),
                Mth.lerp(partialTicks, entity.yo, entity.getY()),
                Mth.lerp(partialTicks, entity.zo, entity.getZ()));

        Deque<Vec3> trail = TRAILS.computeIfAbsent(entity.getId(), id -> new ArrayDeque<>());
        if (!entity.isAlive()) {
            TRAILS.remove(entity.getId());
            return;
        }
        if (!trail.isEmpty() && trail.getLast().distanceToSqr(current) > 256.0D) {
            trail.clear();
        }
        
        Vec3 sample = current.add(
                entity.getRandom().nextGaussian() * 0.05,
                entity.getRandom().nextGaussian() * 0.05,
                entity.getRandom().nextGaussian() * 0.05);
        trail.addLast(sample);
        while (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
        state.trail = new ArrayList<>(trail);
        state.current = current;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        List<Vec3> trail = state.trail;
        if (trail.size() > 2) {
            renderBeam(state, trail, poseStack, collector);
        }
        super.submit(state, poseStack, collector, camera);
    }

    private void renderBeam(State state, List<Vec3> trail, PoseStack poseStack, SubmitNodeCollector collector) {
        int size = trail.size();
        float vv = (float) (Math.PI / (size - 1));
        Vec3 current = state.current;

        double[][] pp = new double[size][3];
        float[][] colours = new float[size][4];
        double[] radii = new double[size];
        for (int c = 0; c < size; c++) {
            Vec3 v = trail.get(c);
            float variance = 1.0F + Mth.sin((c + state.tickCount) / 3.0F) * 0.2F;
            float xx = Mth.sin((c + state.tickCount) / 6.0F) * 0.01F;
            float yy = Mth.sin((c + state.tickCount) / 7.0F) * 0.01F;
            float zz = Mth.sin((c + state.tickCount) / 8.0F) * 0.01F;
            pp[c][0] = v.x + xx - current.x;
            pp[c][1] = v.y + yy - current.y;
            pp[c][2] = v.z + zz - current.z;
            radii[c] = 0.1 * variance * Mth.sin(c * vv);
            colours[c][0] = 1.0F;
            colours[c][1] = 0.0F;
            colours[c][2] = 0.0F;
            colours[c][3] = 1.0F;
        }

        poseStack.pushPose();
        for (int q = 0; q <= 1; q++) {
            float m = 1.5F - q;
            double[] scaled = new double[size];
            for (int c = 0; c < size; c++) {
                scaled[c] = radii[c] * m;
            }
            RenderType type = q < 1
                    ? TCRenderTypes.additiveTextured(TEXTURE)
                    : RenderTypes.entityTranslucent(TEXTURE);
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                PoseStack work = new PoseStack();
                work.last().pose().set(pose.pose());
                work.last().normal().set(pose.normal());
                gle.set_POLYCYL_TESS(3);
                gle.gleSetJoinStyle(1042);
                gle.setEmitter(new GLEEmitter(work, consumer, 0xF000F0));
                gle.glePolyCone(pp.length, pp, colours, scaled, 1.0F / pp.length, 0.0F);
            });
        }
        poseStack.popPose();
    }
}