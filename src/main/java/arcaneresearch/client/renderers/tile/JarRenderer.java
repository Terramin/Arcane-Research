package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.common.tiles.essentia.TileJar;
import java.awt.*;
@OnlyIn(Dist.CLIENT)
public class JarRenderer implements BlockEntityRenderer<TileJar> {
    private static final ResourceLocation LIQUID_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/animatedglow.png");
    private static final int LIQUID_FRAMES = 20;
    private static final int LIQUID_CYCLE = 38;
    private static final ResourceLocation LABEL_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/label.png");
    private static final ResourceLocation BRINE_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/jarbrine.png");
    private final arcaneresearch.client.models.block.JarModel fittings;
    public JarRenderer(BlockEntityRendererProvider.Context context) {
        this.fittings = new arcaneresearch.client.models.block.JarModel(
                context.bakeLayer(arcaneresearch.client.models.block.JarModel.LAYER_LOCATION));
    }
    private static int getLiquidFrame(TileJar tile, float partialTicks) {
        long tick = 0;
        if (tile.getLevel() != null) {
            tick = tile.getLevel().getGameTime();
        }
        int step = (int) ((tick / 2) % LIQUID_CYCLE);
        return step < LIQUID_FRAMES ? step : LIQUID_CYCLE - step;
    }
    @Override
    public void render(TileJar tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.isBlocked()) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.01, 0.5);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            VertexConsumer brine = buffer.getBuffer(RenderType.entityCutoutNoCull(BRINE_TEXTURE));
            fittings.renderLidBrace(poseStack, brine, packedLight, packedOverlay);
            poseStack.popPose();
        }
        if (tile.getLevel() != null && arcaneresearch.api.ArcaneresearchApiHelper.getConnectableTile(
                tile.getLevel(), tile.getBlockPos(), net.minecraft.core.Direction.UP) != null) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.01, 0.5);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            VertexConsumer brine = buffer.getBuffer(RenderType.entityCutoutNoCull(BRINE_TEXTURE));
            fittings.renderLidExtension(poseStack, brine, packedLight, packedOverlay);
            poseStack.popPose();
        }
        if (tile.getAmount() > 0) {
            renderLiquid(tile, partialTicks, poseStack, buffer, packedLight);
        }
        Aspect filter = tile.getAspectFilter();
        if (filter != null) {
            renderLabel(tile, filter, partialTicks, poseStack, buffer, packedLight);
        }
    }
    private void renderLiquid(TileJar tile, float partialTicks, PoseStack poseStack,
                              MultiBufferSource buffer, int packedLight) {
        Aspect aspect = tile.getAspect();
        if (aspect == null) return;
        int amount = tile.getAmount();
        float fillLevel = amount / 250.0f;
        float liquidHeight = 0.0625f + fillLevel * 0.625f;
        Color color = new Color(aspect.getColor());
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;
        float a = 0.8f;
        poseStack.pushPose();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(LIQUID_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float minX = 0.251f;
        float maxX = 0.749f;
        float minY = 0.0625f;
        float maxY = liquidHeight;
        float minZ = 0.251f;
        float maxZ = 0.749f;
        int light = 0x00F000F0;
        int frame = getLiquidFrame(tile, partialTicks);
        float minU = 0, maxU = 1;
        float minV = frame / (float) LIQUID_FRAMES;
        float maxV = (frame + 1) / (float) LIQUID_FRAMES;
        addQuad(vertexConsumer, matrix,
                minX, minY, minZ, maxX, minY, maxZ,
                0, -1, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        addQuad(vertexConsumer, matrix,
                minX, maxY, maxZ, maxX, maxY, minZ,
                0, 1, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        addQuad(vertexConsumer, matrix,
                maxX, minY, minZ, minX, maxY, minZ,
                0, 0, -1, r, g, b, a, light, minU, maxU, minV, maxV);
        addQuad(vertexConsumer, matrix,
                minX, minY, maxZ, maxX, maxY, maxZ,
                0, 0, 1, r, g, b, a, light, minU, maxU, minV, maxV);
        addQuad(vertexConsumer, matrix,
                minX, minY, minZ, minX, maxY, maxZ,
                -1, 0, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        addQuad(vertexConsumer, matrix,
                maxX, minY, maxZ, maxX, maxY, minZ,
                1, 0, 0, r, g, b, a, light, minU, maxU, minV, maxV);
        poseStack.popPose();
    }
    private void addQuad(VertexConsumer consumer, Matrix4f matrix,
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         float nx, float ny, float nz, float r, float g, float b, float a, int light,
                         float minU, float maxU, float minV, float maxV) {
        if (ny != 0) {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        } else if (nz != 0) {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        } else {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        }
    }
    private void renderLabel(TileJar tile, Aspect aspect, float partialTicks,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        int facing = tile.getFacing();
        switch (facing) {
            case 2 -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case 3 -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case 4 -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case 5 -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        poseStack.translate(0, -0.1, 0.315);
        if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.crooked && tile.getLevel() != null) {
            float rot = (float) (((aspect.getTag().hashCode() + tile.getBlockPos().getX() + facing) % 4) - 2);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rot));
        }
        poseStack.scale(0.5f, 0.5f, 0.5f);
        VertexConsumer labelConsumer = buffer.getBuffer(RenderType.entityTranslucent(LABEL_TEXTURE));
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.5f;
        labelConsumer.vertex(matrix, -size, -size, 0).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, size, -size, 0).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        labelConsumer.vertex(matrix, -size, size, 0).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0, 0, 1).endVertex();
        poseStack.translate(0, 0, 0.01);
        poseStack.scale(0.03f, 0.03f, 0.03f);
        Color color = new Color(aspect.getColor());
        VertexConsumer iconConsumer = buffer.getBuffer(
                RenderType.entityTranslucent(UtilsFX.strippedAspectTexture(aspect.getImage())));
        Matrix4f iconMatrix = poseStack.last().pose();
        float iconSize = 8;
        float r = color.getRed() / 255f;
        float g = color.getGreen() / 255f;
        float b = color.getBlue() / 255f;
        iconConsumer.vertex(iconMatrix, -iconSize, -iconSize, 0).color(r, g, b, 1f).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, iconSize, -iconSize, 0).color(r, g, b, 1f).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, iconSize, iconSize, 0).color(r, g, b, 1f).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        iconConsumer.vertex(iconMatrix, -iconSize, iconSize, 0).color(r, g, b, 1f).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0x00F000F0).normal(0, 0, 1).endVertex();
        poseStack.popPose();
    }
}
