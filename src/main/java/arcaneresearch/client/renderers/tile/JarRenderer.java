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
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.essentia.TileJar;

import java.awt.*;



public class JarRenderer implements BlockEntityRenderer<TileJar, JarRenderer.State> {

    private static final Identifier LIQUID_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/animatedglow.png");
    private static final int LIQUID_FRAMES = 20;

    private static final int LIQUID_CYCLE = 38;
    private static final Identifier LABEL_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/label.png");
    private static final Identifier BRINE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jarbrine.png");

    public static class State extends BlockEntityRenderState {
        public boolean blocked;
        public boolean connectedUp;
        public int amount;
        public Aspect aspect;
        public Aspect filter;
        public int facing;
        public boolean crooked;
        public int crookedSeed;
        public long gameTime;
    }

    private final arcaneresearch.client.models.block.JarModel fittings;

    public JarRenderer(BlockEntityRendererProvider.Context context) {
        this.fittings = new arcaneresearch.client.models.block.JarModel(
                context.bakeLayer(arcaneresearch.client.models.block.JarModel.LAYER_LOCATION));
    }

    private static int getLiquidFrame(long gameTime) {
        int step = (int) ((gameTime / 2) % LIQUID_CYCLE);
        return step < LIQUID_FRAMES ? step : LIQUID_CYCLE - step;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileJar tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.blocked = tile.isBlocked();
        state.connectedUp = tile.getLevel() != null && arcaneresearch.api.ArcaneresearchApiHelper.getConnectableTile(
                tile.getLevel(), tile.getBlockPos(), net.minecraft.core.Direction.UP) != null;
        state.amount = tile.getAmount();
        state.aspect = tile.getAspect();
        state.filter = tile.getAspectFilter();
        state.facing = tile.getFacing();
        state.crooked = arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.crooked && tile.getLevel() != null;
        state.crookedSeed = tile.getBlockPos().getX() + state.facing;
        state.gameTime = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0L;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        RenderType brineType = TCRenderTypes.objSolid(BRINE_TEXTURE);
        int light = state.lightCoords;
        if (state.blocked) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.01, 0.5);
            poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
            collector.submitCustomGeometry(poseStack, brineType, (pose, consumer) -> {
                PoseStack work = new PoseStack();
                work.last().pose().set(pose.pose());
                work.last().normal().set(pose.normal());
                fittings.renderLidBrace(work, consumer, light, OverlayTexture.NO_OVERLAY);
            });
            poseStack.popPose();
        }
        if (state.connectedUp) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.01, 0.5);
            poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
            collector.submitCustomGeometry(poseStack, brineType, (pose, consumer) -> {
                PoseStack work = new PoseStack();
                work.last().pose().set(pose.pose());
                work.last().normal().set(pose.normal());
                fittings.renderLidExtension(work, consumer, light, OverlayTexture.NO_OVERLAY);
            });
            poseStack.popPose();
        }

        if (state.amount > 0 && state.aspect != null) {
            renderLiquid(poseStack, collector, state);
        }

        if (state.filter != null) {
            renderLabel(poseStack, collector, state);
        }
    }

    private void renderLiquid(PoseStack poseStack, SubmitNodeCollector collector, State state) {
        Aspect aspect = state.aspect;
        int amount = state.amount;
        float fillLevel = amount / 250.0f;
        float liquidHeight = 0.0625f + fillLevel * 0.625f;

        Color color = new Color(aspect.getColor());
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = 0.8f;

        float minX = 0.251f;
        float maxX = 0.749f;
        float minY = 0.0625f;
        float maxY = liquidHeight;
        float minZ = 0.251f;
        float maxZ = 0.749f;

        int light = 0x00F000F0;

        int frame = getLiquidFrame(state.gameTime);
        float minU = 0, maxU = 1;
        float minV = frame / (float) LIQUID_FRAMES;
        float maxV = (frame + 1) / (float) LIQUID_FRAMES;

        RenderType type = TCRenderTypes.translucentPortal(LIQUID_TEXTURE);
        
        emitQuad(poseStack, collector, type,
                minX, minY, minZ, maxX, minY, maxZ,
                0, -1, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        emitQuad(poseStack, collector, type,
                minX, maxY, maxZ, maxX, maxY, minZ,
                0, 1, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        emitQuad(poseStack, collector, type,
                maxX, minY, minZ, minX, maxY, minZ,
                0, 0, -1, r, g, b, a, light, minU, maxU, minV, maxV);
        emitQuad(poseStack, collector, type,
                minX, minY, maxZ, maxX, maxY, maxZ,
                0, 0, 1, r, g, b, a, light, minU, maxU, minV, maxV);
        emitQuad(poseStack, collector, type,
                minX, minY, minZ, minX, maxY, maxZ,
                -1, 0, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        emitQuad(poseStack, collector, type,
                maxX, minY, maxZ, maxX, maxY, minZ,
                1, 0, 0, r, g, b, a, light, minU, maxU, minV, maxV);
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float nx, float ny, float nz, float r, float g, float b, float a, int light,
                                 float minU, float maxU, float minV, float maxV) {
        float[] v;
        if (ny != 0) {
            v = new float[]{
                    x1, y1, z1, minU, minV,
                    x1, y1, z2, minU, maxV,
                    x2, y1, z2, maxU, maxV,
                    x2, y1, z1, maxU, minV};
        } else if (nz != 0) {
            v = new float[]{
                    x1, y1, z1, minU, maxV,
                    x2, y1, z1, maxU, maxV,
                    x2, y2, z1, maxU, minV,
                    x1, y2, z1, minU, minV};
        } else {
            v = new float[]{
                    x1, y1, z1, minU, maxV,
                    x1, y1, z2, maxU, maxV,
                    x1, y2, z2, maxU, minV,
                    x1, y2, z1, minU, minV};
        }
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            for (int i = 0; i < 4; i++) {
                consumer.addVertex(pose, v[i * 5], v[i * 5 + 1], v[i * 5 + 2])
                        .setColor(r, g, b, a)
                        .setUv(v[i * 5 + 3], v[i * 5 + 4])
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(light)
                        .setNormal(pose, nx, ny, nz);
            }
        });
    }

    private void renderLabel(PoseStack poseStack, SubmitNodeCollector collector, State state) {
        Aspect aspect = state.filter;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        int facing = state.facing;
        switch (facing) {
            case 2 -> poseStack.rotate(Axis.YP.rotationDegrees(0));
            case 3 -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case 4 -> poseStack.rotate(Axis.YP.rotationDegrees(270));
            case 5 -> poseStack.rotate(Axis.YP.rotationDegrees(90));
        }

        poseStack.translate(0, -0.1, 0.315);

        if (state.crooked) {
            float rot = (float) (((aspect.getTag().hashCode() + state.crookedSeed) % 4) - 2);
            poseStack.rotate(Axis.ZP.rotationDegrees(rot));
        }

        poseStack.scale(0.5f, 0.5f, 0.5f);

        RenderType labelType = TCRenderTypes.translucentPortal(LABEL_TEXTURE);
        int light = state.lightCoords;
        float size = 0.5f;
        collector.submitCustomGeometry(poseStack, labelType, (pose, consumer) -> {
            quad(consumer, pose, -size, -size, 0, 0, 1, light, 1, 1, 1, 1, 0, 0, 1);
            quad(consumer, pose, size, -size, 0, 1, 1, light, 1, 1, 1, 1, 0, 0, 1);
            quad(consumer, pose, size, size, 0, 1, 0, light, 1, 1, 1, 1, 0, 0, 1);
            quad(consumer, pose, -size, size, 0, 0, 0, light, 1, 1, 1, 1, 0, 0, 1);
        });

        poseStack.translate(0, 0, 0.01);
        poseStack.scale(0.03f, 0.03f, 0.03f);

        Color color = new Color(aspect.getColor());
        
        RenderType iconType = TCRenderTypes.translucentPortal(UtilsFX.strippedAspectTexture(aspect.getImage()));
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        float iconSize = 8;
        collector.submitCustomGeometry(poseStack, iconType, (pose, consumer) -> {
            quad(consumer, pose, -iconSize, -iconSize, 0, 0, 1, 0x00F000F0, r, g, b, 1, 0, 0, 1);
            quad(consumer, pose, iconSize, -iconSize, 0, 1, 1, 0x00F000F0, r, g, b, 1, 0, 0, 1);
            quad(consumer, pose, iconSize, iconSize, 0, 1, 0, 0x00F000F0, r, g, b, 1, 0, 0, 1);
            quad(consumer, pose, -iconSize, iconSize, 0, 0, 0, 0x00F000F0, r, g, b, 1, 0, 0, 1);
        });

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light,
                             float r, float g, float b, float a,
                             float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}