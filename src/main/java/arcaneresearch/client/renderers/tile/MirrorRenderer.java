package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.blocks.devices.BlockMirror;
import arcaneresearch.common.tiles.devices.TileMirror;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class MirrorRenderer implements BlockEntityRenderer<TileMirror> {
    private static final ResourceLocation TUNNEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/tunnel.png");
    private static final ResourceLocation PARTICLEFIELD_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particlefield.png");
    private static final ResourceLocation INACTIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorpane.png");
    private static final ResourceLocation ACTIVE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorpanetrans.png");
    private static final ResourceLocation FRAME_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorframe.png");
    private static final ResourceLocation FRAME2_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/mirrorframe2.png");

    private static final float FRAME_THICKNESS = 0.1f;
    private static final double PLANE_Z = -0.4475;
    private static final float OVERLAY_SIZE = 0.35f;
    private static final float[][] CORNERS = {
            {-0.3125f, -0.3125f},
            {-0.3125f, 0.3125f},
            {0.3125f, 0.3125f},
            {0.3125f, -0.3125f}
    };

    private static final Map<ResourceLocation, RenderType> CUTOUT_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_CACHE = new ConcurrentHashMap<>();

    private static final Quaternionf[] FACING_ROTATIONS = new Quaternionf[6];

    private static final float[] RND_R = new float[16];
    private static final float[] RND_G = new float[16];
    private static final float[] RND_B = new float[16];
    private static final double[] COS = new double[16];
    private static final double[] SIN = new double[16];
    private static final float[] F2 = new float[16];
    private static final float[] F3 = new float[16];
    private static final float[] F4 = new float[16];

    static {
        for (Direction d : Direction.values()) {
            Quaternionf q = new Quaternionf();
            switch (d) {
                case DOWN -> q.mul(Axis.XP.rotationDegrees(90));
                case UP -> q.mul(Axis.XP.rotationDegrees(-90));
                case NORTH -> q.mul(Axis.YP.rotationDegrees(180));
                case SOUTH -> {}
                case WEST -> q.mul(Axis.YP.rotationDegrees(-90));
                case EAST -> q.mul(Axis.YP.rotationDegrees(90));
            }
            FACING_ROTATIONS[d.ordinal()] = q;
        }

        java.util.Random random = new java.util.Random(31100L);
        for (int i = 0; i < 16; i++) {
            float r = random.nextFloat() * 0.5f + 0.1f;
            float g = random.nextFloat() * 0.5f + 0.4f;
            float b = random.nextFloat() * 0.5f + 0.5f;
            if (i == 0) {
                r = g = b = 1.0f;
            }
            RND_R[i] = r;
            RND_G[i] = g;
            RND_B[i] = b;
            double rot = Math.toRadians((i * i * 4321 + i * 9) * 2.0);
            COS[i] = Math.cos(rot);
            SIN[i] = Math.sin(rot);

            float f2 = 16 - i;
            float f3 = 0.0625f;
            float f4 = 1.0f / (f2 + 1.0f);
            if (i == 0) {
                f4 = 0.1f;
                f2 = 65.0f;
                f3 = 0.125f;
            }
            if (i == 1) {
                f3 = 0.5f;
            }
            F2[i] = f2;
            F3[i] = f3;
            F4[i] = f4;
        }
    }

    private static final class MirrorTypes {
        static final RenderType TUNNEL = TCRenderTypes.mirrorLayer(TUNNEL_TEXTURE, false);
        static final RenderType PARTICLE = TCRenderTypes.mirrorLayer(PARTICLEFIELD_TEXTURE, true);
    }

    private static RenderType cutout(ResourceLocation texture) {
        return CUTOUT_CACHE.computeIfAbsent(texture, RenderType::entityCutout);
    }

    private static RenderType translucent(ResourceLocation texture) {
        return TRANSLUCENT_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
    }

    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }

    static ResourceLocation frame2Texture() {
        return FRAME2_TEXTURE;
    }

    public static void renderMirror(BlockPos pos, Level level, BlockState state, boolean linked,
                                    ResourceLocation frameTexture, float partialTicks, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight) {
        if (poseStack == null || buffer == null || state == null || pos == null || level == null) return;
        Direction facing = getFacing(state);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            applyRotation(poseStack, facing);
            renderFrame(poseStack, buffer, frameTexture, packedLight);
            if (linked && isPlayerNearby(pos, level)) {
                renderActivePortal(poseStack, buffer, pos, facing, packedLight);
            } else {
                renderInactivePane(poseStack, buffer, packedLight);
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderFrame(PoseStack poseStack, MultiBufferSource buffer,
                                    ResourceLocation texture, int packedLight) {
        if (poseStack == null || buffer == null || texture == null) return;
        poseStack.pushPose();
        try {
            poseStack.translate(-0.5f, -0.5f, -0.499f + FRAME_THICKNESS);
            VertexConsumer vertexConsumer = buffer.getBuffer(cutout(texture));
            UtilsFX.renderTextureIn3D(poseStack, vertexConsumer,
                    1.0F, 1.0F, 0.0F, 0.0F, 16, 16, FRAME_THICKNESS, packedLight);
        } finally {
            poseStack.popPose();
        }
    }

    private static boolean isPlayerNearby(BlockPos pos, Level level) {
        if (pos == null || level == null) return false;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 1024.0;
    }

    private static void renderActivePortal(PoseStack poseStack, MultiBufferSource buffer,
                                           BlockPos pos, Direction facing, int packedLight) {
        if (poseStack == null || buffer == null || pos == null || facing == null) return;
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        Entity view = mc.getCameraEntity();
        Vec3 playPos = view != null ? view.getPosition(1.0f) : camPos;

        double[] camLocal = toLocal(facing, pos, camPos.x, camPos.y, camPos.z);
        double[] playLocal = toLocal(facing, pos, playPos.x, playPos.y, playPos.z);

        double f6 = PLANE_Z - camLocal[2];
        if (Math.abs(f6) < 1.0e-4) {
            f6 = f6 < 0.0 ? -1.0e-4 : 1.0e-4;
        }

        float time = (System.currentTimeMillis() % 700000L) / 250000.0f;
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < 16; i++) {
            float r = RND_R[i];
            float g = RND_G[i];
            float b = RND_B[i];
            double shift = F2[i] / f6;
            double cos = COS[i];
            double sin = SIN[i];
            float f3 = F3[i];
            float f4 = F4[i];
            VertexConsumer builder = buffer.getBuffer(i == 0 ? MirrorTypes.TUNNEL : MirrorTypes.PARTICLE);

            for (float[] corner : CORNERS) {
                double u = corner[0] + camLocal[0] * shift - playLocal[0];
                double v = corner[1] + camLocal[1] * shift - playLocal[1];
                u -= 0.5;
                v -= 0.5;
                double ru = u * cos - v * sin;
                double rv = u * sin + v * cos;
                u = ru + 0.5;
                v = rv + 0.5;
                u *= f3;
                v *= f3;
                v += time;
                builder.vertex(matrix, corner[0], corner[1], (float) PLANE_Z)
                        .uv((float) u, (float) v)
                        .color(r * f4, g * f4, b * f4, 1.0f)
                        .endVertex();
            }
        }
        renderOverlay(poseStack, buffer, ACTIVE_TEXTURE, packedLight);
    }

    private static double[] toLocal(Direction facing, BlockPos pos, double x, double y, double z) {
        if (facing == null || pos == null) return new double[]{x, y, z};
        double dx = x - (pos.getX() + 0.5);
        double dy = y - (pos.getY() + 0.5);
        double dz = z - (pos.getZ() + 0.5);
        return switch (facing) {
            case NORTH -> new double[]{-dx, dy, -dz};
            case WEST -> new double[]{dz, dy, -dx};
            case EAST -> new double[]{-dz, dy, dx};
            case DOWN -> new double[]{dx, dz, -dy};
            case UP -> new double[]{dx, -dz, dy};
            default -> new double[]{dx, dy, dz};
        };
    }

    private static void renderInactivePane(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        renderOverlay(poseStack, buffer, INACTIVE_TEXTURE, packedLight);
    }

    private static void renderOverlay(PoseStack poseStack, MultiBufferSource buffer,
                                      ResourceLocation texture, int packedLight) {
        if (poseStack == null || buffer == null || texture == null) return;
        poseStack.pushPose();
        try {
            poseStack.translate(0, 0, -0.4375f);
            VertexConsumer vertexConsumer = buffer.getBuffer(translucent(texture));
            Matrix4f matrix = poseStack.last().pose();
            float size = OVERLAY_SIZE;
            vertexConsumer.vertex(matrix, -size, -size, 0).color(255, 255, 255, 255)
                    .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, size, -size, 0).color(255, 255, 255, 255)
                    .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                    .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, -size, size, 0).color(255, 255, 255, 255)
                    .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        } finally {
            poseStack.popPose();
        }
    }

    private static void applyRotation(PoseStack poseStack, Direction facing) {
        if (facing == null) facing = Direction.NORTH;
        poseStack.mulPose(FACING_ROTATIONS[facing.ordinal()]);
    }

    private static Direction getFacing(BlockState state) {
        if (state != null && state.hasProperty(BlockMirror.FACING)) {
            return state.getValue(BlockMirror.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    public void render(@NotNull TileMirror tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        BlockPos pos = tile.getBlockPos();
        BlockState state = tile.getBlockState();
        renderMirror(pos, level, state, tile.linked, FRAME_TEXTURE, partialTicks, poseStack, buffer, packedLight);
    }
}