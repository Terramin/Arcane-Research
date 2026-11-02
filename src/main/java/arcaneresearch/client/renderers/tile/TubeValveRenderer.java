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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.TubeValveModel;
import arcaneresearch.common.tiles.essentia.TileTubeValve;

@OnlyIn(Dist.CLIENT)
public class TubeValveRenderer implements BlockEntityRenderer<TileTubeValve> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/valve.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);
    private static final Quaternionf[] FACING_ROTATIONS = new Quaternionf[6];

    static {
        for (Direction facing : Direction.values()) {
            Quaternionf q = new Quaternionf();
            if (facing.getAxis() != Direction.Axis.Y) {
                q.mul(Axis.YP.rotationDegrees(90.0f));
            } else {
                q.mul(Axis.XN.rotationDegrees(90.0f));
                q.mul(Axis.YP.rotationDegrees(90.0f * facing.getStepY()));
            }
            q.mul(Axis.XP.rotationDegrees(90.0f * facing.getStepX()));
            q.mul(Axis.YP.rotationDegrees(90.0f * facing.getStepY()));
            q.mul(Axis.ZP.rotationDegrees(90.0f * facing.getStepZ()));
            FACING_ROTATIONS[facing.ordinal()] = q;
        }
    }

    private final TubeValveModel model;

    public TubeValveRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TubeValveModel(context.bakeLayer(TubeValveModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull TileTubeValve tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        Direction facing = tile.facing;
        if (facing == null) facing = Direction.NORTH;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(FACING_ROTATIONS[facing.ordinal()]);
            float rotation = tile.rotation;
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation * 1.5f));
            float offset = -0.03f - (rotation / 360.0f) * 0.09f;
            poseStack.translate(0, offset, 0);
            poseStack.scale(0.0625f, 0.0625f, 0.0625f);
            VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
            model.renderRing(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.scale(0.75f, 1.0f, 0.75f);
            model.renderRod(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        } finally {
            poseStack.popPose();
        }
    }
}