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
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class FocalManipulatorRenderer implements BlockEntityRenderer<TileFocalManipulator> {
    private static final ResourceLocation PARTICLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/particles.png");
    private static final RenderType PARTICLE_TYPE = RenderType.entityTranslucentEmissive(PARTICLE_TEXTURE);
    private static final RenderType LIGHTNING_TYPE = RenderType.lightning();

    private static final Quaternionf ROT_YP_180 = Axis.YP.rotationDegrees(180.0f);
    private static final Quaternionf ROT_XN_90 = Axis.XN.rotationDegrees(90.0f);

    private static final int FULL_LIGHT = 0x00F000F0;

    private static final ThreadLocal<Random> RAY_RANDOM = ThreadLocal.withInitial(() -> new Random(0L));
    private static final Map<Aspect, ItemStack> CRYSTAL_CACHE = new ConcurrentHashMap<>();

    private final ItemRenderer itemRenderer;

    public FocalManipulatorRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(@NotNull TileFocalManipulator tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        Minecraft mc = Minecraft.getInstance();
        var dispatcher = mc.getEntityRenderDispatcher();
        Quaternionf cameraRot = dispatcher.cameraOrientation();
        Entity camera = mc.getCameraEntity();
        float ticks = (camera != null ? camera.tickCount : 0) + partialTicks;

        ItemStack focusStack = tile.getItem(0);
        if (!focusStack.isEmpty()) {
            float hoverStart = Mth.sin(ticks / 14.0f) * 0.2f + 0.2f;
            BakedModel focusModel = itemRenderer.getModel(
                    focusStack, level, null,
                    Item.getId(focusStack.getItem()) + focusStack.getDamageValue());
            float groundScaleY = focusModel.getTransforms()
                    .getTransform(ItemDisplayContext.GROUND).scale.y();
            float bob = Mth.sin(ticks / 10.0f + hoverStart) * 0.1f + 0.1f;
            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 1.075, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f));
                poseStack.translate(0.0f, bob + 0.25f * groundScaleY, 0.0f);
                poseStack.mulPose(Axis.YP.rotation(ticks / 20.0f + hoverStart));
                itemRenderer.renderStatic(focusStack, ItemDisplayContext.GROUND, packedLight,
                        packedOverlay, poseStack, buffer, level, 0);
            } finally {
                poseStack.popPose();
            }
        }

        if (tile.crystalsSync == null) return;
        Aspect[] aspects = tile.crystalsSync.getAspects();
        if (aspects == null || aspects.length == 0) return;
        int count = aspects.length;
        float angleStep = 360.0f / count;
        for (int a = 0; a < count; a++) {
            Aspect aspect = aspects[a];
            if (aspect == null) continue;
            int colorInt = aspect.getColor();
            float r = ((colorInt >> 16) & 0xFF) / 255.0f;
            float g = ((colorInt >> 8) & 0xFF) / 255.0f;
            float b = (colorInt & 0xFF) / 255.0f;
            float angle = (ticks % 720.0f / 2.0f) + angleStep * a;
            float bob = Mth.sin((ticks + a * 10) / 12.0f) * 0.02f + 0.02f;

            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 1.425, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, bob, 0.4);
                poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
                renderGlowingOrb(poseStack, buffer, r, g, b, ticks, cameraRot);
            } finally {
                poseStack.popPose();
            }

            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 1.175, 0.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(angle));
                poseStack.translate(0, bob, 0.4);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                renderRay(poseStack, buffer, angle, a, bob, r, g, b, ticks);
                renderRay(poseStack, buffer, angle, (a + 1) * 5, bob, r, g, b, ticks);

                ItemStack crystalStack = CRYSTAL_CACHE.computeIfAbsent(aspect, ArcaneresearchApiHelper::makeCrystal);
                if (crystalStack != null && !crystalStack.isEmpty()) {
                    BakedModel crystalModel = itemRenderer.getModel(
                            crystalStack, level, null,
                            Item.getId(crystalStack.getItem()) + crystalStack.getDamageValue());
                    float crystalGroundY = crystalModel.getTransforms()
                            .getTransform(ItemDisplayContext.GROUND).scale.y();
                    float crystalBob = Mth.sin(ticks / 10.0f) * 0.1f + 0.1f;
                    poseStack.translate(0.0f, crystalBob + 0.25f * crystalGroundY, 0.0f);
                    poseStack.mulPose(Axis.YP.rotation(ticks / 20.0f));
                    itemRenderer.renderStatic(crystalStack, ItemDisplayContext.GROUND, packedLight,
                            packedOverlay, poseStack, buffer, level, 0);
                }
            } finally {
                poseStack.popPose();
            }
        }
    }

    private static void renderGlowingOrb(PoseStack poseStack, MultiBufferSource buffer,
                                         float r, float g, float b, float ticks,
                                         Quaternionf cameraRot) {
        poseStack.pushPose();
        try {
            if (cameraRot != null) {
                poseStack.mulPose(cameraRot);
            }
            poseStack.mulPose(ROT_YP_180);
            VertexConsumer vertexConsumer = buffer.getBuffer(PARTICLE_TYPE);
            Matrix4f matrix = poseStack.last().pose();
            float half = (float) 0.175 / 2.0f;
            float frame = ((int) ticks) % 16;
            float u0 = frame / 64.0f;
            float u1 = (frame + 1) / 64.0f;
            float v0 = 5.0f / 64.0f;
            float v1 = 6.0f / 64.0f;
            vertexConsumer.vertex(matrix, -half, -half, 0).color(r, g, b, (float) 0.66)
                    .uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_LIGHT).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, half, -half, 0).color(r, g, b, (float) 0.66)
                    .uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_LIGHT).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, half, half, 0).color(r, g, b, (float) 0.66)
                    .uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_LIGHT).normal(0, 0, 1).endVertex();
            vertexConsumer.vertex(matrix, -half, half, 0).color(r, g, b, (float) 0.66)
                    .uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_LIGHT).normal(0, 0, 1).endVertex();
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderRay(PoseStack poseStack, MultiBufferSource buffer,
                                  float angle, int num, float lift, float r, float g, float b, float ticks) {
        poseStack.pushPose();
        try {
            float pan = Mth.sin((ticks + num * 10) / 15.0f) * 15.0f;
            float aperture = Mth.sin((ticks + num * 10) / 14.0f) * 2.0f;
            poseStack.translate(0, 0.475f + lift, 0);
            poseStack.mulPose(ROT_XN_90);
            poseStack.mulPose(Axis.YP.rotationDegrees(angle));
            Random random = RAY_RANDOM.get();
            random.setSeed(187L + (long) num * num);
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(pan));
            VertexConsumer vertexConsumer = buffer.getBuffer(LIGHTNING_TYPE);
            Matrix4f matrix = poseStack.last().pose();
            float fa = random.nextFloat() * 20.0f + 10.0f;
            float f4 = random.nextFloat() * 4.0f + 6.0f + aperture;
            float scale = Math.min(ticks, 10.0f) / 10.0f;
            fa /= 30.0f / scale;
            f4 /= 30.0f / scale;
            vertexConsumer.vertex(matrix, 0, 0, 0).color(r, g, b, 0.66f).endVertex();
            vertexConsumer.vertex(matrix, -0.8f * f4, fa, -0.5f * f4).color(r, g, b, 0.0f).endVertex();
            vertexConsumer.vertex(matrix, 0.8f * f4, fa, -0.5f * f4).color(r, g, b, 0.0f).endVertex();
            vertexConsumer.vertex(matrix, 0, fa, 1.0f * f4).color(r, g, b, 0.0f).endVertex();
            vertexConsumer.vertex(matrix, -0.8f * f4, fa, -0.5f * f4).color(r, g, b, 0.0f).endVertex();
        } finally {
            poseStack.popPose();
        }
    }
}