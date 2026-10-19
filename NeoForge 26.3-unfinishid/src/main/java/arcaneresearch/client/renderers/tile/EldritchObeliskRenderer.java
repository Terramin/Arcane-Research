package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchObeliskTile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

import java.util.Random;

/**
 * Р РµРЅРґРµСЂ РѕР±РµР»РёСЃРєР°, РїРѕСЂС‚ TC4 TileEldritchObeliskRenderer 1:1 (С‡РµСЂРµР· TC5):
 * 4 РїРѕСЂС‚Р°Р»СЊРЅС‹Рµ СЃС‚РµРЅС‹ (16 СЃР»РѕС‘РІ РІР±Р»РёР·Рё, СЃС‚Р°С‚РёС‡РЅС‹Р№ РєРІР°Рґ РІРґР°Р»Рё),
 * 4 Р±РѕРєРѕРІС‹Рµ РіСЂР°РЅРё РїР°СЂСЏС‰РµРіРѕ РїРёР»РѕРЅР° 1x1x3 + 2 РєР°РїР° РёР· obelisk_cap.obj.
 * РџРёР»РѕРЅ Р»РµРІРёС‚РёСЂСѓРµС‚: y+1+bob, bob = sin(time/10)*0.1+0.1.
 * Р’Рѕ Р’РЅРµС€РЅРёС… Р·РµРјР»СЏС… (РїСѓС‚СЊ РґРёРјРµРЅС€РЅР° СЃРѕРґРµСЂР¶РёС‚ "outer") вЂ” С‚РµРєСЃС‚СѓСЂС‹ _2.
 * Р›РµР¶РёС‚ РІ arcaneresearch.arcaneresearch.client.renderers.tile.
 * NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
 * via submitCustomGeometry.
 */
public class EldritchObeliskRenderer implements BlockEntityRenderer<EldritchObeliskTile, EldritchObeliskRenderer.State> {
    private static final Identifier CAP_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/obelisk_cap.obj");
    private static final Identifier TUNNEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/tunnel.png");
    private static final Identifier PARTICLEFIELD =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/particlefield.png");
    private static final Identifier PARTICLEFIELD32 =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/particlefield32.png");
    private static final Identifier SIDE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_side.png");
    private static final Identifier CAP =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap.png");
    private static final Identifier SIDE_OUTER =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_side_2.png");
    private static final Identifier CAP_OUTER =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap_2.png");

    public static class State extends BlockEntityRenderState {
        public boolean present;
        public boolean inRange;
        public float bob;
        public boolean outer;
        public double bx;
        public double by;
        public double bz;
    }

    /** РљР°Рї РіСЂСѓР·РёРј РѕРґРёРЅ СЂР°Р·: TurretObjModel.load РєР°Р¶РґС‹Р№ РєР°РґСЂ РїСЂРё Р±РёС‚РѕРј СЂРµСЃСѓСЂСЃРµ СЃРїР°РјРёС‚ РѕС€РёР±РєР°РјРё. */
    private static volatile TurretObjModel CAP_MODEL_OBJ = null;
    private static volatile boolean CAP_MODEL_TRIED = false;

    /** Double-checked locking: СЂРµРЅРґРµСЂ РјРѕРіСѓС‚ РґС‘СЂРіР°С‚СЊ РїР°СЂР°Р»Р»РµР»СЊРЅРѕ. */
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
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchObeliskTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        if (tile.getLevel() == null) {
            state.present = false;
            return;
        }
        boolean inRange = true;
        float time = 0.0F;
        try {
            if (Minecraft.getInstance().player == null) {
                state.present = false;
                return;
            }
            inRange = Minecraft.getInstance().player.distanceToSqr(
                    tile.getBlockPos().getX() + 0.5D,
                    tile.getBlockPos().getY() + 0.5D,
                    tile.getBlockPos().getZ() + 0.5D) < 512.0D;
        } catch (Exception e) {
            state.present = false;
            return;
        }

        try {
            time = Minecraft.getInstance().player.tickCount + partialTicks;
        } catch (Exception ignored) {
            try {
                time = tile.getLevel().getGameTime() + partialTicks;
            } catch (Exception ignored2) {
            }
        }
        state.present = true;
        state.inRange = inRange;
        state.bob = Mth.sin(time / 10.0F) * 0.1F + 0.1F;

