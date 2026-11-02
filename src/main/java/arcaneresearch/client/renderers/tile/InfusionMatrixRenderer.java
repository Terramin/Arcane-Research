package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;
import arcaneresearch.init.ModBlocks;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class InfusionMatrixRenderer implements BlockEntityRenderer<TileInfusionMatrix> {
    private static final ResourceLocation TEXTURE_NORMAL =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/infuser_normal.png");
    private static final ResourceLocation TEXTURE_ANCIENT =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/infuser_ancient.png");
    private static final ResourceLocation TEXTURE_ELDRITCH =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/infuser_eldritch.png");

    private static final RenderType CUTOUT_NORMAL = RenderType.entityCutoutNoCull(TEXTURE_NORMAL);
    private static final RenderType CUTOUT_ANCIENT = RenderType.entityCutoutNoCull(TEXTURE_ANCIENT);
    private static final RenderType CUTOUT_ELDRITCH = RenderType.entityCutoutNoCull(TEXTURE_ELDRITCH);
    private static final RenderType EMISSIVE_NORMAL = RenderType.entityTranslucentEmissive(TEXTURE_NORMAL);
    private static final RenderType EMISSIVE_ANCIENT = RenderType.entityTranslucentEmissive(TEXTURE_ANCIENT);
    private static final RenderType EMISSIVE_ELDRITCH = RenderType.entityTranslucentEmissive(TEXTURE_ELDRITCH);

    private static final Quaternionf ROT_XP_90 = Axis.XP.rotationDegrees(90.0f);
    private static final Quaternionf ROT_YP_90 = Axis.YP.rotationDegrees(90.0f);
    private static final Quaternionf ROT_ZP_90 = Axis.ZP.rotationDegrees(90.0f);
    private static final Random HALO_RANDOM = new Random();

    private static final float CUBE_SCALE = 0.45f;
    private static final float CUBE_OFFSET = 0.25f;
    private static final float CUBE_SIZE = 0.5f;
    private static final int GLOW_LIGHT = 0x00F000F0;
    private static final float UV_T = 64.0f;

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull TileInfusionMatrix tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();

        float ticks = mc.player != null ? mc.player.tickCount + partialTicks : 0;
        float startUp = tile.startUp;
        boolean active = tile.active;
        int craftCount = tile.craftCount;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 0.5, 0.5);
            if (startUp > 0) {
                poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f * startUp));
                poseStack.mulPose(Axis.XP.rotationDegrees(35.0f * startUp));
                poseStack.mulPose(Axis.ZP.rotationDegrees(45.0f * startUp));
            }

            float instability = Math.min(6.0f, 1.0f
                    + (tile.stability < 0 ? -tile.stability * 0.66f : 1.0f)
                    * (Math.min(craftCount, 50) / 50.0f));

            int kind = resolvePillarKind(tile);
            VertexConsumer cutoutConsumer = buffer.getBuffer(cutoutFor(kind));

            float wobbleBase = (active && startUp > 0) ? 0.01f * startUp * instability : 0.0f;
            float wX0 = Mth.sin(ticks / 15.0f) * wobbleBase;
            float wX1 = Mth.sin((ticks + 10) / 15.0f) * wobbleBase;
            float wY0 = Mth.sin(ticks / 14.0f) * wobbleBase;
            float wY1 = Mth.sin((ticks + 10) / 14.0f) * wobbleBase;
            float wZ0 = Mth.sin(ticks / 13.0f) * wobbleBase;
            float wZ1 = Mth.sin((ticks + 10) / 13.0f) * wobbleBase;

            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 2; b++) {
                    for (int c = 0; c < 2; c++) {
                        float wX = a == 0 ? wX0 : wX1;
                        float wY = b == 0 ? wY0 : wY1;
                        float wZ = c == 0 ? wZ0 : wZ1;
                        renderOneCube(poseStack, cutoutConsumer, packedLight, packedOverlay,
                                wX, wY, wZ, a, b, c, 1.0f, 1.0f, 1.0f, 1.0f, 0);
                    }
                }
            }

            if (active) {
                VertexConsumer glowConsumer = buffer.getBuffer(emissiveFor(kind));
                for (int a = 0; a < 2; a++) {
                    for (int b = 0; b < 2; b++) {
                        for (int c = 0; c < 2; c++) {
                            float wX = a == 0 ? wX0 : wX1;
                            float wY = b == 0 ? wY0 : wY1;
                            float wZ = c == 0 ? wZ0 : wZ1;
                            float pulse = (Mth.sin((ticks + a * 2 + b * 3 + c * 4) / 4.0f) * 0.1f + 0.2f) * startUp;
                            renderOneCube(poseStack, glowConsumer, GLOW_LIGHT, packedOverlay,
                                    wX, wY, wZ, a, b, c, 0.8f, 0.1f, 1.0f, pulse, 32);
                        }
                    }
                }
            }
        } finally {
            poseStack.popPose();
        }

        if (tile.crafting && craftCount > 0) {
            renderHalo(poseStack, buffer, craftCount);
        }
    }

    private static int resolvePillarKind(TileInfusionMatrix tile) {
        Level level = tile.getLevel();
        if (level == null) return 0;
        BlockPos pos = tile.getBlockPos();
        BlockState pillarState = level.getBlockState(pos.offset(-1, -2, -1));
        if (ModBlocks.ANCIENT_PILLAR != null) {
            Block ancient = ModBlocks.ANCIENT_PILLAR.get();
            if (pillarState.is(ancient)) return 1;
        }
        if (ModBlocks.ELDRITCH_PILLAR != null) {
            Block eldritch = ModBlocks.ELDRITCH_PILLAR.get();
            if (pillarState.is(eldritch)) return 2;
        }
        return 0;
    }

    private static RenderType cutoutFor(int kind) {
        return switch (kind) {
            case 1 -> CUTOUT_ANCIENT;
            case 2 -> CUTOUT_ELDRITCH;
            default -> CUTOUT_NORMAL;
        };
    }

    private static RenderType emissiveFor(int kind) {
        return switch (kind) {
            case 1 -> EMISSIVE_ANCIENT;
            case 2 -> EMISSIVE_ELDRITCH;
            default -> EMISSIVE_NORMAL;
        };
    }

    private static void renderOneCube(PoseStack poseStack, VertexConsumer consumer,
                                      int packedLight, int packedOverlay,
                                      float wX, float wY, float wZ,
                                      int a, int b, int c,
                                      float r, float g, float b2, float alpha, int vOff) {
        int signA = (a == 0) ? -1 : 1;
        int signB = (b == 0) ? -1 : 1;
        int signC = (c == 0) ? -1 : 1;
        poseStack.pushPose();
        try {
            poseStack.translate(wX + signA * CUBE_OFFSET, wY + signB * CUBE_OFFSET, wZ + signC * CUBE_OFFSET);
            if (a > 0) poseStack.mulPose(ROT_XP_90);
            if (b > 0) poseStack.mulPose(ROT_YP_90);
            if (c > 0) poseStack.mulPose(ROT_ZP_90);
            poseStack.scale(CUBE_SCALE, CUBE_SCALE, CUBE_SCALE);
            renderCube(poseStack, consumer, packedLight, packedOverlay, r, g, b2, alpha, vOff);
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderCube(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                   float r, float g, float b, float a, int vOff) {
        Matrix4f matrix = poseStack.last().pose();
        float size = CUBE_SIZE;
        float t = UV_T;
        float v0 = vOff / t;
        float v1 = (vOff + 16) / t;
        float v2 = (vOff + 32) / t;
        addQuad(consumer, matrix, -size, size, -size, size, size, size, 0, 1, 0, r, g, b, a, packedLight,
                16 / t, v0, 32 / t, v1);
        addQuad(consumer, matrix, -size, -size, size, size, -size, -size, 0, -1, 0, r, g, b, a, packedLight,
                32 / t, v0, 48 / t, v1);
        addQuad(consumer, matrix, size, -size, -size, -size, size, -size, 0, 0, -1, r, g, b, a, packedLight,
                16 / t, v1, 32 / t, v2);
        addQuad(consumer, matrix, -size, -size, size, size, size, size, 0, 0, 1, r, g, b, a, packedLight,
                48 / t, v1, 64 / t, v2);
        addQuad(consumer, matrix, -size, -size, -size, -size, size, size, -1, 0, 0, r, g, b, a, packedLight,
                0 / t, v1, 16 / t, v2);
        addQuad(consumer, matrix, size, -size, size, size, size, -size, 1, 0, 0, r, g, b, a, packedLight,
                32 / t, v1, 48 / t, v2);
    }

    private static void addQuad(VertexConsumer consumer, Matrix4f matrix,
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

    private static void renderHalo(PoseStack poseStack, MultiBufferSource buffer, int craftCount) {
        if (poseStack == null || buffer == null) return;
        Minecraft mc = Minecraft.getInstance();
        int gfxId = mc.options.graphicsMode().get().getId();
        int numSpikes = gfxId >= 1 ? 20 : 10;
        float intensity = craftCount / 500.0f;
        float sizeFactor = Math.min(craftCount, 50) / 50.0f;

        Random random = HALO_RANDOM;
        random.setSeed(245L);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buf = tessellator.getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        try {
            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 0.5, 0.5);
                for (int i = 0; i < numSpikes; i++) {
                    poseStack.pushPose();
                    try {
                        poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
                        poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
                        poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
                        poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + intensity * 360.0f));
                        float length = (random.nextFloat() * 20.0f + 5.0f) / 20.0f * sizeFactor;
                        float width = (random.nextFloat() * 2.0f + 1.0f) / 20.0f * sizeFactor;
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
                    } finally {
                        poseStack.popPose();
                    }
                }
            } finally {
                poseStack.popPose();
            }
        } finally {
            tessellator.end();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull TileInfusionMatrix tile) {
        return tile.crafting;
    }
}