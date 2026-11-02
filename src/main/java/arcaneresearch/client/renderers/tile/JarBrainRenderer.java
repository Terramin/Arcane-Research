package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.BrainModel;
import arcaneresearch.common.tiles.essentia.TileJarBrain;

@OnlyIn(Dist.CLIENT)
public class JarBrainRenderer implements BlockEntityRenderer<TileJarBrain> {
    private static final ResourceLocation BRAIN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/brain2.png");
    private static final ResourceLocation BRINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jarbrine.png");

    private static final RenderType BRAIN_TYPE = RenderType.entityCutoutNoCull(BRAIN_TEXTURE);
    private static final RenderType BRINE_TYPE = RenderType.entityTranslucent(BRINE_TEXTURE);

    private static final Quaternionf ROT_XP_180 = Axis.XP.rotationDegrees(180.0F);
    private static final Quaternionf ROT_YP_NEG_90 = Axis.YP.rotationDegrees(-90.0F);

    private final BrainModel brainModel;
    private final arcaneresearch.client.models.block.JarModel fittings;

    public JarBrainRenderer(BlockEntityRendererProvider.Context context) {
        this.brainModel = new BrainModel(context.bakeLayer(BrainModel.LAYER_LOCATION));
        this.fittings = new arcaneresearch.client.models.block.JarModel(
                context.bakeLayer(arcaneresearch.client.models.block.JarModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull TileJarBrain tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (brainModel == null || fittings == null) return;

        float rotation = Mth.lerp(partialTicks, tile.brainRotationPrev, tile.brainRotation);
        float yOffset = Mth.lerp(partialTicks, tile.brainYPrev, tile.brainY);

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.01, 0.5);
            poseStack.mulPose(ROT_XP_180);
            poseStack.translate(0.0F, -0.77F + yOffset, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.mulPose(ROT_YP_NEG_90);
            float scale = 0.4F;
            poseStack.scale(scale, scale, scale);
            VertexConsumer vertexConsumer = buffer.getBuffer(BRAIN_TYPE);
            brainModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            poseStack.popPose();
        }

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.01, 0.5);
            poseStack.mulPose(ROT_XP_180);
            VertexConsumer brine = buffer.getBuffer(BRINE_TYPE);
            fittings.renderBrine(poseStack, brine, packedLight, packedOverlay);
        } finally {
            poseStack.popPose();
        }
    }
}