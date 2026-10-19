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
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.BrainModel;
import arcaneresearch.common.tiles.essentia.TileJarBrain;
@OnlyIn(Dist.CLIENT)
public class JarBrainRenderer implements BlockEntityRenderer<TileJarBrain> {
    private static final ResourceLocation BRAIN_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/brain2.png");
    private static final ResourceLocation BRINE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/jarbrine.png");
    private final BrainModel brainModel;
    private final arcaneresearch.client.models.block.JarModel fittings;
    public JarBrainRenderer(BlockEntityRendererProvider.Context context) {
        this.brainModel = new BrainModel(context.bakeLayer(BrainModel.LAYER_LOCATION));
        this.fittings = new arcaneresearch.client.models.block.JarModel(
                context.bakeLayer(arcaneresearch.client.models.block.JarModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileJarBrain tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float rotation = Mth.lerp(partialTicks, tile.brainRotationPrev, tile.brainRotation);
        float yOffset = Mth.lerp(partialTicks, tile.brainYPrev, tile.brainY);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.01, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -0.77F + yOffset, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        float scale = 0.4F;
        poseStack.scale(scale, scale, scale);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BRAIN_TEXTURE));
        brainModel.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.01, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        VertexConsumer brine = buffer.getBuffer(RenderType.entityTranslucent(BRINE_TEXTURE));
        fittings.renderBrine(poseStack, brine, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
