package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.projectile.EntityRiftBlast;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@OnlyIn(Dist.CLIENT)
public class RiftBlastRenderer extends EntityRenderer<EntityRiftBlast> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("textures/entity/end_portal.png");
    private static final int TRAIL_LENGTH = 9;
    private static final Map<Integer, Deque<Vec3>> TRAILS = new ConcurrentHashMap<>();
    private final arcaneresearch.lib.CoreGLE gle =
            new arcaneresearch.lib.CoreGLE();
    public RiftBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityRiftBlast entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityRiftBlast entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
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
                entity.level().random.nextGaussian() * 0.05,
                entity.level().random.nextGaussian() * 0.05,
                entity.level().random.nextGaussian() * 0.05);
        trail.addLast(sample);
        while (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
        if (trail.size() > 2) {
            renderBeam(entity, current, new ArrayList<>(trail), partialTicks, poseStack, buffer);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderBeam(EntityRiftBlast entity, Vec3 current, List<Vec3> trail,
                            float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        int size = trail.size();
        float vv = (float) (Math.PI / (size - 1));
        double[][] pp = new double[size][3];
        float[][] colours = new float[size][4];
        double[] radii = new double[size];
        for (int c = 0; c < size; c++) {
            Vec3 v = trail.get(c);
            float variance = 1.0F + Mth.sin((c + entity.tickCount) / 3.0F) * 0.2F;
            float xx = Mth.sin((c + entity.tickCount) / 6.0F) * 0.01F;
            float yy = Mth.sin((c + entity.tickCount) / 7.0F) * 0.01F;
            float zz = Mth.sin((c + entity.tickCount) / 8.0F) * 0.01F;
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
        double[] scaled = new double[size];
        for (int q = 0; q <= 1; q++) {
            float m = 1.5F - q;
            for (int c = 0; c < size; c++) {
                scaled[c] = radii[c] * m;
            }
            VertexConsumer builder = q < 1
                    ? buffer.getBuffer(TCRenderTypes.additiveTextured(TEXTURE))
                    : buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
            gle.set_POLYCYL_TESS(3);
            gle.gleSetJoinStyle(1042);
            gle.setEmitter(new arcaneresearch.lib.GLEEmitter(poseStack, builder, 0xF000F0));
            gle.glePolyCone(pp.length, pp, colours, scaled, 1.0F / pp.length, 0.0F);
        }
        poseStack.popPose();
    }
}
