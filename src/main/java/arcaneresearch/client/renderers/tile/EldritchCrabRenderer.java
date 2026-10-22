package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchCrabTile;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

 






 
public class EldritchCrabRenderer implements BlockEntityRenderer<EldritchCrabTile, EldritchCrabRenderer.State> {
    private static final Identifier VENT_MODEL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/block/crabvent.obj");
    private static final Identifier VENT_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/crabvent.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public byte facing;
    }

      
    private static volatile TurretObjModel VENT_OBJ = null;
    private static volatile boolean VENT_TRIED = false;

      
    private static TurretObjModel ventModel() {
        if (!VENT_TRIED) {
            synchronized (EldritchCrabRenderer.class) {
                if (!VENT_TRIED) {
                    VENT_TRIED = true;
                    try {
                        VENT_OBJ = TurretObjModel.load(VENT_MODEL);
                    } catch (Throwable ignored) {
                        VENT_OBJ = null;
                    }
                }
            }
        }
        return VENT_OBJ;
    }

    public EldritchCrabRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchCrabTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        byte facing = 0;
        try {
            facing = tile.getFacing();
        } catch (Exception ignored) {
        }
        state.facing = facing;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        TurretObjModel model;
        try {
            model = ventModel();
        } catch (Throwable ignored) {
            return;
        }
        if (model == null || !model.hasGroup("Vent")) {
            return;
        }
        try {
            RenderType type = TCRenderTypes.objSolid(VENT_TEX);
            int light = state.lightCoords;
            byte facing = state.facing;
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            switch (facing) {
                case 0 -> poseStack.rotate(Axis.YP.rotationDegrees(180.0F));
                case 2 -> poseStack.rotate(Axis.YP.rotationDegrees(270.0F));
                case 3 -> poseStack.rotate(Axis.YP.rotationDegrees(90.0F));
                default -> {
                }
            }
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                PoseStack work = new PoseStack();
                work.last().pose().set(pose.pose());
                work.last().normal().set(pose.normal());
                model.renderGroup(work, consumer, "Vent", light, OverlayTexture.NO_OVERLAY,
                        1.0F, 1.0F, 1.0F, 1.0F);
            });
            poseStack.popPose();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    
    
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(EldritchCrabTile blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
