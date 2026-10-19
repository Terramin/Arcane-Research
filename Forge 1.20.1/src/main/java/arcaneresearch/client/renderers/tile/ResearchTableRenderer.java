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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.ResearchTableModel;
import arcaneresearch.common.blocks.crafting.BlockResearchTable;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import java.awt.*;
@OnlyIn(Dist.CLIENT)
public class ResearchTableRenderer implements BlockEntityRenderer<TileResearchTable> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/research_table_model.png");
    private static final ResourceLocation QUILL_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/research/quill.png");
    private final ResearchTableModel tableModel;
    public ResearchTableRenderer(BlockEntityRendererProvider.Context context) {
        this.tableModel = new ResearchTableModel(context.bakeLayer(ResearchTableModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileResearchTable tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = getFacing(tile.getBlockState());
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        applyRotation(poseStack, facing);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        if (tile.hasResearchData()) {
            Color color = new Color(Aspect.ALCHEMY.getColor());
            float r = color.getRed() / 255f;
            float g = color.getGreen() / 255f;
            float b = color.getBlue() / 255f;
            tableModel.renderScroll(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, 1.0f);
        }
        if (tile.hasScribeTools()) {
            VertexConsumer inkwellBuffer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
            tableModel.renderInkwell(poseStack, inkwellBuffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(-0.3, 0.05, -0.2);
            poseStack.mulPose(Axis.YP.rotationDegrees(60));
            poseStack.translate(0.25f, 0.25f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.translate(-0.25f, -0.25f, 0.0f);
            poseStack.scale(0.5f, 0.5f, 0.5f);
            VertexConsumer quillBuffer = buffer.getBuffer(RenderType.entityTranslucent(QUILL_TEXTURE));
            UtilsFX.renderItemIn2D(QUILL_TEXTURE, 0.0625f, quillBuffer, poseStack, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    private void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            default -> {
            }
        }
    }
    private Direction getFacing(BlockState state) {
        if (state.hasProperty(BlockResearchTable.FACING)) {
            return state.getValue(BlockResearchTable.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }
}