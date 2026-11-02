package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.CentrifugeModel;
import arcaneresearch.common.tiles.essentia.TileCentrifuge;

@OnlyIn(Dist.CLIENT)
public class CentrifugeRenderer implements BlockEntityRenderer<TileCentrifuge> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/centrifuge.png");

    private static final float MODEL_SCALE = 0.0625f;
    private static final float FULL_WHITE = 1.0f;

    private final CentrifugeModel model;

    public CentrifugeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CentrifugeModel(context.bakeLayer(CentrifugeModel.LAYER_LOCATION));
    }

    @Override
    public void render(TileCentrifuge tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        model.renderStaticParts(poseStack, vertexConsumer, packedLight, packedOverlay,
                FULL_WHITE, FULL_WHITE, FULL_WHITE, FULL_WHITE);

        model.setSpinRotation(tile.rotation * Mth.DEG_TO_RAD);
        model.renderSpinnyBit(poseStack, vertexConsumer, packedLight, packedOverlay,
                FULL_WHITE, FULL_WHITE, FULL_WHITE, FULL_WHITE);

        poseStack.popPose();
    }
}