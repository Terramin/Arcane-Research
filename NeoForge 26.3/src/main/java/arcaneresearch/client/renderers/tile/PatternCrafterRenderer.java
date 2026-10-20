package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.crafting.TilePatternCrafter;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (mode display + two meshing gears).
public class PatternCrafterRenderer implements BlockEntityRenderer<TilePatternCrafter, PatternCrafterRenderer.State> {

    private static final Identifier MODES_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/pattern_crafter_modes.png");
    private static final Identifier GEAR_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/gear_brass.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public Direction facing = Direction.NORTH;
        public int type;
        public float rot;
    }

    public PatternCrafterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TilePatternCrafter tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        Direction facing = Direction.NORTH;
        if (tile.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        state.facing = facing;
        state.type = tile.type;
        state.rot = tile.rot;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);

        float rotation = switch (state.facing) {
            case EAST -> 90.0f;
            case WEST -> 270.0f;
            case NORTH -> 180.0f;
            case SOUTH -> 0.0f;
            default -> 0.0f;
        };
        poseStack.rotate(Axis.YP.rotationDegrees(rotation));

        poseStack.pushPose();
        poseStack.rotate(Axis.ZP.rotationDegrees(90));
        poseStack.translate(0, 0, -0.5);

        renderModeDisplay(poseStack, collector, state.lightCoords, state.type);

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(-0.2, -0.40625, 0.05);
        float gearRot = -state.rot % 360.0f;
        poseStack.rotate(Axis.ZP.rotationDegrees(gearRot));
        poseStack.scale(0.5f, 0.5f, 1.0f);
        poseStack.translate(-0.5, -0.5, 0);

        renderGear(poseStack, collector, state.lightCoords);

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.2, -0.40625, 0.05);
        gearRot = state.rot % 360.0f;
        poseStack.rotate(Axis.ZP.rotationDegrees(gearRot));
        poseStack.scale(0.5f, 0.5f, 1.0f);
        poseStack.translate(-0.5, -0.5, 0);

        renderGear(poseStack, collector, state.lightCoords);

        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderModeDisplay(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int type) {
        RenderType renderType = TCRenderTypes.objSolid(MODES_TEXTURE);

        float uMin = type / 10.0f;
        float uMax = (type + 1) / 10.0f;
        float vMin = 0;
        float vMax = 1;

        float size = 0.5f;

        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            quad(consumer, pose, -size, -size, 0, uMin, vMax, packedLight);
            quad(consumer, pose, size, -size, 0, uMax, vMax, packedLight);
            quad(consumer, pose, size, size, 0, uMax, vMin, packedLight);
            quad(consumer, pose, -size, size, 0, uMin, vMin, packedLight);
        });
    }

    private void renderGear(PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        RenderType renderType = TCRenderTypes.objSolid(GEAR_TEXTURE);

        float size = 1.0f;

        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            quad(consumer, pose, 0, 0, 0, 0, 1, packedLight);
            quad(consumer, pose, size, 0, 0, 1, 1, packedLight);
            quad(consumer, pose, size, size, 0, 1, 0, packedLight);
            quad(consumer, pose, 0, size, 0, 0, 0, packedLight);
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }
}