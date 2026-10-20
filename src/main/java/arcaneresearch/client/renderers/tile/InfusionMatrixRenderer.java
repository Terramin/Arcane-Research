package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;
import java.util.Random;
@OnlyIn(Dist.CLIENT)
public class InfusionMatrixRenderer implements BlockEntityRenderer<TileInfusionMatrix> {
    private static final ResourceLocation TEXTURE_NORMAL =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/infuser_normal.png");
    private static final ResourceLocation TEXTURE_ANCIENT =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/infuser_ancient.png");
    private static final ResourceLocation TEXTURE_ELDRITCH =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/infuser_eldritch.png");
    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TileInfusionMatrix tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float ticks = Minecraft.getInstance().player != null ?
                Minecraft.getInstance().player.tickCount + partialTicks : 0;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        if (tile.startUp > 0) {
            poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f * tile.startUp));
            poseStack.mulPose(Axis.XP.rotationDegrees(35.0f * tile.startUp));
            poseStack.mulPose(Axis.ZP.rotationDegrees(45.0f * tile.startUp));
        }
        float instability = Math.min(6.0f, 1.0f + (tile.stability < 0 ? -tile.stability * 0.66f : 1.0f) *
                (Math.min(tile.craftCount, 50) / 50.0f));
        ResourceLocation texture = TEXTURE_NORMAL;
        if (tile.getLevel() != null) {
            net.minecraft.world.level.block.state.BlockState pillarState = tile.getLevel()
                    .getBlockState(tile.getBlockPos().offset(-1, -2, -1));
            if (arcaneresearch.init.ModBlocks.ANCIENT_PILLAR != null
                    && pillarState.is(arcaneresearch.init.ModBlocks.ANCIENT_PILLAR.get())) {
                texture = TEXTURE_ANCIENT;
            } else if (arcaneresearch.init.ModBlocks.ELDRITCH_PILLAR != null
                    && pillarState.is(arcaneresearch.init.ModBlocks.ELDRITCH_PILLAR.get())) {
                texture = TEXTURE_ELDRITCH;
            }
        }
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float wobbleX = 0, wobbleY = 0, wobbleZ = 0;
                    if (tile.active) {
                        wobbleX = Mth.sin((ticks + a * 10) / 15.0f) * 0.01f * tile.startUp * instability;
                        wobbleY = Mth.sin((ticks + b * 10) / 14.0f) * 0.01f * tile.startUp * instability;
                        wobbleZ = Mth.sin((ticks + c * 10) / 13.0f) * 0.01f * tile.startUp * instability;
                    }
                    int signA = (a == 0) ? -1 : 1;
                    int signB = (b == 0) ? -1 : 1;
                    int signC = (c == 0) ? -1 : 1;
                    poseStack.pushPose();
                    poseStack.translate(wobbleX + signA * 0.25f, wobbleY + signB * 0.25f, wobbleZ + signC * 0.25f);
                    if (a > 0) poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                    if (b > 0) poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                    if (c > 0) poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
                    poseStack.scale(0.45f, 0.45f, 0.45f);
                    renderCube(poseStack, vertexConsumer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f, 0);
                    poseStack.popPose();
                }
            }
        }
        if (tile.active) {
            VertexConsumer glowConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));
            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 2; b++) {
                    for (int c = 0; c < 2; c++) {
                        float wobbleX = Mth.sin((ticks + a * 10) / 15.0f) * 0.01f * tile.startUp * instability;
                        float wobbleY = Mth.sin((ticks + b * 10) / 14.0f) * 0.01f * tile.startUp * instability;
                        float wobbleZ = Mth.sin((ticks + c * 10) / 13.0f) * 0.01f * tile.startUp * instability;
                        int signA = (a == 0) ? -1 : 1;
                        int signB = (b == 0) ? -1 : 1;
                        int signC = (c == 0) ? -1 : 1;
                        poseStack.pushPose();
                        poseStack.translate(wobbleX + signA * 0.25f, wobbleY + signB * 0.25f, wobbleZ + signC * 0.25f);
                        if (a > 0) poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
                        if (b > 0) poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
                        if (c > 0) poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
                        poseStack.scale(0.45f, 0.45f, 0.45f);
                        float pulse = (Mth.sin((ticks + a * 2 + b * 3 + c * 4) / 4.0f) * 0.1f + 0.2f) * tile.startUp;
                        renderCube(poseStack, glowConsumer, 0x00F000F0, packedOverlay, 0.8f, 0.1f, 1.0f, pulse, 32);
                        poseStack.popPose();
                    }
                }
            }
        }
        poseStack.popPose();
        if (tile.crafting && tile.craftCount > 0) {
            renderHalo(tile, poseStack, buffer, partialTicks, tile.craftCount);
        }
    }
    private void renderCube(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                            float r, float g, float b, float a, int vOff) {
        Matrix4f matrix = poseStack.last().pose();
        float size = 0.5f;
        float t = 64.0f;
        addQuad(consumer, matrix, -size, size, -size, size, size, size, 0, 1, 0, r, g, b, a, packedLight,
                16 / t, vOff / t, 32 / t, (vOff + 16) / t);
        addQuad(consumer, matrix, -size, -size, size, size, -size, -size, 0, -1, 0, r, g, b, a, packedLight,
                32 / t, vOff / t, 48 / t, (vOff + 16) / t);
        addQuad(consumer, matrix, size, -size, -size, -size, size, -size, 0, 0, -1, r, g, b, a, packedLight,
                16 / t, (vOff + 16) / t, 32 / t, (vOff + 32) / t);
        addQuad(consumer, matrix, -size, -size, size, size, size, size, 0, 0, 1, r, g, b, a, packedLight,
                48 / t, (vOff + 16) / t, 64 / t, (vOff + 32) / t);
        addQuad(consumer, matrix, -size, -size, -size, -size, size, size, -1, 0, 0, r, g, b, a, packedLight,
                0 / t, (vOff + 16) / t, 16 / t, (vOff + 32) / t);
        addQuad(consumer, matrix, size, -size, size, size, size, -size, 1, 0, 0, r, g, b, a, packedLight,
                32 / t, (vOff + 16) / t, 48 / t, (vOff + 32) / t);
    }
    private void addQuad(VertexConsumer consumer, Matrix4f matrix,
                         float x1, float y1, float z1, float x2, float y2, float z2,
                         float nx, float ny, float nz, float r, float g, float b, float a,
                         int light, float u0, float v0, float u1, float v1) {
        if (ny != 0) {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z2).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        } else if (nz != 0) {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y1, z1).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x2, y2, z1).color(r, g, b, a).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        } else {
            consumer.vertex(matrix, x1, y1, z1).color(r, g, b, a).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y1, z2).color(r, g, b, a).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z2).color(r, g, b, a).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
            consumer.vertex(matrix, x1, y2, z1).color(r, g, b, a).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(nx, ny, nz).endVertex();
        }
    }
    private void renderHalo(TileInfusionMatrix tile, PoseStack poseStack, MultiBufferSource buffer,
                            float partialTicks, int craftCount) {
        Random random = new Random(245L);
        int numSpikes = Minecraft.getInstance().options.graphicsMode().get().getId() >= 1 ? 20 : 10;
        float intensity = craftCount / 500.0f;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        com.mojang.blaze3d.systems.RenderSystem.depthMask(false);
        com.mojang.blaze3d.systems.RenderSystem.disableCull();
        com.mojang.blaze3d.systems.RenderSystem.setShader(
                net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        com.mojang.blaze3d.vertex.Tesselator tessellator =
                com.mojang.blaze3d.vertex.Tesselator.getInstance();
        com.mojang.blaze3d.vertex.BufferBuilder buf = tessellator.getBuilder();
        buf.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.TRIANGLES,
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        for (int i = 0; i < numSpikes; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + intensity * 360.0f));
            float length = (random.nextFloat() * 20.0f + 5.0f) / 20.0f * (Math.min(craftCount, 50) / 50.0f);
            float width = (random.nextFloat() * 2.0f + 1.0f) / 20.0f * (Math.min(craftCount, 50) / 50.0f);
            Matrix4f matrix = poseStack.last().pose();
            float alpha = 1.0f - intensity;
            buf.vertex(matrix, 0, 0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            buf.vertex(matrix, -0.866f * width, length, -0.5f * width).color(1.0f, 0.0f, 1.0f, 0.0f).endVertex();
            buf.vertex(matrix, 0.866f * width, length, -0.5f * width).color(1.0f, 0.0f, 1.0f, 0.0f).endVertex();
            buf.vertex(matrix, 0, 0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            buf.vertex(matrix, 0.866f * width, length, -0.5f * width).color(1.0f, 0.0f, 1.0f, 0.0f).endVertex();
            buf.vertex(matrix, 0, length, width).color(1.0f, 0.0f, 1.0f, 0.0f).endVertex();
            buf.vertex(matrix, 0, 0, 0).color(1.0f, 1.0f, 1.0f, alpha).endVertex();
            buf.vertex(matrix, 0, length, width).color(1.0f, 0.0f, 1.0f, 0.0f).endVertex();
            buf.vertex(matrix, -0.866f * width, length, -0.5f * width).color(1.0f, 0.0f, 1.0f, 0.0f).endVertex();
            poseStack.popPose();
        }
        poseStack.popPose();
        tessellator.end();
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.enableCull();
    }
    @Override
    public boolean shouldRenderOffScreen(TileInfusionMatrix tile) {
        return tile.crafting;
    }
}
