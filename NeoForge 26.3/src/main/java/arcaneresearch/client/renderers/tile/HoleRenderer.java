package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.tiles.misc.TileHole;
import arcaneresearch.init.ModBlocks;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (void face on each solid-neighbour side, end-portal shader quad).
public class HoleRenderer implements BlockEntityRenderer<TileHole, HoleRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        // NEOFORGE_PORT: per-face solidity stashed (level reads forbidden in submit).
        public boolean[] solid = new boolean[6];
    }

    public HoleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileHole tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        if (state.hasLevel) {
            for (Direction face : Direction.values()) {
                BlockState adjacentState = tile.getLevel().getBlockState(tile.getBlockPos().relative(face));
                // NEOFORGE_PORT: isSolidRender(level, pos) -> no-arg (cached).
                state.solid[face.ordinal()] = adjacentState.isSolidRender()
                        && !adjacentState.is(ModBlocks.HOLE.get());
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        RenderType type = RenderTypes.endPortal();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        for (Direction face : Direction.values()) {
            if (!state.solid[face.ordinal()]) {
                continue;
            }
            poseStack.pushPose();

            switch (face) {
                case DOWN -> poseStack.rotate(Axis.XP.rotationDegrees(-90));
                case UP -> poseStack.rotate(Axis.XP.rotationDegrees(90));
                case NORTH -> {
                }
                case SOUTH -> poseStack.rotate(Axis.YP.rotationDegrees(180));
                case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(90));
                case EAST -> poseStack.rotate(Axis.YP.rotationDegrees(-90));
            }

            poseStack.translate(0, 0, -0.499);
            float half = 0.5f;
            // NEOFORGE_PORT: Forge СЃР»Р°Р» С‚РѕР»СЊРєРѕ РїРѕР·РёС†РёРё (POSITION-С„РѕСЂРјР°С‚ РїРѕСЂС‚Р°Р»Р°);
            // С€РµР№РґРµСЂ СЃС‡РёС‚Р°РµС‚ РїСЂРѕРµРєС†РёСЋ СЃР°Рј вЂ” С‚РѕС‚ Р¶Рµ РјРёРЅРёРјСѓРј.
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                consumer.addVertex(pose, -half, -half, 0);
                consumer.addVertex(pose, half, -half, 0);
                consumer.addVertex(pose, half, half, 0);
                consumer.addVertex(pose, -half, half, 0);
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}