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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TilePatternCrafter;
@OnlyIn(Dist.CLIENT)
public class PatternCrafterRenderer implements BlockEntityRenderer<TilePatternCrafter> {
    private static final ResourceLocation MODES_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/pattern_crafter_modes.png");
    private static final ResourceLocation GEAR_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/gear_brass.png");
    public PatternCrafterRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TilePatternCrafter tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        Direction facing = Direction.NORTH;
        if (tile.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        float rotation = switch (facing) {
            case EAST -> 90.0f;
            case WEST -> 270.0f;
            case NORTH -> 180.0f;
            case SOUTH -> 0.0f;
            default -> 0.0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90));
        poseStack.translate(0, 0, -0.5);
        renderModeDisplay(tile, poseStack, buffer, packedLight, tile.type);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(-0.2, -0.40625, 0.05);
        float gearRot = -tile.rot % 360.0f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(gearRot));
        poseStack.scale(0.5f, 0.5f, 1.0f);
        poseStack.translate(-0.5, -0.5, 0);
        renderGear(poseStack, buffer, packedLight);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.2, -0.40625, 0.05);
        gearRot = tile.rot % 360.0f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(gearRot));
        poseStack.scale(0.5f, 0.5f, 1.0f);
        poseStack.translate(-0.5, -0.5, 0);
        renderGear(poseStack, buffer, packedLight);
        poseStack.popPose();
        poseStack.popPose();
    }
    private void renderModeDisplay(TilePatternCrafter tile, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, int type) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(MODES_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float uMin = type / 10.0f;
        float uMax = (type + 1) / 10.0f;
        float vMin = 0;
        float vMax = 1;
        float size = 0.5f;
        vertexConsumer.vertex(matrix, -size, -size, 0).color(255, 255, 255, 255)
                .uv(uMin, vMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, -size, 0).color(255, 255, 255, 255)
                .uv(uMax, vMax).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                .uv(uMax, vMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -size, size, 0).color(255, 255, 255, 255)
                .uv(uMin, vMin).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
    private void renderGear(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(GEAR_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float size = 1.0f;
        vertexConsumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, 0, 0).color(255, 255, 255, 255)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0, size, 0).color(255, 255, 255, 255)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
    }
}
