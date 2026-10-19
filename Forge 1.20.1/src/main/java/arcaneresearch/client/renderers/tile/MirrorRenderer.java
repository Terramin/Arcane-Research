package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.devices.TileMirror;
@OnlyIn(Dist.CLIENT)
public class MirrorRenderer implements BlockEntityRenderer<TileMirror> {
    private static final ResourceLocation TUNNEL_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/tunnel.png");
    private static final ResourceLocation PARTICLEFIELD_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particlefield.png");
    private static final ResourceLocation INACTIVE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/mirrorpane.png");
    private static final ResourceLocation ACTIVE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/mirrorpanetrans.png");
    private static final ResourceLocation FRAME_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/mirrorframe.png");
    private static final ResourceLocation FRAME2_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/mirrorframe2.png");
    public MirrorRenderer(BlockEntityRendererProvider.Context context) {
    }
    static ResourceLocation frame2Texture() {
        return FRAME2_TEXTURE;
    }
    public static void renderMirror(net.minecraft.core.BlockPos pos,
                                    net.minecraft.world.level.Level level,
                                    BlockState state, boolean linked,
                                    ResourceLocation frameTexture,
                                    float partialTicks, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight) {
        Direction facing = getFacing(state);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        applyRotation(poseStack, facing);
        renderFrame(poseStack, buffer, frameTexture, packedLight);
        if (linked && isPlayerNearby(pos, level)) {
            renderActivePortal(poseStack, buffer, pos, facing, packedLight);
        } else {
            renderInactivePane(poseStack, buffer, packedLight);
        }
        poseStack.popPose();
    }
    private static final float FRAME_THICKNESS = 0.1f;
    private static void renderFrame(PoseStack poseStack, MultiBufferSource buffer,
                                    ResourceLocation texture, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(-0.5f, -0.5f, -0.499f + FRAME_THICKNESS);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(texture));
        arcaneresearch.client.lib.UtilsFX.renderTextureIn3D(poseStack, vertexConsumer,
                1.0F, 1.0F, 0.0F, 0.0F, 16, 16, FRAME_THICKNESS, packedLight);
        poseStack.popPose();
    }
    private static boolean isPlayerNearby(net.minecraft.core.BlockPos pos,
                                          net.minecraft.world.level.Level level) {
        if (level == null) return false;
        return Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.distanceToSqr(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5) < 1024.0;
    }
    private static void renderActivePortal(PoseStack poseStack, MultiBufferSource buffer,
                                           net.minecraft.core.BlockPos pos, Direction facing,
                                           int packedLight) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.gameRenderer.getMainCamera() == null) {
            return;
        }
        net.minecraft.world.phys.Vec3 camPos =
                mc.gameRenderer.getMainCamera().getPosition();
        net.minecraft.world.entity.Entity view = mc.getCameraEntity();
        net.minecraft.world.phys.Vec3 playPos =
                view != null ? view.getPosition(1.0f) : camPos;
        double[] camLocal = toLocal(facing, pos,
                camPos.x, camPos.y, camPos.z);
        double[] playLocal = toLocal(facing, pos,
                playPos.x, playPos.y, playPos.z);
        final double planeZ = -0.4475;
        double f6 = planeZ - camLocal[2];
        if (Math.abs(f6) < 1.0e-4) {
            f6 = f6 < 0.0 ? -1.0e-4 : 1.0e-4;
        }
        float time = (System.currentTimeMillis() % 700000L) / 250000.0f;
        java.util.Random random = new java.util.Random(31100L);
        Matrix4f matrix = poseStack.last().pose();
        float half = 0.5f - 0.1875f;
        for (int i = 0; i < 16; ++i) {
            float f2 = 16 - i;
            float f3 = 0.0625f;
            float f4 = 1.0f / (f2 + 1.0f);
            ResourceLocation tex = PARTICLEFIELD_TEXTURE;
            boolean additive = true;
            if (i == 0) {
                tex = TUNNEL_TEXTURE;
                f4 = 0.1f;
                f2 = 65.0f;
                f3 = 0.125f;
                additive = false;
            }
            if (i == 1) {
                f3 = 0.5f;
            }
            float r = random.nextFloat() * 0.5f + 0.1f;
            float g = random.nextFloat() * 0.5f + 0.4f;
            float b = random.nextFloat() * 0.5f + 0.5f;
            if (i == 0) {
                r = (g = (b = 1.0f));
            }
            VertexConsumer builder =
                    buffer.getBuffer(arcaneresearch.client.renderers.TCRenderTypes
                            .mirrorLayer(tex, additive));
            double shift = f2 / f6;
            double rot = Math.toRadians((i * i * 4321 + i * 9) * 2.0);
            double cos = Math.cos(rot);
            double sin = Math.sin(rot);
            float[][] corners = {
                    {-half, -half}, {-half, half}, {half, half}, {half, -half}
            };
            for (float[] corner : corners) {
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
                builder.vertex(matrix, corner[0], corner[1], (float) planeZ)
                        .uv((float) u, (float) v)
                        .color(r * f4, g * f4, b * f4, 1.0f)
                        .endVertex();
            }
        }
        renderOverlay(poseStack, buffer, ACTIVE_TEXTURE, packedLight);
    }
    private static double[] toLocal(Direction facing, net.minecraft.core.BlockPos pos,
                                    double x, double y, double z) {
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
        poseStack.pushPose();
        poseStack.translate(0, 0, -0.4375f);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.35f;
        vertexConsumer.vertex(matrix, -size, -size, 0).color(255, 255, 255, 255)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, -size, 0).color(255, 255, 255, 255)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -size, size, 0).color(255, 255, 255, 255)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        poseStack.popPose();
    }
    private static void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case SOUTH -> {
            }
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
    }
    private static Direction getFacing(BlockState state) {
        if (state.hasProperty(arcaneresearch.common.blocks.devices.BlockMirror.FACING)) {
            return state.getValue(arcaneresearch.common.blocks.devices.BlockMirror.FACING);
        }
        return Direction.NORTH;
    }
    @Override
    public void render(TileMirror tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderMirror(tile.getBlockPos(), tile.getLevel(), tile.getBlockState(),
                tile.linked, FRAME_TEXTURE, partialTicks, poseStack, buffer, packedLight);
    }
}