        boolean outer = false;
        try {
            // NEOFORGE_PORT: dimension().location() -> dimension().identifier().
            outer = tile.getLevel().dimension().identifier().getPath().contains("outer");
        } catch (Exception ignored) {
        }
        state.outer = outer;
        state.bx = tile.getBlockPos().getX();
        state.by = tile.getBlockPos().getY();
        state.bz = tile.getBlockPos().getZ();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.present) {
            return;
        }
        float bob = state.bob;
        boolean inRange = state.inRange;

        // 4 РїРѕСЂС‚Р°Р»СЊРЅС‹Рµ СЃС‚РµРЅС‹ TC4 (drawPlaneZNeg/ZPos/XNeg/XPos, height 3).
        try {
            renderWall(poseStack, collector, state, 0.0F, 1.0F + bob, 0.0F, 0.0F, 3, inRange);
            renderWall(poseStack, collector, state, 0.0F, 1.0F + bob, 0.0F, 180.0F, 3, inRange);
            renderWall(poseStack, collector, state, 0.0F, 1.0F + bob, 0.0F, 90.0F, 3, inRange);
            renderWall(poseStack, collector, state, 0.0F, 1.0F + bob, 0.0F, 270.0F, 3, inRange);
        } catch (Throwable ignored) {
        }

        Identifier sideTex = state.outer ? SIDE_OUTER : SIDE;
        Identifier capTex = state.outer ? CAP_OUTER : CAP;

        // 4 Р±РѕРєРѕРІС‹Рµ РіСЂР°РЅРё РїРёР»РѕРЅР° TC4 (renderSide, 1x1x3).
        // РўРµРєСЃС‚СѓСЂР° РїРѕР»СѓРїСЂРѕР·СЂР°С‡РЅР°СЏ (25+ СѓСЂРѕРІРЅРµР№ Р°Р»СЊС„С‹), РІ TC4 СЂРёСЃРѕРІР°Р»Р°СЃСЊ
        // Р±Р»РµРЅРґРѕРј SRC_ALPHA/ONE_MINUS_SRC_ALPHA РїРѕРІРµСЂС… РїРѕСЂС‚Р°Р»Р° вЂ” РїРѕСЌС‚РѕРјСѓ
        // entityTranslucent, Р° РЅРµ cutout: cutout РіР»СѓС€РёР» РїРѕСЂС‚Р°Р» РіСЂР°РЅСЏРјРё.
        try {
            RenderType sideType = TCRenderTypes.translucentPortal(sideTex);
            int light = state.lightCoords;
            for (int a = 0; a < 4; a++) {
                poseStack.pushPose();
                poseStack.translate(0.5F, 1.0F + bob, 0.5F);
                poseStack.rotate(Axis.YP.rotationDegrees(a * 90));
                // Р“СЂР°РЅРё РЅР° РІРѕР»РѕСЃРѕРє (0.005) СЃРЅР°СЂСѓР¶Рё СЃС‚РµРЅ: СЃС‚СЂРѕРіРѕ РІРїРµСЂРµРґРё РІСЃРµС…
                // СЃР»РѕС‘РІ РїРѕСЂС‚Р°Р»Р°, РґРµС‚РµСЂРјРёРЅРёСЂРѕРІР°РЅРЅРѕ, Р±РµР· РєРѕРїР»Р°РЅР°СЂРЅРѕРіРѕ С„Р»РёРєР°.
                poseStack.translate(0.0D, 0.0D, -0.505D);
                quad(poseStack, collector, sideType, light, -0.5F, 0.0F, 0.5F, 3.0F);
                poseStack.popPose();
            }
        } catch (Throwable ignored) {
        }

        // 2 Р»РµС‚Р°СЋС‰РёС… РєР°РїР°-РїРёР»РѕРЅР° TC4 (model.renderPart("Cap") СЃРЅРёР·Сѓ Рё СЃРІРµСЂС…Сѓ).
        // Р’ TC4 РІРµСЃСЊ СѓР·РµР» РіСЂР°РЅРµР№+РєР°РїРѕРІ СЂРёСЃРѕРІР°Р»СЃСЏ СЃ РІРєР»СЋС‡С‘РЅРЅС‹Рј Р±Р»РµРЅРґРѕРј
        // (glEnable BLEND, blendFunc SRC_ALPHA/ONE_MINUS_SRC_ALPHA), РїРѕСЌС‚РѕРјСѓ
        // РєР°РїС‹ вЂ” РїРѕР»СѓРїСЂРѕР·СЂР°С‡РЅС‹Рј С‚РёРїРѕРј РґР»СЏ С‚СЂРµСѓРіРѕР»СЊРЅРёРєРѕРІ (golemGhost:
        // translucent + depth write + LEQUAL, РєР°Рє Сѓ РѕСЂРёРіРёРЅР°Р»Р°),
        // Р° РЅРµ opaque-cutout.
        try {
            TurretObjModel cap = capModel();
            if (cap != null) {
                RenderType capType = TCRenderTypes.golemGhost(capTex);
                int light = state.lightCoords;
                poseStack.pushPose();
                poseStack.translate(0.5F, 1.0F + bob, 0.5F);
                poseStack.rotate(Axis.XP.rotationDegrees(90.0F));
                emitCap(poseStack, collector, capType, cap, light);
                poseStack.popPose();
                poseStack.pushPose();
                poseStack.translate(0.5F, 4.0F + bob, 0.5F);
                poseStack.rotate(Axis.XP.rotationDegrees(-90.0F));
                emitCap(poseStack, collector, capType, cap, light);
                poseStack.popPose();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void emitCap(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                TurretObjModel cap, int light) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            cap.renderGroup(work, consumer, "Cap", light, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
        });
    }

    private void renderWall(PoseStack poseStack, SubmitNodeCollector collector, State state,
                            float x, float y, float z, float yaw, int height, boolean inRange) {
        Random random = new Random(31100L);
        float scroll = (System.currentTimeMillis() % 700000L) / 250000.0F;
        if (inRange) {
            for (int i = 0; i < 16; i++) {
                float brightness = 1.0F / (16 - i + 1.0F);
                Identifier tex = PARTICLEFIELD;
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
                RenderType type = i == 1
                        ? TCRenderTypes.additiveTexturedNoCull(tex)
                        : TCRenderTypes.translucentPortal(tex);
                // TC4 РєСЂСѓС‚РёР» РўР•РљРЎРўРЈР РЈ С‡РµСЂРµР· texgen (EYE_LINEAR + GL_TEXTURE
                // РјР°С‚СЂРёС†Р°): СѓР·РѕСЂ РїСЂРёРІСЏР·Р°РЅ Рє РњРР РЈ, Р° РЅРµ Рє РіСЂР°РЅРё вЂ” РёРЅР°С‡Рµ РѕРЅ
                // С‚РµС‡С‘С‚ РїРѕ РїРѕРІРµСЂС…РЅРѕСЃС‚Рё РєР°Рє Р¶РёРґРєРѕСЃС‚СЊ. Р“РµРѕРјРµС‚СЂРёСЏ РІСЃРµС… 16 СЃР»РѕС‘РІ
                // СЃС‚СЂРѕРіРѕ РєРѕРїР»Р°РЅР°СЂРЅР°; UV СЃС‡РёС‚Р°РµРј РёР· РјРёСЂРѕРІС‹С… РєРѕРѕСЂРґРёРЅР°С‚ РІРµСЂС€РёРЅ.
                float angleDeg = (i * i * 4321 + i * 9) * 2.0F;
                float[][] uvs = portalUvs(state, x, y, z, yaw, height, scroll, uvScale, angleDeg, i);
                poseStack.pushPose();
                poseStack.translate(0.5F + x, y, 0.5F + z);
                poseStack.rotate(Axis.YP.rotationDegrees(yaw));
                // РЎС‚СЂРѕРіРѕ РєРѕРїР»Р°РЅР°СЂРЅРѕ РєР°Рє РІ TC4: РїРѕСЂСЏРґРѕРє СЂРµС€Р°СЋС‚ РїР°СЃСЃС‹ Р±СѓС„РµСЂРѕРІ
                // (СЃС‚РµРЅС‹ в†’ РіСЂР°РЅРё в†’ РєР°РїС‹), РґРµС‚РµСЂРјРёРЅРёСЂРѕРІР°РЅРЅРѕ, Р±РµР· РјРµСЂС†Р°РЅРёСЏ.
                // Р­РїСЃРёР»РѕРЅС‹ С‚СѓС‚ С‚РѕР»СЊРєРѕ СЂР°СЃСЃР»Р°РёРІР°Р»Рё РїР»РѕСЃРєРѕСЃС‚Рё Рё РґР°РІР°Р»Рё
                // В«Р°РЅРёРјРёСЂРѕРІР°РЅРЅС‹РµВ» z-fight РїСЏС‚РЅР° Р±РµР· РїСЂРѕР·СЂР°С‡РЅРѕСЃС‚Рё.
                poseStack.translate(0.0F, 0.0F, 0.5F);
                float hw = 0.5F;
                final float fr = r * brightness, fg = g * brightness, fb = b * brightness;
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                vquad(consumer, pose, -hw, 0.0F, uvs[0][0], uvs[0][1], fr, fg, fb, 1.0F);
                vquad(consumer, pose, -hw, height, uvs[1][0], uvs[1][1], fr, fg, fb, 1.0F);
                vquad(consumer, pose, hw, height, uvs[2][0], uvs[2][1], fr, fg, fb, 1.0F);
                vquad(consumer, pose, hw, 0.0F, uvs[3][0], uvs[3][1], fr, fg, fb, 1.0F);
            });
                poseStack.popPose();
            }
        } else {
            // Р”Р°Р»СЊРЅРёР№ РєРІР°Рґ TC4 С‚РѕР¶Рµ Р±С‹Р» РіР»СѓС…РёРј, РЅРѕ С‚РµРєСЃС‚СѓСЂР° particlefield32
            // РїРѕР»РЅРѕСЃС‚СЊСЋ РЅРµРїСЂРѕР·СЂР°С‡РЅР°СЏ вЂ” РєР°С‚-Р°СѓС‚ РґР°РІР°Р» Р¶С‘СЃС‚РєРёР№ РїР°РЅС†РёСЂСЊ РІРґР°Р»Рё.
            // Р‘Р»РµРЅРґ + Р°Р»СЊС„Р° 0.65: РІСѓР°Р»СЊ РІРјРµСЃС‚Рѕ С‰РёС‚Р°, Р±Р»РµРЅРґ С‚РµРїРµСЂСЊ РІРµР·РґРµ.
            RenderType type = TCRenderTypes.translucentPortal(PARTICLEFIELD32);
            poseStack.pushPose();
            poseStack.translate(0.5F + x, y, 0.5F + z);
            poseStack.rotate(Axis.YP.rotationDegrees(yaw));
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                vquad(consumer, pose, -0.5F, 0.0F, 1.0F, 1.0F, 0.5F, 0.5F, 0.5F, 0.65F);
                vquad(consumer, pose, -0.5F, height, 1.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.65F);
                vquad(consumer, pose, 0.5F, height, 0.0F, 0.0F, 0.5F, 0.5F, 0.5F, 0.65F);
                vquad(consumer, pose, 0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F, 0.5F, 0.65F);
            });
            poseStack.popPose();
        }
    }

    private static void vquad(VertexConsumer consumer, PoseStack.Pose pose,
                              float x, float y, float u, float v,
                              float r, float g, float b, float a) {
        // NEOFORGE_PORT: z=0 (РїРѕР·Р° СѓР¶Рµ СЃРґРІРёРЅСѓС‚Р° РЅР° +0.5, РєР°Рє РІС‹Р·РѕРІС‹ translate РІ Forge).
        consumer.addVertex(pose, x, y, 0.0F)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    /**
     * UV РїРѕСЂС‚Р°Р»СЊРЅРѕР№ СЃС‚РµРЅС‹ TC4: planar-РїСЂРѕРµРєС†РёСЏ РјРёСЂРѕРІС‹С… РєРѕРѕСЂРґРёРЅР°С‚ РІРµСЂС€РёРЅС‹
     * (Р·Р°РјРµРЅР° texgen EYE_LINEAR) + СЃРєСЂРѕР»Р» РІСЂРµРјРµРЅРё + СЃС‚Р°С‚РёС‡РЅС‹Р№ РїРѕРІРѕСЂРѕС‚ СЃР»РѕСЏ.
     * РЈР·РѕСЂ СЃС‚РѕРёС‚ РІ РјРёСЂРµ, Р° РЅРµ С‚РµС‡С‘С‚ РїРѕ РіСЂР°РЅРё. РџРѕСЂСЏРґРѕРє СѓРіР»РѕРІ:
     * (-hw,0),(-hw,h),(hw,h),(hw,0) вЂ” РєР°Рє РіРµРѕРјРµС‚СЂРёСЏ РІС‹С€Рµ.
     */
    private static float[][] portalUvs(State state,
                                       float x, float y, float z, float yaw, int height,
                                       float scroll, float uvScale, float angleDeg, int layer) {
        double rad = Math.toRadians(yaw);
        double c = Math.cos(rad);
        double s = Math.sin(rad);
        boolean useX = Math.abs(yaw - 90.0F) < 1.0F || Math.abs(yaw - 270.0F) < 1.0F;
        double bx = state.bx;
        double by = state.by;
        double bz = state.bz;
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

    private static void quad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, int packedLight,
                             float x0, float y0, float x1, float y1) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            consumer.addVertex(pose, x0, y1, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
            consumer.addVertex(pose, x1, y1, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(1.0F, 1.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
            consumer.addVertex(pose, x1, y0, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(1.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
            consumer.addVertex(pose, x0, y0, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 0.0F)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 0.0F, -1.0F);
        });
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}