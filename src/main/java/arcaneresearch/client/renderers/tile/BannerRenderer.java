package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.BannerModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.misc.TileBanner;

import java.awt.*;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (texture select, facing/wave animation, aspect icon).
public class BannerRenderer implements BlockEntityRenderer<TileBanner, BannerRenderer.State> {

    private static final Identifier TEX_CULT =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/banner_cultist.png");
    private static final Identifier TEX_BLANK =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/banner_blank.png");
    private static final int ICON_SIZE = 16;

    public static class State extends BlockEntityRenderState {
        public boolean cult;
        public boolean hasLevel;
        public boolean wall;
        public float bannerFacing;
        public int color = -1;
        public float waveRx;
        public Aspect aspect;
        public float partialTicks;
        public int playerTickCount;
    }

    private final BannerModel model;

    public BannerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BannerModel(context.bakeLayer(BannerModel.LAYER_LOCATION));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileBanner banner, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(banner, state, partialTicks, cameraPosition, breakProgress);
        state.cult = banner.getAspect() == null && banner.getColor() == -1;
        state.hasLevel = banner.getLevel() != null;
        state.wall = banner.getWall();
        state.bannerFacing = banner.getBannerFacing() * 360.0f / 16.0f;
        state.color = banner.getColor();
        state.aspect = banner.getAspect();
        state.partialTicks = partialTicks;
        Player player = Minecraft.getInstance().player;
        state.playerTickCount = player != null ? player.tickCount : 0;
        if (player != null && state.hasLevel) {
            float time = banner.getBlockPos().getX() * 7 +
                    banner.getBlockPos().getY() * 9 +
                    banner.getBlockPos().getZ() * 13 +
                    player.tickCount + partialTicks;
            state.waveRx = 0.02f - Mth.sin(time / 11.0f) * 0.02f;
        } else {
            state.waveRx = 0.0f;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        Identifier texture = state.cult ? TEX_CULT : TEX_BLANK;

        poseStack.pushPose();

        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.rotate(Axis.XP.rotationDegrees(180.0f));

        if (state.hasLevel) {
            poseStack.rotate(Axis.YP.rotationDegrees(180.0f));
            poseStack.rotate(Axis.YP.rotationDegrees(state.bannerFacing));
        }

        RenderType type = TCRenderTypes.objSolid(texture);
        int light = state.lightCoords;
        boolean wall = state.wall;

        if (!wall) {
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                PoseStack work = bridge(pose);
                model.renderPole(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
            });
        } else {
            poseStack.translate(0.0, 1.0, -0.4125);
        }

        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            model.renderBeam(work, consumer, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        });

        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        int color = state.color;
        if (color != -1) {
            Color c = new Color(color);
            red = c.getRed() / 255.0f;
            green = c.getGreen() / 255.0f;
            blue = c.getBlue() / 255.0f;
        }
        final float fr = red, fg = green, fb = blue;

        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            model.renderTabs(work, consumer, light, OverlayTexture.NO_OVERLAY, fr, fg, fb, 1.0f);
        });

        // NEOFORGE_PORT (26.3): geometry is emitted later (deferred) and the model is
        // shared, so the per-banner wave rotation is applied inside the callback.
        final float waveRx = state.waveRx;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            model.setBannerRotation(waveRx);
            model.renderBanner(work, consumer, light, OverlayTexture.NO_OVERLAY, fr, fg, fb, 1.0f);
        });

        Aspect aspect = state.aspect;
        if (aspect != null && state.hasLevel) {
            poseStack.pushPose();
            poseStack.translate(0.0, 0.0, 0.05001);
            poseStack.scale(0.0375f, 0.0375f, 0.0375f);
            poseStack.rotate(Axis.YP.rotationDegrees(180.0f));

            poseStack.rotate(Axis.XP.rotationDegrees(-state.waveRx * 57.295776f * 2.0f));

            renderAspectIcon(poseStack, collector, aspect, -8, 0, light, 0.75f);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static PoseStack bridge(PoseStack.Pose pose) {
        PoseStack work = new PoseStack();
        work.last().pose().set(pose.pose());
        work.last().normal().set(pose.normal());
        return work;
    }

    private void renderAspectIcon(PoseStack poseStack, SubmitNodeCollector collector, Aspect aspect,
                                  int x, int y, int packedLight, float alpha) {
        if (aspect == null) return;

        // NEOFORGE_PORT (Forge 1:1): Вырезка фона через общий helper — как в world popup.
        Identifier texture = UtilsFX.strippedAspectTexture(aspect.getImage());
        RenderType type = TCRenderTypes.translucentPortal(texture);

        int color = aspect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        float x1 = x;
        float y1 = y;
        float x2 = x + ICON_SIZE;
        float y2 = y + ICON_SIZE;

        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, x1, y2, 0, 0, 1, packedLight, r, g, b, alpha);
            quad(consumer, pose, x2, y2, 0, 1, 1, packedLight, r, g, b, alpha);
            quad(consumer, pose, x2, y1, 0, 1, 0, packedLight, r, g, b, alpha);
            quad(consumer, pose, x1, y1, 0, 0, 0, packedLight, r, g, b, alpha);
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v, int light,
                             float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    // NEOFORGE_PORT: render bounds moved from BlockEntity to the renderer in 26.x
    // (IBlockEntityRendererExtension); forward the tile's Forge-parity culling box.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TileBanner blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
