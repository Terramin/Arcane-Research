package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import arcaneresearch.common.tiles.misc.TileHole;
import arcaneresearch.init.ModBlocks;
@OnlyIn(Dist.CLIENT)
public class HoleRenderer implements BlockEntityRenderer<TileHole> {
    private static final ResourceLocation END_PORTAL_TEXTURE =
            new ResourceLocation("textures/entity/end_portal.png");
    public HoleRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TileHole tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        for (Direction face : Direction.values()) {
            BlockState adjacentState = tile.getLevel().getBlockState(tile.getBlockPos().relative(face));
            if (adjacentState.isSolidRender(tile.getLevel(), tile.getBlockPos().relative(face))
                    && !adjacentState.is(ModBlocks.HOLE.get())) {
                poseStack.pushPose();
                switch (face) {
                    case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                    case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    case NORTH -> {
                    }
                    case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
                    case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                }
                poseStack.translate(0, 0, -0.499);
                renderVoidFace(poseStack, buffer);
                poseStack.popPose();
            }
        }
        poseStack.popPose();
    }
    private void renderVoidFace(PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();
        float half = 0.5f;
        vertexConsumer.vertex(matrix, -half, -half, 0).endVertex();
        vertexConsumer.vertex(matrix, half, -half, 0).endVertex();
        vertexConsumer.vertex(matrix, half, half, 0).endVertex();
        vertexConsumer.vertex(matrix, -half, half, 0).endVertex();
    }
    private void renderSimpleVoidFace(PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(END_PORTAL_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float half = 0.5f;
        int fullLight = 0x00F000F0;
        vertexConsumer.vertex(matrix, -half, -half, 0).color(0, 0, 0, 255)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, half, -half, 0).color(0, 0, 0, 255)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, half, half, 0).color(0, 0, 0, 255)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -half, half, 0).color(0, 0, 0, 255)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
    }
}
