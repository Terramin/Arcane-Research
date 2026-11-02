package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.tiles.crafting.TileCrucible;

@OnlyIn(Dist.CLIENT)
public class CrucibleRenderer implements BlockEntityRenderer<TileCrucible> {
    private static final float MIN_X = 0.0f;
    private static final float MAX_X = 1.0f;
    private static final float MIN_Z = 0.0f;
    private static final float MAX_Z = 1.0f;

    private static TextureAtlasSprite waterSprite;

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static TextureAtlasSprite waterSprite() {
        if (waterSprite == null) {
            IClientFluidTypeExtensions fluidExtensions = IClientFluidTypeExtensions.of(Fluids.WATER);
            ResourceLocation stillTexture = fluidExtensions.getStillTexture();

            @SuppressWarnings("deprecation")
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .apply(stillTexture);

            waterSprite = sprite;
        }

        return waterSprite;
    }

    @Override
    public void render(TileCrucible tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getTank().getFluidAmount() <= 0) return;

        float fluidHeight = tile.getFluidHeight();

        AspectList aspects = tile.getAspects();
        int total = aspects.visSize();

        float r, g, b;

        if (total > 0) {
            double ar = 0.0, ag = 0.0, ab = 0.0;

            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null) continue;

                int color = aspect.getColor() & 0xFFFFFF;
                int amount = aspects.getAmount(aspect);

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
                        .getSpecialEffects().getWaterColor();
            }

            r = ((waterColor >> 16) & 0xFF) / 255.0f;
            g = ((waterColor >> 8) & 0xFF) / 255.0f;
            b = (waterColor & 0xFF) / 255.0f;
        }

        float recolor = (float) total / TileCrucible.MAX_ASPECTS;

        if (recolor > 0.0f) {
            recolor = Mth.clamp(0.5f + recolor / 2.0f, 0.0f, 1.0f);
        }

        float alpha = 1.0f - recolor / 2.0f;

        poseStack.pushPose();

        TextureAtlasSprite sprite = waterSprite();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        Matrix4f matrix = poseStack.last().pose();

        float y = fluidHeight;

        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        vertexConsumer.vertex(matrix, MIN_X, y, MIN_Z).color(r, g, b, alpha)
                .uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, MIN_X, y, MAX_Z).color(r, g, b, alpha)
                .uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, MAX_X, y, MAX_Z).color(r, g, b, alpha)
                .uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(0, 1, 0).endVertex();
        vertexConsumer.vertex(matrix, MAX_X, y, MIN_Z).color(r, g, b, alpha)
                .uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight).normal(0, 1, 0).endVertex();

        poseStack.popPose();
    }
}