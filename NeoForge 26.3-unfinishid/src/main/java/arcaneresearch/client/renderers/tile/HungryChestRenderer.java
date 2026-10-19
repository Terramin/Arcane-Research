package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.devices.TileHungryChest;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (facing rotation + lid animation via submitModelPart).
public class HungryChestRenderer implements BlockEntityRenderer<TileHungryChest, HungryChestRenderer.State> {

    private static final net.minecraft.resources.Identifier TEXTURE =
            net.minecraft.resources.Identifier.fromNamespaceAndPath(arcaneresearch.Arcaneresearch.MODID,
                    "textures/block/chesthungry.png");

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.SOUTH;
        public float lidXRot;
    }

    private final net.minecraft.client.model.geom.ModelPart lid;
    private final net.minecraft.client.model.geom.ModelPart bottom;
    private final net.minecraft.client.model.geom.ModelPart lock;

    public HungryChestRenderer(BlockEntityRendererProvider.Context context) {

        net.minecraft.client.model.geom.ModelPart modelPart = context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.CHEST);
        this.bottom = modelPart.getChild("bottom");
        this.lid = modelPart.getChild("lid");
        this.lock = modelPart.getChild("lock");
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileHungryChest tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        Direction facing = Direction.SOUTH;
        if (tile.getLevel() != null && tile.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        state.facing = facing;

        float lidAngle = tile.prevLidAngle + (tile.lidAngle - tile.prevLidAngle) * partialTicks;
        lidAngle = 1.0f - lidAngle;
        lidAngle = 1.0f - lidAngle * lidAngle * lidAngle;
        state.lidXRot = -(lidAngle * ((float) Math.PI / 2.0f));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(-state.facing.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);

        lid.xRot = state.lidXRot;
        lock.xRot = state.lidXRot;

        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        collector.submitModelPart(lid, poseStack, type, state.lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, 0);
        collector.submitModelPart(lock, poseStack, type, state.lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, 0);
        collector.submitModelPart(bottom, poseStack, type, state.lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, 0);

        poseStack.popPose();
    }
}