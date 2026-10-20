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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.common.tiles.essentia.TileAlembic;
import java.awt.*;
@OnlyIn(Dist.CLIENT)
public class AlembicRenderer implements BlockEntityRenderer<TileAlembic> {
    private static final ResourceLocation LABEL_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/label.png");
    private static final ResourceLocation BORE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/bore.png");
    private final arcaneresearch.client.models.block.BellowsNozzleModel nozzleModel;
    public AlembicRenderer(BlockEntityRendererProvider.Context context) {
        this.nozzleModel = new arcaneresearch.client.models.block.BellowsNozzleModel(
                context.bakeLayer(arcaneresearch.client.models.block.BellowsNozzleModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileAlembic tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getAspectFilter() != null) {
            renderLabel(tile, tile.getAspectFilter(), poseStack, buffer, packedLight);
        }
        if (tile.getLevel() != null) {
            renderNozzles(tile, poseStack, buffer, packedLight);
        }
    }
    private void renderLabel(TileAlembic tile, Aspect aspect, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        int facing = tile.getFacing();
        switch (facing) {
            case 2 -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case 3 -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case 4 -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case 5 -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
        }
        poseStack.translate(0, 0.5, -0.376);
        VertexConsumer labelConsumer = buffer.getBuffer(RenderType.entityTranslucent(LABEL_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.22f;
        labelConsumer.vertex(matrix, -size, -size, 0).color(255, 255, 255, 255)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, size, -size, 0).color(255, 255, 255, 255)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, -size, size, 0).color(255, 255, 255, 255)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        poseStack.translate(0, 0, -0.001);
        Color color = new Color(aspect.getColor());
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        VertexConsumer iconConsumer = buffer.getBuffer(
                RenderType.entityTranslucent(UtilsFX.strippedAspectTexture(aspect.getImage())));
        Matrix4f iconMatrix = poseStack.last().pose();
        float iconSize = 0.15f;
        iconConsumer.vertex(iconMatrix, -iconSize, -iconSize, 0).color(r, g, b, 1f)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, iconSize, -iconSize, 0).color(r, g, b, 1f)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, iconSize, iconSize, 0).color(r, g, b, 1f)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, -iconSize, iconSize, 0).color(r, g, b, 1f)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        poseStack.popPose();
    }
    private void renderNozzles(TileAlembic tile, PoseStack poseStack,
                               MultiBufferSource buffer, int packedLight) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (!tile.canOutputTo(dir)) continue;
            BlockEntity te = tile.getLevel().getBlockEntity(tile.getBlockPos().relative(dir));
            if (te instanceof IEssentiaTransport transport) {
                if (transport.canInputFrom(dir.getOpposite())) {
                    renderNozzle(dir, poseStack, buffer, packedLight);
                }
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
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
