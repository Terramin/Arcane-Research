package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.tiles.essentia.TileJar;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class JarRenderer implements BlockEntityRenderer<TileJar> {
    private static final ResourceLocation LIQUID_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/animatedglow.png");
    private static final int LIQUID_FRAMES = 20;
    private static final int LIQUID_CYCLE = 38;
    private static final ResourceLocation LABEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/label.png");
    private static final ResourceLocation BRINE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jarbrine.png");

    private static final RenderType BRINE_TYPE = RenderType.entityCutoutNoCull(BRINE_TEXTURE);
    private static final RenderType LIQUID_TYPE = RenderType.entityTranslucent(LIQUID_TEXTURE);
    private static final RenderType LABEL_TYPE = RenderType.entityTranslucent(LABEL_TEXTURE);

    private static final Map<ResourceLocation, RenderType> TRANSLUCENT_CACHE = new ConcurrentHashMap<>();

    private static final Quaternionf ROT_XP_180 = Axis.XP.rotationDegrees(180.0F);
    private static final Quaternionf ROT_YP_180 = Axis.YP.rotationDegrees(180.0F);
    private static final Quaternionf ROT_YP_270 = Axis.YP.rotationDegrees(270.0F);
    private static final Quaternionf ROT_YP_90 = Axis.YP.rotationDegrees(90.0F);

    private static final int LIGHT_FULL = 0x00F000F0;

    private final arcaneresearch.client.models.block.JarModel fittings;

    public JarRenderer(BlockEntityRendererProvider.Context context) {
        this.fittings = new arcaneresearch.client.models.block.JarModel(
                context.bakeLayer(arcaneresearch.client.models.block.JarModel.LAYER_LOCATION));
    }

    private static RenderType translucentCached(ResourceLocation texture) {
        return TRANSLUCENT_CACHE.computeIfAbsent(texture, RenderType::entityTranslucent);
    }

    private static int getLiquidFrame(Level level) {
        long tick = level != null ? level.getGameTime() : 0;
        int step = (int) ((tick / 2) % LIQUID_CYCLE);
        return step < LIQUID_FRAMES ? step : LIQUID_CYCLE - step;
    }

    @Override
    public void render(@NotNull TileJar tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();

        if (tile.isBlocked()) {
            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 0.01, 0.5);
                poseStack.mulPose(ROT_XP_180);
                VertexConsumer brine = buffer.getBuffer(BRINE_TYPE);
                fittings.renderLidBrace(poseStack, brine, packedLight, packedOverlay);
            } finally {
                poseStack.popPose();
            }
        }

        if (level != null && ArcaneresearchApiHelper.getConnectableTile(
                level, tile.getBlockPos(), Direction.UP) != null) {
            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 0.01, 0.5);
                poseStack.mulPose(ROT_XP_180);
                VertexConsumer brine = buffer.getBuffer(BRINE_TYPE);
                fittings.renderLidExtension(poseStack, brine, packedLight, packedOverlay);
            } finally {
                poseStack.popPose();
            }
        }

        if (tile.getAmount() > 0) {
            renderLiquid(tile, level, poseStack, buffer, packedLight);
        }

        Aspect filter = tile.getAspectFilter();
        if (filter != null) {
            renderLabel(tile, filter, poseStack, buffer, packedLight);
        }
    }

    private static void renderLiquid(TileJar tile, Level level, PoseStack poseStack,
                                     MultiBufferSource buffer, int packedLight) {
        Aspect aspect = tile.getAspect();
        if (aspect == null) return;
        int amount = tile.getAmount();
        float fillLevel = amount / 250.0f;
        float liquidHeight = 0.0625f + fillLevel * 0.625f;

        int colorInt = aspect.getColor();
        float r = ((colorInt >> 16) & 0xFF) / 255.0f;
        float g = ((colorInt >> 8) & 0xFF) / 255.0f;
        float b = (colorInt & 0xFF) / 255.0f;
        float a = 0.8f;

        poseStack.pushPose();
        try {
            VertexConsumer vertexConsumer = buffer.getBuffer(LIQUID_TYPE);
            Matrix4f matrix = poseStack.last().pose();
            float minX = 0.251f;
            float maxX = 0.749f;
            float minY = 0.0625f;
            float minZ = 0.251f;
            float maxZ = 0.749f;
            int frame = getLiquidFrame(level);
            float minU = 0, maxU = 1;
            float minV = frame / (float) LIQUID_FRAMES;
            float maxV = (frame + 1) / (float) LIQUID_FRAMES;
            addQuad(vertexConsumer, matrix,
                    minX, minY, minZ, maxX, minY, maxZ,
                    0, -1, 0, r, g, b, a, minU, maxU, minV, maxV);
            addQuad(vertexConsumer, matrix,
                    minX, liquidHeight, maxZ, maxX, liquidHeight, minZ,
                    0, 1, 0, r, g, b, a, minU, maxU, minV, maxV);
            addQuad(vertexConsumer, matrix,
                    maxX, minY, minZ, minX, liquidHeight, minZ,
                    0, 0, -1, r, g, b, a, minU, maxU, minV, maxV);
            addQuad(vertexConsumer, matrix,
                    minX, minY, maxZ, maxX, liquidHeight, maxZ,
                    0, 0, 1, r, g, b, a, minU, maxU, minV, maxV);
            addQuad(vertexConsumer, matrix,
                    minX, minY, minZ, minX, liquidHeight, maxZ,
                    -1, 0, 0, r, g, b, a, minU, maxU, minV, maxV);
            addQuad(vertexConsumer, matrix,
                    maxX, minY, maxZ, maxX, liquidHeight, minZ,
                    1, 0, 0, r, g, b, a, minU, maxU, minV, maxV);
        } finally {
            poseStack.popPose();
        }
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float nx, float ny, float nz, float r, float g, float b, float a,
                                float minU, float maxU, float minV, float maxV) {
        if (ny != 0) {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
        } else if (nz != 0) {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
        } else {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(JarRenderer.LIGHT_FULL).normal(nx, ny, nz).endVertex();
        }
    }

    private static void renderLabel(TileJar tile, Aspect aspect, PoseStack poseStack,
                                    MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            int facing = tile.getFacing();
            Quaternionf facingRot = switch (facing) {
                case 3 -> ROT_YP_180;
                case 4 -> ROT_YP_270;
                case 5 -> ROT_YP_90;
                default -> null;
            };
            if (facingRot != null) {
                poseStack.mulPose(facingRot);
            }
            poseStack.translate(0, -0.1, 0.315);
            if (ModConfig.CONFIG_GRAPHICS.crooked && tile.getLevel() != null) {
                String tag = aspect.getTag();
                int tagHash = tag != null ? tag.hashCode() : 0;
                float rot = (float) (((tagHash + tile.getBlockPos().getX() + facing) % 4) - 2);
                poseStack.mulPose(Axis.ZP.rotationDegrees(rot));
            }
            poseStack.scale(0.5f, 0.5f, 0.5f);
            VertexConsumer labelConsumer = buffer.getBuffer(LABEL_TYPE);
            Matrix4f matrix = poseStack.last().pose();
            float size = 0.5f;
            labelConsumer.vertex(matrix, -size, -size, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            labelConsumer.vertex(matrix, size, -size, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            labelConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
            labelConsumer.vertex(matrix, -size, size, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();

            ResourceLocation image = aspect.getImage();
            if (image == null) return;
            ResourceLocation stripped = UtilsFX.strippedAspectTexture(image);
            if (stripped == null) return;

            poseStack.translate(0, 0, 0.01);
            poseStack.scale(0.03f, 0.03f, 0.03f);
            int colorInt = aspect.getColor();
            float r = ((colorInt >> 16) & 0xFF) / 255f;
            float g = ((colorInt >> 8) & 0xFF) / 255f;
            float b = (colorInt & 0xFF) / 255f;
            VertexConsumer iconConsumer = buffer.getBuffer(translucentCached(stripped));
            Matrix4f iconMatrix = poseStack.last().pose();
            float iconSize = 8;
            iconConsumer.vertex(iconMatrix, -iconSize, -iconSize, 0).color(r, g, b, 1f).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LIGHT_FULL).normal(0, 0, 1).endVertex();
            iconConsumer.vertex(iconMatrix, iconSize, -iconSize, 0).color(r, g, b, 1f).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LIGHT_FULL).normal(0, 0, 1).endVertex();
            iconConsumer.vertex(iconMatrix, iconSize, iconSize, 0).color(r, g, b, 1f).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LIGHT_FULL).normal(0, 0, 1).endVertex();
            iconConsumer.vertex(iconMatrix, -iconSize, iconSize, 0).color(r, g, b, 1f).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LIGHT_FULL).normal(0, 0, 1).endVertex();
        } finally {
            poseStack.popPose();
        }
    }
}