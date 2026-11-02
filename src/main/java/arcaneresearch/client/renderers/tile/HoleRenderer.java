package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.common.tiles.misc.TileHole;
import arcaneresearch.init.ModBlocks;

@OnlyIn(Dist.CLIENT)
public class HoleRenderer implements BlockEntityRenderer<TileHole> {
    private static final ResourceLocation END_PORTAL_TEXTURE =
            ResourceLocation.tryParse("textures/entity/end_portal.png");

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Quaternionf[] FACE_ROTATIONS = new Quaternionf[DIRECTIONS.length];

    static {
        for (Direction face : DIRECTIONS) {
            Quaternionf q = new Quaternionf();
            switch (face) {
                case DOWN -> q.mul(Axis.XP.rotationDegrees(-90));
                case UP -> q.mul(Axis.XP.rotationDegrees(90));
                case NORTH -> {}
                case SOUTH -> q.mul(Axis.YP.rotationDegrees(180));
                case WEST -> q.mul(Axis.YP.rotationDegrees(90));
                case EAST -> q.mul(Axis.YP.rotationDegrees(-90));
            }
            FACE_ROTATIONS[face.ordinal()] = q;
        }
    }

    public HoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull TileHole tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        BlockPos pos = tile.getBlockPos();

        Block holeBlock = ModBlocks.HOLE != null ? ModBlocks.HOLE.get() : null;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            for (int i = 0; i < DIRECTIONS.length; i++) {
                Direction face = DIRECTIONS[i];
                BlockPos adjacentPos = pos.relative(face);
                BlockState adjacentState = level.getBlockState(adjacentPos);
                if (!adjacentState.isSolidRender(level, adjacentPos)) continue;
                if (holeBlock != null && adjacentState.is(holeBlock)) continue;

                poseStack.pushPose();
                try {
                    poseStack.mulPose(FACE_ROTATIONS[i]);
                    poseStack.translate(0, 0, -0.499);
                    renderVoidFace(poseStack, buffer);
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderVoidFace(PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.endPortal());
        Matrix4f matrix = poseStack.last().pose();
        float half = 0.5f;
        vertexConsumer.vertex(matrix, -half, -half, 0).endVertex();
        vertexConsumer.vertex(matrix, half, -half, 0).endVertex();
        vertexConsumer.vertex(matrix, half, half, 0).endVertex();
        vertexConsumer.vertex(matrix, -half, half, 0).endVertex();
    }

    private static void renderSimpleVoidFace(PoseStack poseStack, MultiBufferSource buffer) {
        assert END_PORTAL_TEXTURE != null;
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