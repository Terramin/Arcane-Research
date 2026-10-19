package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.client.models.block.TubeValveModel;
import arcaneresearch.common.tiles.essentia.TileTubeBuffer;
@OnlyIn(Dist.CLIENT)
public class TubeBufferRenderer implements BlockEntityRenderer<TileTubeBuffer> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/valve.png");
    private final TubeValveModel model;
    public TubeBufferRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TubeValveModel(context.bakeLayer(TubeValveModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileTubeBuffer tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        for (Direction dir : Direction.values()) {
            if (tile.chokedSides[dir.ordinal()] != 0 && tile.openSides[dir.ordinal()]) {
                BlockEntity connectedTile = ArcaneresearchApiHelper.getConnectableTile(
                        tile.getLevel(), tile.getBlockPos(), dir);
                if (connectedTile != null) {
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);
                    applyDirectionRotation(poseStack, dir.getOpposite());
                    float r, g, b;
                    if (tile.chokedSides[dir.ordinal()] == 2) {
                        r = 1.0f;
                        g = 0.3f;
                        b = 0.3f;
                    } else {
                        r = 0.3f;
                        g = 0.3f;
                        b = 1.0f;
                    }
                    poseStack.scale(0.0625f, 0.0625f, 0.0625f);
                    poseStack.scale(2.0f, 1.0f, 2.0f);
                    poseStack.translate(0, -0.5 / 0.0625, 0);
                    model.renderRod(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, 1.0f);
                    poseStack.popPose();
                }
            }
        }
    }
    private void applyDirectionRotation(PoseStack poseStack, Direction dir) {
        if (dir.getAxis() != Direction.Axis.Y) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        } else {
            poseStack.mulPose(Axis.XN.rotationDegrees(90));
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f * dir.getStepY()));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f * dir.getStepX()));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f * dir.getStepY()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f * dir.getStepZ()));
    }
}
