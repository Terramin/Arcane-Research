package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.common.entities.projectile.EntityHomingShard;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@OnlyIn(Dist.CLIENT)
public class HomingShardRenderer extends EntityRenderer<EntityHomingShard> {
    private static final ResourceLocation PARTICLES =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particles.png");
    private static final ResourceLocation BEAM_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/beaml.png");
    private static final int TRAIL_LENGTH = 14;
    private static final Map<Integer, Deque<Vec3>> TRAILS = new ConcurrentHashMap<>();
    public HomingShardRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityHomingShard entity) {
        return PARTICLES;
    }
    @Override
    public void render(EntityHomingShard entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        renderHead(entity, poseStack, buffer);
        renderTrail(entity, partialTicks, poseStack, buffer);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderHead(EntityHomingShard entity, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float s = 0.4F + 0.1F * entity.getStrength();
        poseStack.scale(s, s, s);
        float f2 = (8 + entity.tickCount % 8) / 64.0F;
        float f3 = f2 + 0.015625F;
        float f4 = 0.0625F;
        float f5 = f4 + 0.015625F;
        float half = 0.5F;
        float height = 1.0F;
        float r = 0.9F, g = 0.075F, b = 0.9525F;
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(PARTICLES));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        int light = 0xF000F0;
        builder.vertex(matrix, -half, -half, 0.0F).color(r, g, b, 1.0F)
                .uv(f2, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, height - half, -half, 0.0F).color(r, g, b, 1.0F)
                .uv(f3, f5).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, height - half, height - half, 0.0F).color(r, g, b, 1.0F)
                .uv(f3, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix, -half, height - half, 0.0F).color(r, g, b, 1.0F)
                .uv(f2, f4).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
        poseStack.popPose();
    }
    private void renderTrail(EntityHomingShard entity, float partialTicks,
                             PoseStack poseStack, MultiBufferSource buffer) {
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
        if (trail.isEmpty() || trail.getLast().distanceToSqr(current) > 1.0E-6D) {
            trail.addLast(current);
            while (trail.size() > TRAIL_LENGTH) {
                trail.removeFirst();
            }
        }
        if (trail.size() < 2) {
            return;
        }
        Vec3 playerPos;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            playerPos = new Vec3(
                    Mth.lerp(partialTicks, player.xo, player.getX()),
                    Mth.lerp(partialTicks, player.yo, player.getY()),
                    Mth.lerp(partialTicks, player.zo, player.getZ()));
        } else {
            playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        }
        List<Vec3> points = new ArrayList<>(trail);
        UtilsFX.Vector p = new UtilsFX.Vector(
                (float) (playerPos.x - current.x),
                (float) (playerPos.y - current.y),
                (float) (playerPos.z - current.z));
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(BEAM_TEXTURE));
        poseStack.pushPose();
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
            UtilsFX.drawBeam(poseStack, builder, vs, nv, p,
                    0.25F * (c / (float) size), 0xF000F0, 0.405F, 0.075F, 0.525F, 0.5F);
            prev = next;
            c--;
        }
        poseStack.popPose();
        if (trail.size() > TRAIL_LENGTH) {
            trail.removeFirst();
        }
    }
}
