package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.aura.EldritchPortalTile;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (camera-facing animated portal quad).
public class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalTile, EldritchPortalRenderer.State> {
    private static final Identifier PORTAL_TEX =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/eldritch_portal.png");

    public static class State extends BlockEntityRenderState {
        public int opencount;
        public float partialTicks;
    }

    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchPortalTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.opencount = tile.opencount;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        int c = Math.min(30, state.opencount + (int) state.partialTicks);
        int e = Math.min(5, state.opencount + (int) state.partialTicks);
        float scale = e / 5.0F;
        float scaley = c / 30.0F;
        long time = System.nanoTime() / 50_000_000L;
        int frame = (int) (time % 16);
        float u0 = frame / 16.0F;
        float u1 = u0 + 0.0625F;
        Quaternionf rotation = Minecraft.getInstance().gameRenderer.mainCamera().rotation();
        Vector3f[] corners = {
                new Vector3f(-0.5F * scale, -0.5F * scaley, 0.0F),
                new Vector3f(0.5F * scale, -0.5F * scaley, 0.0F),
                new Vector3f(0.5F * scale, 0.5F * scaley, 0.0F),
                new Vector3f(-0.5F * scale, 0.5F * scaley, 0.0F)};
        float[] us = {u0, u1, u1, u0};
        RenderType type = TCRenderTypes.translucentPortal(PORTAL_TEX);
        int overlay = OverlayTexture.NO_OVERLAY;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            for (int i = 0; i < 4; i++) {
                Vector3f corner = corners[i];
                corner.rotate(rotation);
                consumer.addVertex(pose, corner.x(), corner.y(), corner.z())
                        .setColor(1.0F, 0.0F, 1.0F, 1.0F)
                        .setUv(us[i], i < 2 ? 1.0F : 0.0F)
                        .setOverlay(overlay)
                        .setLight(0xF000F0)
                        .setNormal(pose, 0.0F, 1.0F, 0.0F);
            }
        });
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    // NEOFORGE_PORT: render bounds moved from BlockEntity to the renderer in 26.x
    // (IBlockEntityRendererExtension); forward the tile's Forge-parity culling box.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(EldritchPortalTile blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
