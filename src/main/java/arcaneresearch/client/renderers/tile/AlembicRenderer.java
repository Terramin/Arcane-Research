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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.BellowsNozzleModel;
import arcaneresearch.common.tiles.essentia.TileAlembic;

@OnlyIn(Dist.CLIENT)
public class AlembicRenderer implements BlockEntityRenderer<TileAlembic> {
    private static final ResourceLocation LABEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/label.png");
    private static final ResourceLocation BORE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/bore.png");

    private static final float LABEL_SIZE = 0.22f;
    private static final float ICON_SIZE = 0.15f;
    private static final float LABEL_Z_OFFSET = -0.376f;
    private static final float ICON_Z_OFFSET = -0.001f;
    private static final float FULL_WHITE = 1.0F;
    private static final int FULLBRIGHT = 0x00F000F0;

    private final BellowsNozzleModel nozzleModel;

    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
        this.nozzleModel = new BellowsNozzleModel(
                context.bakeLayer(BellowsNozzleModel.LAYER_LOCATION));
    }

    @Override
    public void render(TileAlembic tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Aspect filter = tile.getAspectFilter();

        if (filter != null) {
            renderLabel(tile, filter, poseStack, buffer, packedLight);
        }

        if (tile.getLevel() != null) {
            renderNozzles(tile, poseStack, buffer, packedLight);
        }
    }

    private void renderLabel(TileAlembic tile, Aspect aspect, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        switch (tile.getFacing()) {
            case 2 -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case 3 -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case 4 -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case 5 -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
        }

        poseStack.translate(0, 0.5, LABEL_Z_OFFSET);

        VertexConsumer labelConsumer = buffer.getBuffer(RenderType.entityTranslucent(LABEL_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();

        labelConsumer.vertex(matrix, -LABEL_SIZE, -LABEL_SIZE, 0).color(255, 255, 255, 255)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, LABEL_SIZE, -LABEL_SIZE, 0).color(255, 255, 255, 255)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, LABEL_SIZE, LABEL_SIZE, 0).color(255, 255, 255, 255)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, -LABEL_SIZE, LABEL_SIZE, 0).color(255, 255, 255, 255)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();

        ResourceLocation image = aspect.getImage();
        if (image == null) {
            poseStack.popPose();
            return;
        }

        poseStack.translate(0, 0, ICON_Z_OFFSET);

        int rgb = aspect.getColor();
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;

        VertexConsumer iconConsumer = buffer.getBuffer(
                RenderType.entityTranslucent(UtilsFX.strippedAspectTexture(image)));
        Matrix4f iconMatrix = poseStack.last().pose();

        iconConsumer.vertex(iconMatrix, -ICON_SIZE, -ICON_SIZE, 0).color(r, g, b, 1f)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, ICON_SIZE, -ICON_SIZE, 0).color(r, g, b, 1f)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, ICON_SIZE, ICON_SIZE, 0).color(r, g, b, 1f)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, -ICON_SIZE, ICON_SIZE, 0).color(r, g, b, 1f)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULLBRIGHT).normal(0, 0, 1).endVertex();

        poseStack.popPose();
    }

    private void renderNozzles(TileAlembic tile, PoseStack poseStack,
                               MultiBufferSource buffer, int packedLight) {
        Level level = tile.getLevel();
        if (level == null) return;

        BlockPos pos = tile.getBlockPos();

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!tile.canOutputTo(dir)) continue;

            BlockEntity te = level.getBlockEntity(pos.relative(dir));

            if (te instanceof IEssentiaTransport transport
                    && transport.canInputFrom(dir.getOpposite())) {
                renderNozzle(dir, poseStack, buffer, packedLight);
            }
        }
    }

    private void renderNozzle(Direction dir, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        switch (dir) {
            case DOWN -> {
                poseStack.translate(-0.5, 0.5, 0.0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            }
            case UP -> {
                poseStack.translate(0.5, 0.5, 0.0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            }
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> {
            }
            default -> {
            }
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BORE_TEXTURE));
        nozzleModel.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY,
                FULL_WHITE, FULL_WHITE, FULL_WHITE, FULL_WHITE);

        poseStack.popPose();
    }
}