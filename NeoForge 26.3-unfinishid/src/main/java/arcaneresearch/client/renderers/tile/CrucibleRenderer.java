package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn; // NEOFORGE_PORT: was net.minecraftforge.api.distmarker.OnlyIn
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.crafting.TileCrucible;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (aspect-tinted water top quad; biome water color fallback).
@OnlyIn(Dist.CLIENT) // NEOFORGE_PORT: kept Forge class annotation (was forge OnlyIn)
public class CrucibleRenderer implements BlockEntityRenderer<TileCrucible, CrucibleRenderer.State> {

    private static final Identifier WATER_STILL =
            Identifier.withDefaultNamespace("block/water_still");

    public static class State extends BlockEntityRenderState {
        public boolean hasFluid;
        public float fluidHeight;
        public float r;
        public float g;
        public float b;
        public float alpha;
    }

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileCrucible tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        if (tile.getTank().getFluidAmount() <= 0) {
            state.hasFluid = false;
            return;
        }
        state.hasFluid = true;
        state.fluidHeight = tile.getFluidHeight();

        float r, g, b;
        int total = tile.getAspects().visSize();
        if (total > 0) {
            double ar = 0.0, ag = 0.0, ab = 0.0;
            for (Aspect aspect : tile.getAspects().getAspects()) {
                int color = aspect.getColor() & 0xFFFFFF;
                int amount = tile.getAspects().getAmount(aspect);
                ar += ((color >> 16) & 0xFF) * (double) amount;
                ag += ((color >> 8) & 0xFF) * (double) amount;
                ab += (color & 0xFF) * (double) amount;
            }
            r = (float) (ar / total / 255.0);
            g = (float) (ag / total / 255.0);
            b = (float) (ab / total / 255.0);
        } else {
            int waterColor = 0x3F76E4;
            if (tile.getLevel() != null) {
                waterColor = tile.getLevel().getBiome(tile.getBlockPos()).value()
                        .getSpecialEffects().waterColor(); // NEOFORGE_PORT: record accessor, was getWaterColor()
            }
            r = ((waterColor >> 16) & 0xFF) / 255.0f;
            g = ((waterColor >> 8) & 0xFF) / 255.0f;
            b = (waterColor & 0xFF) / 255.0f;
        }

        float recolor = (float) total / TileCrucible.MAX_ASPECTS;
        if (recolor > 0.0f) {
            recolor = 0.5f + recolor / 2.0f;
        }
        if (recolor > 1.0f) {
            recolor = 1.0f;
        }
        state.r = r;
        state.g = g;
        state.b = b;
        state.alpha = 1.0f - recolor / 2.0f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasFluid) {
            return;
        }
        TextureAtlasSprite sprite;
        try {
            // NEOFORGE_PORT: was getTextureAtlas(LOCATION_BLOCKS).apply(stillTexture);
            // atlas lookup is now AtlasManager.get(SpriteId). LOCATION_BLOCKS still valid (deprecated only).
            sprite = Minecraft.getInstance().getAtlasManager().get(
                    new SpriteId(TextureAtlas.LOCATION_BLOCKS, WATER_STILL));
        } catch (Exception e) {
            return;
        }
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float y = state.fluidHeight;
        float r = state.r;
        float g = state.g;
        float b = state.b;
        float a = state.alpha;
        int light = state.lightCoords; // NEOFORGE_PORT: was packedLight param; filled by extractBase
        // NEOFORGE_PORT: bind the BLOCKS atlas, not WATER_STILL. Forge RenderType.translucent()
        // sampled the block atlas with sprite UVs (vanilla SpriteId.renderType applies the fn to
        // the atlasLocation); binding the still texture directly would sample atlas-space UVs
        // from a standalone texture (wrong crop, no animation).
        RenderType type = TCRenderTypes.translucentPortal(TextureAtlas.LOCATION_BLOCKS);
        poseStack.pushPose();
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, 0.0f, y, 0.0f, u0, v0, r, g, b, a, light);
            quad(consumer, pose, 0.0f, y, 1.0f, u0, v1, r, g, b, a, light);
            quad(consumer, pose, 1.0f, y, 1.0f, u1, v1, r, g, b, a, light);
            quad(consumer, pose, 1.0f, y, 0.0f, u1, v0, r, g, b, a, light);
        });
        poseStack.popPose();
    }

    private static void quad(com.mojang.blaze3d.vertex.VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v,
                             float r, float g, float b, float a, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 1, 0);
    }
}