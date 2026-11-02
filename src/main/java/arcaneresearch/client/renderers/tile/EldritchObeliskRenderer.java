package arcaneresearch.client.renderers.tile;

import arcaneresearch.common.tiles.aura.EldritchObeliskTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskTile> {
    private static final ResourceLocation CAP_MODEL =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "models/obj/obelisk_cap.obj");
    private static final ResourceLocation TUNNEL =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/tunnel.png");
    private static final ResourceLocation PARTICLEFIELD =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/particlefield.png");
    private static final ResourceLocation PARTICLEFIELD32 =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/particlefield32.png");
    private static final ResourceLocation SIDE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_side.png");
    private static final ResourceLocation CAP =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap.png");
    private static final ResourceLocation SIDE_OUTER =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_side_2.png");
    private static final ResourceLocation CAP_OUTER =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap_2.png");

    private static final RenderType TUNNEL_TYPE = RenderType.entityTranslucent(TUNNEL);
    private static final RenderType PARTICLEFIELD_ADDITIVE = TCRenderTypes.additiveTexturedNoCull(PARTICLEFIELD);
    private static final RenderType PARTICLEFIELD_TRANSLUCENT = RenderType.entityTranslucent(PARTICLEFIELD);
    private static final RenderType PARTICLEFIELD32_TYPE = RenderType.entityTranslucent(PARTICLEFIELD32);
    private static final RenderType SIDE_INNER_TYPE = RenderType.entityTranslucent(SIDE);
    private static final RenderType SIDE_OUTER_TYPE = RenderType.entityTranslucent(SIDE_OUTER);
    private static final RenderType CAP_INNER_TYPE = TCRenderTypes.golemGhost(CAP);
    private static final RenderType CAP_OUTER_TYPE = TCRenderTypes.golemGhost(CAP_OUTER);

    private static final Quaternionf[] YAW_ROTATIONS = new Quaternionf[4];
    private static final Quaternionf ROT_XP_90 = Axis.XP.rotationDegrees(90.0F);
    private static final Quaternionf ROT_XP_NEG_90 = Axis.XP.rotationDegrees(-90.0F);

    private static final int LAYERS = 16;
    private static final int WALL_HEIGHT = 3;
    private static final float HALF_WIDTH = 0.5F;
    private static final float LOCAL_Z = 0.5F;

    private static final float[] LAYER_BRIGHTNESS = new float[LAYERS];
    private static final float[] LAYER_UV_SCALE = new float[LAYERS];
    private static final float[] LAYER_RGB_R = new float[LAYERS];
    private static final float[] LAYER_RGB_G = new float[LAYERS];
    private static final float[] LAYER_RGB_B = new float[LAYERS];
    private static final float[] LAYER_ROT_COS = new float[LAYERS];
    private static final float[] LAYER_ROT_SIN = new float[LAYERS];

    private static final float[] YAW_COS = new float[4];
    private static final float[] YAW_SIN = new float[4];
    private static final boolean[] YAW_USE_X = new boolean[4];

    static {
        for (int i = 0; i < 4; i++) {
            float yawDeg = i * 90.0F;
            YAW_ROTATIONS[i] = Axis.YP.rotationDegrees(yawDeg);
            float rad = (float) Math.toRadians(yawDeg);
            YAW_COS[i] = (float) Math.cos(rad);
            YAW_SIN[i] = (float) Math.sin(rad);
            YAW_USE_X[i] = (i == 1 || i == 3);
        }

        Random rng = new Random(31100L);
        for (int i = 0; i < LAYERS; i++) {
            float brightness = 1.0F / (16 - i + 1.0F);
            float uvScale = 0.0625F;
            if (i == 0) {
                brightness = 0.1F;
                uvScale = 0.125F;
            }
            if (i == 1) {
                uvScale = 0.5F;
            }
            LAYER_BRIGHTNESS[i] = brightness;
            LAYER_UV_SCALE[i] = uvScale;

            float r = rng.nextFloat() * 0.5F + 0.1F;
            float g = rng.nextFloat() * 0.5F + 0.4F;
            float b = rng.nextFloat() * 0.5F + 0.5F;
            if (i == 0) {
                r = g = b = 1.0F;
            }
            LAYER_RGB_R[i] = r;
            LAYER_RGB_G[i] = g;
            LAYER_RGB_B[i] = b;

            float angleDeg = (i * i * 4321 + i * 9) * 2.0F;
            float rad = (float) Math.toRadians(angleDeg);
            LAYER_ROT_COS[i] = Mth.cos(rad);
            LAYER_ROT_SIN[i] = Mth.sin(rad);
        }
    }

    private static final class CapModelHolder {
        static final TurretObjModel MODEL = load();

        private static TurretObjModel load() {
            try {
                return TurretObjModel.load(CAP_MODEL);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull EldritchObeliskTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        BlockPos pos = tile.getBlockPos();

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean inRange = player.distanceToSqr(
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) < 512.0D;

        float time = player.tickCount + partialTicks;
        float bob = Mth.sin(time / 10.0F) * 0.1F + 0.1F;
        float scroll = (System.currentTimeMillis() % 700000L) / 250000.0F;

        try {
            renderWall(poseStack, buffer, pos, 1.0F + bob, 0, inRange, scroll);
            renderWall(poseStack, buffer, pos, 1.0F + bob, 2, inRange, scroll);
            renderWall(poseStack, buffer, pos, 1.0F + bob, 1, inRange, scroll);
            renderWall(poseStack, buffer, pos, 1.0F + bob, 3, inRange, scroll);
        } catch (Throwable ignored) {
        }

        boolean outer = false;
        try {
            ResourceLocation dimLoc = level.dimension().location();
            outer = dimLoc.getPath().contains("outer");
        } catch (Exception ignored) {
        }

        try {
            VertexConsumer sideConsumer = buffer.getBuffer(outer ? SIDE_OUTER_TYPE : SIDE_INNER_TYPE);
            for (int a = 0; a < 4; a++) {
                poseStack.pushPose();
                try {
                    poseStack.translate(0.5F, 1.0F + bob, 0.5F);
                    poseStack.mulPose(YAW_ROTATIONS[a]);
                    poseStack.translate(0.0D, 0.0D, -0.505D);
                    quad(poseStack, sideConsumer, packedLight);
                } finally {
                    poseStack.popPose();
                }
            }
        } catch (Throwable ignored) {
        }

        try {
            TurretObjModel cap = CapModelHolder.MODEL;
            if (cap != null) {
                VertexConsumer capConsumer = buffer.getBuffer(outer ? CAP_OUTER_TYPE : CAP_INNER_TYPE);

                poseStack.pushPose();
                try {
                    poseStack.translate(0.5F, 1.0F + bob, 0.5F);
                    poseStack.mulPose(ROT_XP_90);
                    cap.renderGroup(poseStack, capConsumer, "Cap", packedLight, packedOverlay,
                            1.0F, 1.0F, 1.0F, 1.0F);
                } finally {
                    poseStack.popPose();
                }

                poseStack.pushPose();
                try {
                    poseStack.translate(0.5F, 4.0F + bob, 0.5F);
                    poseStack.mulPose(ROT_XP_NEG_90);
                    cap.renderGroup(poseStack, capConsumer, "Cap", packedLight, packedOverlay,
                            1.0F, 1.0F, 1.0F, 1.0F);
                } finally {
                    poseStack.popPose();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static void renderWall(PoseStack poseStack, MultiBufferSource buffer, BlockPos pos,
                                   float y, int yawIndex,
                                   boolean inRange, float scroll) {
        if (!inRange) {
            VertexConsumer consumer = buffer.getBuffer(PARTICLEFIELD32_TYPE);
            poseStack.pushPose();
            try {
                poseStack.translate(0.5F + (float) 0.0, y, 0.5F + (float) 0.0);
                poseStack.mulPose(YAW_ROTATIONS[yawIndex]);
                Matrix4f matrix = poseStack.last().pose();
                consumer.vertex(matrix, -0.5F, 0.0F, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(1.0F, 1.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, -0.5F, WALL_HEIGHT, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(1.0F, 0.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, 0.5F, WALL_HEIGHT, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(0.0F, 0.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, 0.5F, 0.0F, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(0.0F, 1.0F)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
            } finally {
                poseStack.popPose();
            }
            return;
        }

        float yawCos = YAW_COS[yawIndex];
        float yawSin = YAW_SIN[yawIndex];
        boolean useX = YAW_USE_X[yawIndex];
        float bx = pos.getX();
        float by = pos.getY();
        float bz = pos.getZ();

        float hw_c = HALF_WIDTH * yawCos;
        float hw_s = HALF_WIDTH * yawSin;
        float lz_s = LOCAL_Z * yawSin;
        float lz_c = LOCAL_Z * yawCos;
        float rxLeft = -hw_c + lz_s;
        float rzLeft = hw_s + lz_c;
        float rxRight = hw_c + lz_s;
        float rzRight = -hw_s + lz_c;

        float leftBase;
        float rightBase;
        if (useX) {
            leftBase = bz + 0.5F + (float) 0.0 + rzLeft;
            rightBase = bz + 0.5F + (float) 0.0 + rzRight;
        } else {
            leftBase = bx + 0.5F + (float) 0.0 + rxLeft;
            rightBase = bx + 0.5F + (float) 0.0 + rxRight;
        }
        float vBaseBottom = by + y;
        float vBaseTop = by + y + WALL_HEIGHT;

        for (int i = 0; i < LAYERS; i++) {
            float brightness = LAYER_BRIGHTNESS[i];
            float uvScale = LAYER_UV_SCALE[i];
            float r = LAYER_RGB_R[i] * brightness;
            float g = LAYER_RGB_G[i] * brightness;
            float b = LAYER_RGB_B[i] * brightness;

            RenderType texType;
            if (i == 0) {
                texType = TUNNEL_TYPE;
            } else if (i == 1) {
                texType = PARTICLEFIELD_ADDITIVE;
            } else {
                texType = PARTICLEFIELD_TRANSLUCENT;
            }
            VertexConsumer consumer = buffer.getBuffer(texType);

            float uLeft = leftBase * uvScale + scroll;
            float uRight = rightBase * uvScale + scroll;
            float vBottom = vBaseBottom * uvScale + scroll;
            float vTop = vBaseTop * uvScale + scroll;

            float cu = (uLeft + uRight) * 0.5F;
            float cv = (vBottom + vTop) * 0.5F;
            float du = (uLeft - uRight) * 0.5F;
            float dv = (vBottom - vTop) * 0.5F;

            float rotCos = LAYER_ROT_COS[i];
            float rotSin = LAYER_ROT_SIN[i];
            float a = du * rotCos;
            float bb = du * rotSin;
            float c = dv * rotCos;
            float d = dv * rotSin;

            float u0 = cu + a - d;
            float v0 = cv + bb + c;
            float u1 = cu + a + d;
            float v1 = cv + bb - c;
            float u2 = cu - a + d;
            float v2 = cv - bb - c;
            float u3 = cu - a - d;
            float v3 = cv - bb + c;

            poseStack.pushPose();
            try {
                poseStack.translate(0.5F + (float) 0.0, y, 0.5F + (float) 0.0);
                poseStack.mulPose(YAW_ROTATIONS[yawIndex]);
                poseStack.translate(0.0F, 0.0F, LOCAL_Z);
                Matrix4f matrix = poseStack.last().pose();
                consumer.vertex(matrix, -HALF_WIDTH, 0.0F, 0.0F)
                        .color(r, g, b, 1.0F).uv(u0, v0)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, -HALF_WIDTH, WALL_HEIGHT, 0.0F)
                        .color(r, g, b, 1.0F).uv(u1, v1)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, HALF_WIDTH, WALL_HEIGHT, 0.0F)
                        .color(r, g, b, 1.0F).uv(u2, v2)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, HALF_WIDTH, 0.0F, 0.0F)
                        .color(r, g, b, 1.0F).uv(u3, v3)
                        .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
            } finally {
                poseStack.popPose();
            }
        }
    }

    private static void quad(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();
        consumer.vertex(matrix, (float) -0.5, (float) 3.0, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, (float) 0.5, (float) 3.0, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, (float) 0.5, (float) 0.0, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, (float) -0.5, (float) 0.0, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull EldritchObeliskTile tile) {
        return true;
    }
}