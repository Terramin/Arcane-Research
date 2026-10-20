package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;
@OnlyIn(Dist.CLIENT)
public class GolemBuilderRenderer implements BlockEntityRenderer<TileGolemBuilder> {
    private static final ResourceLocation MODEL =
            new ResourceLocation(Arcaneresearch.MODID, "models/block/golembuilder.obj");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/golembuilder.png");
    public GolemBuilderRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TileGolemBuilder tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) return;
        VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.objSolid(TEXTURE));
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        Direction facing = Direction.SOUTH;
        BlockState state = tile.getBlockState();
        if (state.hasProperty(BlockTCDevice.FACING)) {
            facing = state.getValue(BlockTCDevice.FACING);
        }
        switch (facing) {
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0f));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            default -> {
            }
        }
        model.renderGroup(poseStack, consumer, "table", packedLight, packedOverlay, 1, 1, 1, 1);
        model.renderGroup(poseStack, consumer, "box", packedLight, packedOverlay, 1, 1, 1, 1);
        model.renderGroup(poseStack, consumer, "anvil", packedLight, packedOverlay, 1, 1, 1, 1);
        poseStack.pushPose();
        float h = (float) tile.pressAnimation;
        poseStack.translate(0.0, -Math.sin(Math.toRadians(h)) * 0.625, 0.0);
        model.renderGroup(poseStack, consumer, "press", packedLight, packedOverlay, 1, 1, 1, 1);
        poseStack.popPose();
        renderLavaPool(tile, poseStack, buffer);
        poseStack.popPose();
    }
    private void renderLavaPool(TileGolemBuilder tile, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(-0.3125, 0.625, 0.3125 + 1.0);
        poseStack.mulPose(Axis.XN.rotationDegrees(90));
        TextureAtlasSprite lavaSprite = Minecraft.getInstance().getBlockRenderer()
                .getBlockModelShaper().getTexture(Blocks.LAVA.defaultBlockState(),
                        tile.getLevel(), tile.getBlockPos());
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.625f;
        int lavaLight = 200;
        float u0 = lavaSprite.getU0();
        float u1 = lavaSprite.getU1();
        float v0 = lavaSprite.getV0();
        float v1 = lavaSprite.getV1();
        vertexConsumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255)
                .uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lavaLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, 0, 0).color(255, 255, 255, 255)
                .uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lavaLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                .uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lavaLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0, size, 0).color(255, 255, 255, 255)
                .uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(lavaLight).normal(0, 0, 1).endVertex();
        poseStack.popPose();
    }
}
