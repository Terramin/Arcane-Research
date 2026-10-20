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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.tiles.crafting.TileGolemBuilder;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (OBJ groups + press animation + lava pool quad).
public class GolemBuilderRenderer implements BlockEntityRenderer<TileGolemBuilder, GolemBuilderRenderer.State> {

    private static final Identifier MODEL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/block/golembuilder.obj");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/golembuilder.png");
    private static final Identifier LAVA_STILL =
            Identifier.withDefaultNamespace("block/lava_still");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public Direction facing = Direction.SOUTH;
        public double pressAnimation;
    }

    public GolemBuilderRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileGolemBuilder tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        Direction facing = Direction.SOUTH;
        BlockState blockState = tile.getBlockState();
        if (blockState.hasProperty(BlockTCDevice.FACING)) {
            facing = blockState.getValue(BlockTCDevice.FACING);
        }
        state.facing = facing;
        state.pressAnimation = tile.pressAnimation;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }

        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) return;

        RenderType type = TCRenderTypes.objSolid(TEXTURE);
        int light = state.lightCoords;
        int overlay = OverlayTexture.NO_OVERLAY;

        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);

        switch (state.facing) {
            case EAST -> poseStack.rotate(Axis.YP.rotationDegrees(270.0f));
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(90.0f));
            case NORTH -> poseStack.rotate(Axis.YP.rotationDegrees(180.0f));
            default -> {
            }
        }

        emitObj(poseStack, collector, type, light, overlay, model, "table");
        emitObj(poseStack, collector, type, light, overlay, model, "box");
        emitObj(poseStack, collector, type, light, overlay, model, "anvil");

        poseStack.pushPose();
        float h = (float) state.pressAnimation;
        poseStack.translate(0.0, -Math.sin(Math.toRadians(h)) * 0.625, 0.0);
        emitObj(poseStack, collector, type, light, overlay, model, "press");
        poseStack.popPose();

        renderLavaPool(poseStack, collector);

        poseStack.popPose();
    }

    private static void emitObj(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                int light, int overlay, TurretObjModel model, String group) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.renderGroup(work, consumer, group, light, overlay, 1, 1, 1, 1);
        });
    }

    private void renderLavaPool(PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();
        poseStack.translate(-0.3125, 0.625, 0.3125 + 1.0);
        poseStack.rotate(Axis.XN.rotationDegrees(90));

        TextureAtlasSprite lavaSprite;
        try {
            lavaSprite = net.minecraft.client.Minecraft.getInstance().getAtlasManager().get(
                    new net.minecraft.client.resources.model.sprite.SpriteId(
                            TextureAtlas.LOCATION_BLOCKS, LAVA_STILL));
        } catch (Exception e) {
            poseStack.popPose();
            return;
        }

        // NEOFORGE_PORT (26.3): sprite UVs are block-atlas UVs, so the BLOCKS atlas must
        // be bound (Forge entitySolid(LOCATION_BLOCKS)); binding "block/lava_still" as a
        // standalone texture sampled a missing texture.
        RenderType lavaType = net.minecraft.client.renderer.rendertype.RenderTypes
                .entitySolid(TextureAtlas.LOCATION_BLOCKS);
        float size = 0.625f;
        int lavaLight = 200;
        float u0 = lavaSprite.getU0();
        float u1 = lavaSprite.getU1();
        float v0 = lavaSprite.getV0();
        float v1 = lavaSprite.getV1();

        collector.submitCustomGeometry(poseStack, lavaType, (pose, consumer) -> {
            quad(consumer, pose, 0, 0, 0, u0, v0, lavaLight);
            quad(consumer, pose, size, 0, 0, u1, v0, lavaLight);
            quad(consumer, pose, size, size, 0, u1, v1, lavaLight);
            quad(consumer, pose, 0, size, 0, u0, v1, lavaLight);
        });

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    // NEOFORGE_PORT: render bounds moved from BlockEntity to the renderer in 26.x
    // (IBlockEntityRendererExtension); forward the tile's Forge-parity culling box.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TileGolemBuilder blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
