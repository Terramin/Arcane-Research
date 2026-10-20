package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import java.awt.*;
@OnlyIn(Dist.CLIENT)
public class FocalManipulatorRenderer implements BlockEntityRenderer<TileFocalManipulator> {
    private static final ResourceLocation PARTICLE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/particles.png");
    private final ItemRenderer itemRenderer;
    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    @Override
    public void render(TileFocalManipulator tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        Minecraft mc = Minecraft.getInstance();
        float ticks = (mc.cameraEntity != null ? mc.cameraEntity.tickCount : 0) + partialTicks;
        ItemStack focusStack = tile.getItem(0);
        if (!focusStack.isEmpty()) {
            float hoverStart = Mth.sin(ticks / 14.0f) * 0.2f + 0.2f;
            net.minecraft.client.resources.model.BakedModel focusModel = itemRenderer.getModel(
                    focusStack, tile.getLevel(), null,
                    net.minecraft.world.item.Item.getId(focusStack.getItem()) + focusStack.getDamageValue());
            float groundScaleY = focusModel.getTransforms()
                    .getTransform(ItemDisplayContext.GROUND).scale.y();
            float bob = Mth.sin(ticks / 10.0f + hoverStart) * 0.1f + 0.1f;
            poseStack.pushPose();
            poseStack.translate(0.5, 1.075, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f));
            poseStack.translate(0.0f, bob + 0.25f * groundScaleY, 0.0f);
            poseStack.mulPose(Axis.YP.rotation(ticks / 20.0f + hoverStart));
            itemRenderer.renderStatic(focusStack, ItemDisplayContext.GROUND, packedLight,
                    packedOverlay, poseStack, buffer, tile.getLevel(), 0);
            poseStack.popPose();
        }
        Aspect[] aspects = tile.crystalsSync.getAspects();
        if (aspects != null && aspects.length > 0) {
            int count = aspects.length;
            float angleStep = 360.0f / count;
            for (int a = 0; a < count; a++) {
                Aspect aspect = aspects[a];
                float angle = (ticks % 720.0f / 2.0f) + angleStep * a;
                float bob = Mth.sin((ticks + a * 10) / 12.0f) * 0.02f + 0.02f;
                poseStack.pushPose();
                poseStack.translate(0.5, 1.425, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, bob, 0.4);
                poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
                Color c = new Color(aspect.getColor());
                float r = c.getRed() / 255.0f;
                float g = c.getGreen() / 255.0f;
                float b = c.getBlue() / 255.0f;
                renderGlowingOrb(poseStack, buffer, 0.175f, r, g, b, 0.66f, ticks);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.5, 1.175, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, bob, 0.4);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                renderRay(poseStack, buffer, angle, a, bob, r, g, b, ticks);
                renderRay(poseStack, buffer, angle, (a + 1) * 5, bob, r, g, b, ticks);
                ItemStack crystalStack = ArcaneresearchApiHelper.makeCrystal(aspect);
                net.minecraft.client.resources.model.BakedModel crystalModel = itemRenderer.getModel(
                        crystalStack, tile.getLevel(), null,
                        net.minecraft.world.item.Item.getId(crystalStack.getItem()) + crystalStack.getDamageValue());
                float crystalGroundY = crystalModel.getTransforms()
                        .getTransform(ItemDisplayContext.GROUND).scale.y();
                float crystalBob = Mth.sin(ticks / 10.0f) * 0.1f + 0.1f;
                poseStack.translate(0.0f, crystalBob + 0.25f * crystalGroundY, 0.0f);
                poseStack.mulPose(Axis.YP.rotation(ticks / 20.0f));
                itemRenderer.renderStatic(crystalStack, ItemDisplayContext.GROUND, packedLight,
                        packedOverlay, poseStack, buffer, tile.getLevel(), 0);
                poseStack.popPose();
            }
        }
    }
    private void renderGlowingOrb(PoseStack poseStack, MultiBufferSource buffer,
                                  float size, float r, float g, float b, float a, float ticks) {
        poseStack.pushPose();
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(PARTICLE_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        int fullLight = 0x00F000F0;
        float half = size / 2.0f;
        float frame = ((int) ticks) % 16;
        float u0 = frame / 64.0f;
        float u1 = (frame + 1) / 64.0f;
        float v0 = 5.0f / 64.0f;
        float v1 = 6.0f / 64.0f;
        vertexConsumer.vertex(matrix, -half, -half, 0).color(r, g, b, a)
                .uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, half, -half, 0).color(r, g, b, a)
                .uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, half, half, 0).color(r, g, b, a)
                .uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, -half, half, 0).color(r, g, b, a)
                .uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(fullLight).normal(0, 0, 1).endVertex();
        poseStack.popPose();
    }
    private void renderRay(PoseStack poseStack, MultiBufferSource buffer,
                           float angle, int num, float lift, float r, float g, float b, float ticks) {
        poseStack.pushPose();
        float pan = Mth.sin((ticks + num * 10) / 15.0f) * 15.0f;
        float aperture = Mth.sin((ticks + num * 10) / 14.0f) * 2.0f;
        poseStack.translate(0, 0.475f + lift, 0);
        poseStack.mulPose(Axis.XN.rotationDegrees(90));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        java.util.Random random = new java.util.Random(187L + (long) num * num);
        poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(pan));
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f matrix = poseStack.last().pose();
        float fa = random.nextFloat() * 20.0f + 10.0f;
        float f4 = random.nextFloat() * 4.0f + 6.0f + aperture;
        fa /= 30.0f / (Math.min(ticks, 10.0f) / 10.0f);
        f4 /= 30.0f / (Math.min(ticks, 10.0f) / 10.0f);
        vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, 0.66f).endVertex();
        vertexConsumer.vertex(matrix, -0.8f * f4, fa, -0.5f * f4).color(r, g, b, 0.0f).endVertex();
        vertexConsumer.vertex(matrix, 0.8f * f4, fa, -0.5f * f4).color(r, g, b, 0.0f).endVertex();
        vertexConsumer.vertex(matrix, 0, fa, 1.0f * f4).color(r, g, b, 0.0f).endVertex();
        vertexConsumer.vertex(matrix, -0.8f * f4, fa, -0.5f * f4).color(r, g, b, 0.0f).endVertex();
        poseStack.popPose();
    }
}
