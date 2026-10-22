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
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;



public class FocalManipulatorRenderer implements BlockEntityRenderer<TileFocalManipulator, FocalManipulatorRenderer.State> {

    private static final Identifier PARTICLE_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");

    public static class State extends BlockEntityRenderState {
        public boolean hasLevel;
        public float ticks;
        public final ItemStackRenderState focusItem = new ItemStackRenderState();
        public boolean hasFocus;
        public final List<Aspect> aspects = new ArrayList<>();
        public final List<ItemStackRenderState> crystalItems = new ArrayList<>();
    }

    private final ItemModelResolver itemModelResolver;

    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileFocalManipulator tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasLevel = tile.getLevel() != null;
        Minecraft mc = Minecraft.getInstance();
        state.ticks = (mc.getCameraEntity() != null ? mc.getCameraEntity().tickCount : 0) + partialTicks;

        ItemStack focusStack = tile.getItem(0);
        state.hasFocus = !focusStack.isEmpty();
        if (state.hasFocus) {
            itemModelResolver.updateForTopItem(state.focusItem, focusStack, ItemDisplayContext.GROUND,
                    tile.getLevel(), null, 0);
        }

        state.aspects.clear();
        state.crystalItems.clear();
        Aspect[] aspects = tile.crystalsSync.getAspects();
        if (aspects != null) {
            for (Aspect aspect : aspects) {
                state.aspects.add(aspect);
                ItemStackRenderState crystal = new ItemStackRenderState();
                itemModelResolver.updateForTopItem(crystal, ArcaneresearchApiHelper.makeCrystal(aspect),
                        ItemDisplayContext.GROUND, tile.getLevel(), null, 0);
                state.crystalItems.add(crystal);
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasLevel) {
            return;
        }
        float ticks = state.ticks;
        int light = state.lightCoords;

        if (state.hasFocus) {
            float hoverStart = Mth.sin(ticks / 14.0f) * 0.2f + 0.2f;
            
            
            float groundScaleY = 0.5f;
            float bob = Mth.sin(ticks / 10.0f + hoverStart) * 0.1f + 0.1f;

            poseStack.pushPose();
            poseStack.translate(0.5, 1.075, 0.5);
            poseStack.rotate(Axis.YP.rotationDegrees(ticks % 360.0f));
            poseStack.translate(0.0f, bob + 0.25f * groundScaleY, 0.0f);
            poseStack.rotate(Axis.YP.rotation(ticks / 20.0f + hoverStart));

            state.focusItem.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }

        int count = state.aspects.size();
        if (count > 0) {
            float angleStep = 360.0f / count;

            for (int a = 0; a < count; a++) {
                Aspect aspect = state.aspects.get(a);
                float angle = (ticks % 720.0f / 2.0f) + angleStep * a;
                float bob = Mth.sin((ticks + a * 10) / 12.0f) * 0.02f + 0.02f;

                poseStack.pushPose();
                poseStack.translate(0.5, 1.425, 0.5);
                poseStack.rotate(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, bob, 0.4);
                poseStack.rotate(Axis.YP.rotationDegrees(-angle));

                Color c = new Color(aspect.getColor());
                float r = c.getRed() / 255.0f;
                float g = c.getGreen() / 255.0f;
                float b = c.getBlue() / 255.0f;

                renderGlowingOrb(poseStack, collector, 0.175f, r, g, b, 0.66f, ticks);

                poseStack.popPose();

                poseStack.pushPose();
                poseStack.translate(0.5, 1.175, 0.5);
                poseStack.rotate(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, bob, 0.4);
                poseStack.scale(0.5f, 0.5f, 0.5f);

                renderRay(poseStack, collector, angle, a, bob, r, g, b, ticks);
                renderRay(poseStack, collector, angle, (a + 1) * 5, bob, r, g, b, ticks);

                float crystalGroundY = 0.5f;
                float crystalBob = Mth.sin(ticks / 10.0f) * 0.1f + 0.1f;
                poseStack.translate(0.0f, crystalBob + 0.25f * crystalGroundY, 0.0f);
                poseStack.rotate(Axis.YP.rotation(ticks / 20.0f));
                state.crystalItems.get(a).submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }
        }
    }

    private void renderGlowingOrb(PoseStack poseStack, SubmitNodeCollector collector,
                                  float size, float r, float g, float b, float a, float ticks) {
        poseStack.pushPose();

        
        poseStack.rotate(Minecraft.getInstance().gameRenderer.mainCamera().rotation());
        poseStack.rotate(Axis.YP.rotationDegrees(180.0f));

        
        
        RenderType type = net.minecraft.client.renderer.rendertype.RenderTypes
                .entityTranslucentEmissive(PARTICLE_TEXTURE);
        int fullLight = 0x00F000F0;
        float half = size / 2.0f;

        float frame = ((int) ticks) % 16;
        float u0 = frame / 64.0f;
        float u1 = (frame + 1) / 64.0f;
        float v0 = 5.0f / 64.0f;
        float v1 = 6.0f / 64.0f;

        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, -half, -half, 0, u0, v1, r, g, b, a, fullLight);
            quad(consumer, pose, half, -half, 0, u1, v1, r, g, b, a, fullLight);
            quad(consumer, pose, half, half, 0, u1, v0, r, g, b, a, fullLight);
            quad(consumer, pose, -half, half, 0, u0, v0, r, g, b, a, fullLight);
        });

        poseStack.popPose();
    }

    private void renderRay(PoseStack poseStack, SubmitNodeCollector collector,
                           float angle, int num, float lift, float r, float g, float b, float ticks) {
        poseStack.pushPose();

        float pan = Mth.sin((ticks + num * 10) / 15.0f) * 15.0f;
        float aperture = Mth.sin((ticks + num * 10) / 14.0f) * 2.0f;

        poseStack.translate(0, 0.475f + lift, 0);
        poseStack.rotate(Axis.XN.rotationDegrees(90));
        poseStack.rotate(Axis.YP.rotationDegrees(angle));

        java.util.Random random = new java.util.Random(187L + (long) num * num);
        poseStack.rotate(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
        poseStack.rotate(Axis.XP.rotationDegrees(pan));

        
        
        RenderType type = TCRenderTypes.orbTendrils();

        float fa = random.nextFloat() * 20.0f + 10.0f;
        float f4 = random.nextFloat() * 4.0f + 6.0f + aperture;
        fa /= 30.0f / (Math.min(ticks, 10.0f) / 10.0f);
        f4 /= 30.0f / (Math.min(ticks, 10.0f) / 10.0f);

        float[] fan = {
                0, 0, 0, r, g, b, 0.66f,
                -0.8f * f4, fa, -0.5f * f4, r, g, b, 0.0f,
                0.8f * f4, fa, -0.5f * f4, r, g, b, 0.0f,
                0, fa, 1.0f * f4, r, g, b, 0.0f,
                -0.8f * f4, fa, -0.5f * f4, r, g, b, 0.0f};
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            for (int i = 0; i < 5; i++) {
                consumer.addVertex(pose, fan[i * 7], fan[i * 7 + 1], fan[i * 7 + 2])
                        .setColor(fan[i * 7 + 3], fan[i * 7 + 4], fan[i * 7 + 5], fan[i * 7 + 6]);
            }
        });

        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v,
                             float r, float g, float b, float a, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    
    
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TileFocalManipulator blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
