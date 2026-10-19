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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import java.util.Random;
@OnlyIn(Dist.CLIENT)
public class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskTile> {
    private static final ResourceLocation CAP_MODEL =
            new ResourceLocation("arcaneresearch", "models/obj/obelisk_cap.obj");
    private static final ResourceLocation TUNNEL =
            new ResourceLocation("arcaneresearch", "textures/misc/tunnel.png");
    private static final ResourceLocation PARTICLEFIELD =
            new ResourceLocation("arcaneresearch", "textures/misc/particlefield.png");
    private static final ResourceLocation PARTICLEFIELD32 =
            new ResourceLocation("arcaneresearch", "textures/misc/particlefield32.png");
    private static final ResourceLocation SIDE =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_side.png");
    private static final ResourceLocation CAP =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_cap.png");
    private static final ResourceLocation SIDE_OUTER =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_side_2.png");
    private static final ResourceLocation CAP_OUTER =
            new ResourceLocation("arcaneresearch", "textures/models/obelisk_cap_2.png");
    private static volatile TurretObjModel CAP_MODEL_OBJ = null;
    private static volatile boolean CAP_MODEL_TRIED = false;
    private static TurretObjModel capModel() {
        if (!CAP_MODEL_TRIED) {
            synchronized (EldritchObeliskRenderer.class) {
                if (!CAP_MODEL_TRIED) {
                    CAP_MODEL_TRIED = true;
                    try {
                        CAP_MODEL_OBJ = TurretObjModel.load(CAP_MODEL);
                    } catch (Throwable ignored) {
                        CAP_MODEL_OBJ = null;
                    }
                }
            }
        }
        return CAP_MODEL_OBJ;
    }
    public EldritchObeliskRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(EldritchObeliskTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        boolean inRange = true;
        try {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            inRange = Minecraft.getInstance().player.distanceToSqr(
                    tile.getBlockPos().getX() + 0.5D,
                    tile.getBlockPos().getY() + 0.5D,
                    tile.getBlockPos().getZ() + 0.5D) < 512.0D;
        } catch (Exception e) {
            return;
        }
        float time = 0.0F;
        try {
            time = Minecraft.getInstance().player.tickCount + partialTicks;
        } catch (Exception ignored) {
            try {
                time = tile.getLevel().getGameTime() + partialTicks;
            } catch (Exception ignored2) {
            }
        }
        float bob = Mth.sin(time / 10.0F) * 0.1F + 0.1F;
        try {
            renderWall(poseStack, buffer, tile, 0.0F, 1.0F + bob, 0.0F, 0.0F, 3, inRange);
            renderWall(poseStack, buffer, tile, 0.0F, 1.0F + bob, 0.0F, 180.0F, 3, inRange);
            renderWall(poseStack, buffer, tile, 0.0F, 1.0F + bob, 0.0F, 90.0F, 3, inRange);
            renderWall(poseStack, buffer, tile, 0.0F, 1.0F + bob, 0.0F, 270.0F, 3, inRange);
        } catch (Throwable ignored) {
        }
        boolean outer = false;
        try {
            outer = tile.getLevel().dimension().location().getPath().contains("outer");
        } catch (Exception ignored) {
        }
        ResourceLocation sideTex = outer ? SIDE_OUTER : SIDE;
        ResourceLocation capTex = outer ? CAP_OUTER : CAP;
        try {
            VertexConsumer sideConsumer = buffer.getBuffer(RenderType.entityTranslucent(sideTex));
            for (int a = 0; a < 4; a++) {
                poseStack.pushPose();
                poseStack.translate(0.5F, 1.0F + bob, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(a * 90));
                poseStack.translate(0.0D, 0.0D, -0.505D);
                quad(poseStack, sideConsumer, packedLight, -0.5F, 0.0F, 0.5F, 3.0F);
                poseStack.popPose();
            }
        } catch (Throwable ignored) {
        }
        try {
            TurretObjModel cap = capModel();
            if (cap != null) {
                VertexConsumer capConsumer = buffer.getBuffer(TCRenderTypes.golemGhost(capTex));
                poseStack.pushPose();
                poseStack.translate(0.5F, 1.0F + bob, 0.5F);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                cap.renderGroup(poseStack, capConsumer, "Cap", packedLight, packedOverlay,
                        1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.5F, 4.0F + bob, 0.5F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                cap.renderGroup(poseStack, capConsumer, "Cap", packedLight, packedOverlay,
                        1.0F, 1.0F, 1.0F, 1.0F);
                poseStack.popPose();
            }
        } catch (Throwable ignored) {
        }
    }
    private void renderWall(PoseStack poseStack, MultiBufferSource buffer, EldritchObeliskTile tile,
                            float x, float y, float z, float yaw, int height, boolean inRange) {
        Random random = new Random(31100L);
        float scroll = (System.currentTimeMillis() % 700000L) / 250000.0F;
        if (inRange) {
            for (int i = 0; i < 16; i++) {
                float brightness = 1.0F / (16 - i + 1.0F);
                ResourceLocation tex = PARTICLEFIELD;
                float uvScale = 0.0625F;
                if (i == 0) {
                    tex = TUNNEL;
                    brightness = 0.1F;
                    uvScale = 0.125F;
                }
                float r = random.nextFloat() * 0.5F + 0.1F;
                float g = random.nextFloat() * 0.5F + 0.4F;
                float b = random.nextFloat() * 0.5F + 0.5F;
                if (i == 0) {
                    r = g = b = 1.0F;
                }
                if (i == 1) {
                    uvScale = 0.5F;
                }
                VertexConsumer consumer = buffer.getBuffer(i == 1
                        ? TCRenderTypes.additiveTexturedNoCull(tex)
                        : RenderType.entityTranslucent(tex));
                float angleDeg = (i * i * 4321 + i * 9) * 2.0F;
                float[][] uvs = portalUvs(tile, x, y, z, yaw, height, scroll, uvScale, angleDeg, i);
                poseStack.pushPose();
                poseStack.translate(0.5F + x, y, 0.5F + z);
                poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
                poseStack.translate(0.0F, 0.0F, 0.5F);
                Matrix4f matrix = poseStack.last().pose();
                float hw = 0.5F;
                consumer.vertex(matrix, -hw, 0.0F, 0.0F)
                        .color(r * brightness, g * brightness, b * brightness, 1.0F)
                        .uv(uvs[0][0], uvs[0][1]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, -hw, height, 0.0F)
                        .color(r * brightness, g * brightness, b * brightness, 1.0F)
                        .uv(uvs[1][0], uvs[1][1]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, hw, height, 0.0F)
                        .color(r * brightness, g * brightness, b * brightness, 1.0F)
                        .uv(uvs[2][0], uvs[2][1]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                consumer.vertex(matrix, hw, 0.0F, 0.0F)
                        .color(r * brightness, g * brightness, b * brightness, 1.0F)
                        .uv(uvs[3][0], uvs[3][1]).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0)
                        .normal(0.0F, 0.0F, 1.0F).endVertex();
                poseStack.popPose();
            }
        } else {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(PARTICLEFIELD32));
            poseStack.pushPose();
            poseStack.translate(0.5F + x, y, 0.5F + z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            Matrix4f matrix = poseStack.last().pose();
            consumer.vertex(matrix, -0.5F, 0.0F, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(1.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, -0.5F, height, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(1.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, 0.5F, height, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(0.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, 0.5F, 0.0F, 0.5F).color(0.5F, 0.5F, 0.5F, 0.65F).uv(0.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
            poseStack.popPose();
        }
    }
    private static float[][] portalUvs(EldritchObeliskTile tile,
                                       float x, float y, float z, float yaw, int height,
                                       float scroll, float uvScale, float angleDeg, int layer) {
        double rad = Math.toRadians(yaw);
        double c = Math.cos(rad);
        double s = Math.sin(rad);
        boolean useX = Math.abs(yaw - 90.0F) < 1.0F || Math.abs(yaw - 270.0F) < 1.0F;
        double bx;
        double by;
        double bz;
        try {
            bx = tile.getBlockPos().getX();
            by = tile.getBlockPos().getY();
            bz = tile.getBlockPos().getZ();
        } catch (Exception e) {
            bx = 0.0D;
            by = 0.0D;
            bz = 0.0D;
        }
        double hw = 0.5D;
        double lz = 0.5D;
        double[][] local = {{-hw, 0.0D}, {-hw, height}, {hw, height}, {hw, 0.0D}};
        float[][] uvs = new float[4][2];
        double cu = 0.0D;
        double cv = 0.0D;
        for (int k = 0; k < 4; k++) {
            double lx = local[k][0];
            double ly = local[k][1];
            double rx = lx * c + lz * s;
            double rz = -lx * s + lz * c;
            double uCoord = (useX ? (bz + 0.5D + z + rz) : (bx + 0.5D + x + rx)) * uvScale + scroll;
            double vCoord = (by + y + ly) * uvScale + scroll;
            uvs[k][0] = (float) uCoord;
            uvs[k][1] = (float) vCoord;
            cu += uCoord;
            cv += vCoord;
        }
        cu /= 4.0D;
        cv /= 4.0D;
        float cos = Mth.cos((float) Math.toRadians(angleDeg));
        float sin = Mth.sin((float) Math.toRadians(angleDeg));
        for (float[] uv : uvs) {
            float dx = (float) (uv[0] - cu);
            float dy = (float) (uv[1] - cv);
            uv[0] = (float) (cu + dx * cos - dy * sin);
            uv[1] = (float) (cv + dx * sin + dy * cos);
        }
        return uvs;
    }
    private static void quad(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                             float x0, float y0, float x1, float y1) {
        Matrix4f matrix = poseStack.last().pose();
        consumer.vertex(matrix, x0, y1, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x1, y1, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x1, y0, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(matrix, x0, y0, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, -1.0F).endVertex();
    }
    @Override
    public boolean shouldRenderOffScreen(EldritchObeliskTile tile) {
        return true;
    }
}
