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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TilePatternCrafter;

@OnlyIn(Dist.CLIENT)
public class PatternCrafterRenderer implements BlockEntityRenderer<TilePatternCrafter> {
    private static final ResourceLocation MODES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/pattern_crafter_modes.png");
    private static final ResourceLocation GEAR_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/gear_brass.png");
    private static final RenderType MODES_TYPE = RenderType.entityCutout(MODES_TEXTURE);
    private static final RenderType GEAR_TYPE = RenderType.entityCutout(GEAR_TEXTURE);
    private static final Quaternionf ROT_ZP_90 = Axis.ZP.rotationDegrees(90.0f);
    private static final Quaternionf[] FACING_ROTATIONS = new Quaternionf[6];

    static {
        for (Direction dir : Direction.values()) {
            float rotation = switch (dir) {
                case EAST -> 90.0f;
                case WEST -> 270.0f;
                case NORTH -> 180.0f;
                default -> 0.0f;
            };
            FACING_ROTATIONS[dir.ordinal()] = rotation == 0.0f
                    ? new Quaternionf()
                    : Axis.YP.rotationDegrees(rotation);
        }
    }

    public PatternCrafterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull TilePatternCrafter tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        BlockState state = tile.getBlockState();
        Direction facing = Direction.NORTH;
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.75, 0.5);
            poseStack.mulPose(FACING_ROTATIONS[facing.ordinal()]);

            poseStack.pushPose();
            try {
                poseStack.mulPose(ROT_ZP_90);
                poseStack.translate(0, 0, -0.5);
                renderModeDisplay(poseStack, buffer, packedLight, tile.type);
            } finally {
                poseStack.popPose();
            }

            float rotMod = tile.rot % 360.0f;
            renderGear(poseStack, buffer, packedLight, -0.2f, -rotMod);
            renderGear(poseStack, buffer, packedLight, 0.2f, rotMod);
        } finally {
            poseStack.popPose();
        }
    }

    private void renderModeDisplay(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int type) {
        VertexConsumer vertexConsumer = buffer.getBuffer(MODES_TYPE);
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

    private void renderGear(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                            float xOffset, float gearRot) {
        poseStack.pushPose();
        try {
            poseStack.translate(xOffset, -0.40625, 0.05);
            poseStack.mulPose(Axis.ZP.rotationDegrees(gearRot));
            poseStack.scale(0.5f, 0.5f, 1.0f);
            poseStack.translate(-0.5, -0.5, 0);
            VertexConsumer vertexConsumer = buffer.getBuffer(GEAR_TYPE);
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
        } finally {
            poseStack.popPose();
        }
    }
}