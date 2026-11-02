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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;

@OnlyIn(Dist.CLIENT)
public class GolemBuilderRenderer implements BlockEntityRenderer<TileGolemBuilder> {
    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "models/block/golembuilder.obj");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/golembuilder.png");

    private static final RenderType OBJ_TYPE = TCRenderTypes.objSolid(TEXTURE);
    private static final RenderType LAVA_TYPE = RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS);

    private static final Quaternionf ROT_YP_270 = Axis.YP.rotationDegrees(270.0f);
    private static final Quaternionf ROT_YP_90 = Axis.YP.rotationDegrees(90.0f);
    private static final Quaternionf ROT_YP_180 = Axis.YP.rotationDegrees(180.0f);
    private static final Quaternionf ROT_XN_90 = Axis.XN.rotationDegrees(90.0f);

    private static final int LAVA_LIGHT = 200;
    private static final float LAVA_SIZE = 0.625f;

    public GolemBuilderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull TileGolemBuilder tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) return;

        BlockState state = tile.getBlockState();
        Direction facing = Direction.SOUTH;
        if (state.hasProperty(BlockTCDevice.FACING)) {
            Direction d = state.getValue(BlockTCDevice.FACING);
            facing = d;
        }

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0, 0.5);
            switch (facing) {
                case EAST -> poseStack.mulPose(ROT_YP_270);
                case WEST -> poseStack.mulPose(ROT_YP_90);
                case NORTH -> poseStack.mulPose(ROT_YP_180);
                default -> {}
            }

            VertexConsumer consumer = buffer.getBuffer(OBJ_TYPE);
            model.renderGroup(poseStack, consumer, "table", packedLight, packedOverlay, 1, 1, 1, 1);
            model.renderGroup(poseStack, consumer, "box", packedLight, packedOverlay, 1, 1, 1, 1);
            model.renderGroup(poseStack, consumer, "anvil", packedLight, packedOverlay, 1, 1, 1, 1);

            poseStack.pushPose();
            try {
                float h = (float) tile.pressAnimation;
                poseStack.translate(0.0, -Math.sin(Math.toRadians(h)) * 0.625, 0.0);
                model.renderGroup(poseStack, consumer, "press", packedLight, packedOverlay, 1, 1, 1, 1);
            } finally {
                poseStack.popPose();
            }

            renderLavaPool(level, tile.getBlockPos(), poseStack, buffer);
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderLavaPool(Level level, BlockPos pos, PoseStack poseStack, MultiBufferSource buffer) {
        if (level == null || pos == null) return;
        Minecraft mc = Minecraft.getInstance();
        TextureAtlasSprite lavaSprite = mc.getBlockRenderer()
                .getBlockModelShaper().getTexture(Blocks.LAVA.defaultBlockState(), level, pos);

        poseStack.pushPose();
        try {
            poseStack.translate(-0.3125, 0.625, 0.3125 + 1.0);
            poseStack.mulPose(ROT_XN_90);
            VertexConsumer vertexConsumer = buffer.getBuffer(LAVA_TYPE);
            Matrix4f matrix = poseStack.last().pose();
            float size = LAVA_SIZE;
            float u0 = lavaSprite.getU0();
            float u1 = lavaSprite.getU1();
            float v0 = lavaSprite.getV0();
            float v1 = lavaSprite.getV1();
            vertexConsumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255)
                    .uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LAVA_LIGHT).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, size, 0, 0).color(255, 255, 255, 255)
                    .uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LAVA_LIGHT).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255)
                    .uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LAVA_LIGHT).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, 0, size, 0).color(255, 255, 255, 255)
                    .uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LAVA_LIGHT).normal(0, 0, 1).endVertex();
        } finally {
            poseStack.popPose();
        }
    }
}