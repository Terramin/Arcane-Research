package arcaneresearch.client.renderers;

import com.mojang.blaze3d.platform.NativeImage;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.seals.ISealConfigArea;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.seals.SealPos;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.entity.golem.GolemRenderHooks;
import arcaneresearch.common.golems.seals.SealEntity;
import arcaneresearch.common.golems.seals.SealHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// NEOFORGE_PORT (26.3 render-state rework): Forge renderSeals() body 1:1,
// emitted at submit time (SubmitCustomGeometryEvent) with camera-relative
// coords. Kept: dist fade, redstone-inactive flicker sparks, animated seal
// quads, rotating area disc, area corner frames.
public class SealRenderer {

    private static final Identifier DEFAULT_SEAL_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/item/seals/seal_blank.png");

    private static final Identifier MIDDLE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/seal_area.png");
    private static final Identifier CFRAME =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/frame_corner.png");
    private static final Direction[][] ROT_FACES = {
            {Direction.DOWN, Direction.NORTH, Direction.WEST},
            {Direction.UP, Direction.NORTH, Direction.WEST},
            {Direction.DOWN, Direction.NORTH, Direction.EAST},
            {Direction.UP, Direction.NORTH, Direction.EAST},
            {Direction.DOWN, Direction.SOUTH, Direction.EAST},
            {Direction.UP, Direction.SOUTH, Direction.EAST},
            {Direction.DOWN, Direction.SOUTH, Direction.WEST},
            {Direction.UP, Direction.SOUTH, Direction.WEST}
    };
    private static final int[][] ROT_MAT = {
            {0, 270, 0}, {270, 180, 270}, {90, 0, 90}, {180, 90, 180},
            {180, 180, 0}, {90, 270, 270}, {270, 90, 90}, {0, 0, 180}
    };

    private static final double MAX_RENDER_DIST_SQ = 256.0;

    private static final Map<Identifier, FrameStrip> FRAME_CACHE = new ConcurrentHashMap<>();

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onSubmitGeometry(
                net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || !isHoldingSealDisplayer(player)) {
                return;
            }
            float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            renderSeals(event.getSubmitNodeCollector(), partialTick, player);
        }

        private static boolean isHoldingSealDisplayer(Player player) {
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof arcaneresearch.api.golems.ISealDisplayer) {
                return true;
            }
            ItemStack offHand = player.getOffhandItem();
            return !offHand.isEmpty() && offHand.getItem() instanceof arcaneresearch.api.golems.ISealDisplayer;
        }
    }

    public static void renderSeals(PoseStack poseStack, float partialTick, Player player) {
        // NEOFORGE_PORT: kept for API parity (ClientRenderEvents); emission now
        // happens in Events.onSubmitGeometry (submit-system, no immediate draws).
    }

    private static void renderSeals(SubmitNodeCollector collector, float partialTick, Player player) {
        if (player == null || player.level() == null) return;

        String dimKey = player.level().dimension().identifier().toString();
        ConcurrentHashMap<SealPos, SealEntity> seals = SealHandler.sealEntities.get(dimKey);

        if (seals == null || seals.isEmpty()) return;

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();

        // NEOFORGE_PORT: sneak X-ray (depth test off) has no vanilla pipeline
        // equivalent; seals always depth-test now (visual-verify pass).
        PoseStack poseStack = new PoseStack();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        try {
            for (ISealEntity seal : seals.values()) {
                if (seal.getSeal() == null || seal.getSealPos() == null) continue;

                BlockPos pos = seal.getSealPos().pos;
                if (!player.level().isLoaded(pos)) continue;
                double distSq = cameraPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

                if (distSq <= MAX_RENDER_DIST_SQ) {
                    float alpha = 1.0f - (float) (distSq / MAX_RENDER_DIST_SQ);
                    boolean inactive = seal.isStoppedByRedstone(player.level());

                    if (inactive && player.getRandom().nextFloat() < partialTick / 12.0f) {
                        Direction face = seal.getSealPos().face;
                        FXDispatcher.INSTANCE.spark(
                                pos.getX() + 0.5f + face.getStepX() * 0.66f,
                                pos.getY() + 0.5f + face.getStepY() * 0.66f,
                                pos.getZ() + 0.5f + face.getStepZ() * 0.66f,
                                2.0f, 0.8f - player.getRandom().nextFloat() * 0.2f, 0.0f, 0.0f, 1.0f);
                    }

                    renderSeal(poseStack, collector, seal, alpha, inactive);

                    drawSealArea(poseStack, collector, player, seal, alpha, partialTick);
                }
            }
        } finally {
            // PoseStack is submit-local; nothing to restore.
        }
    }

    private static void renderSeal(PoseStack poseStack, SubmitNodeCollector collector, ISealEntity seal,
                                   float alpha, boolean inactive) {
        SealPos sealPos = seal.getSealPos();
        BlockPos pos = sealPos.pos;
        Direction face = sealPos.face;

        poseStack.pushPose();

        poseStack.translate(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        );

        applyFaceRotation(poseStack, face);

        poseStack.translate(0, 0, 0.55);

        poseStack.scale(0.5f, 0.5f, 0.5f);

        float brightness = inactive ? 0.5f : 1.0f;

        Player viewer = Minecraft.getInstance().player;
        float thickness = (viewer != null && viewer.isShiftKeyDown()) ? 0.0f : 0.1f;
        renderSealQuad(poseStack, collector, seal.getSeal().getSealIcon(), brightness, brightness, brightness, alpha,
                viewer != null ? viewer.tickCount : 0L, thickness);

        poseStack.popPose();
    }

    private static void drawSealArea(PoseStack poseStack, SubmitNodeCollector collector,
                                     Player player, ISealEntity seal, float alpha, float partialTick) {
        float r;
        float g;
        float b;
        if (seal.getColor() > 0) {
            // NEOFORGE_PORT: getTextureDiffuseColors() float[] -> getTextureDiffuseColor() int.
            int diffuse = DyeColor.byId(seal.getColor() - 1).getTextureDiffuseColor();
            r = ((diffuse >> 16) & 0xFF) / 255.0f;
            g = ((diffuse >> 8) & 0xFF) / 255.0f;
            b = (diffuse & 0xFF) / 255.0f;
        } else {
            float t = player.tickCount + partialTick;
            BlockPos sp = seal.getSealPos().pos;
            r = 0.7f + Mth.sin((t + sp.getX()) / 4.0f) * 0.1f;
            g = 0.7f + Mth.sin((t + sp.getY()) / 5.0f) * 0.1f;
            b = 0.7f + Mth.sin((t + sp.getZ()) / 6.0f) * 0.1f;
        }

        BlockPos pos = seal.getSealPos().pos;
        Direction face = seal.getSealPos().face;

        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        GolemRenderHooks.rotateDeg(poseStack, 90.0f,
                -face.getStepY(), face.getStepX(), -face.getStepZ());
        poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.51 : 0.51);
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(player.tickCount % 360 + partialTick));
        UtilsFX.renderQuadCentered(poseStack, collector, MIDDLE, 0.9f, r, g, b, 200, 771, alpha * 0.8f);
        poseStack.popPose();

        if (seal.getSeal() instanceof ISealConfigArea) {
            // Use the same box the working logic uses (GolemHelper.getBoundsForArea),
            // so the rendered frame always matches the actual seal area on every face.
            net.minecraft.world.phys.AABB areaBox =
                    arcaneresearch.api.golems.GolemHelper.getBoundsForArea(seal);
            double minX = areaBox.minX;
            double minY = areaBox.minY;
            double minZ = areaBox.minZ;
            double maxX = areaBox.maxX;
            double maxY = areaBox.maxY;
            double maxZ = areaBox.maxZ;
            double[][] locs = {
                    {minX, minY, minZ}, {minX, maxY - 1.0, minZ},
                    {maxX - 1.0, minY, minZ}, {maxX - 1.0, maxY - 1.0, minZ},
                    {maxX - 1.0, minY, maxZ - 1.0}, {maxX - 1.0, maxY - 1.0, maxZ - 1.0},
                    {minX, minY, maxZ - 1.0}, {minX, maxY - 1.0, maxZ - 1.0}
            };
            int q = 0;
            for (double[] loc : locs) {
                poseStack.pushPose();
                poseStack.translate(loc[0] + 0.5,
                        loc[1] + 0.5, loc[2] + 0.5);
                int w = 0;
                for (Direction cornerFace : ROT_FACES[q]) {
                    poseStack.pushPose();
                    GolemRenderHooks.rotateDeg(poseStack, 90.0f,
                            -cornerFace.getStepY(), cornerFace.getStepX(), -cornerFace.getStepZ());
                    poseStack.translate(0.0, 0.0, cornerFace.getStepZ() < 0 ? -0.49 : 0.49);
                    GolemRenderHooks.rotateDeg(poseStack, 90.0f, 0.0f, 0.0f, -1.0f);
                    GolemRenderHooks.rotateDeg(poseStack, ROT_MAT[q][w], 0.0f, 0.0f, 1.0f);
                    UtilsFX.renderQuadCentered(poseStack, collector, CFRAME, 1.0f,
                            r, g, b, 200, 771, alpha * 0.7f);
                    poseStack.popPose();
                    w++;
                }
                poseStack.popPose();
                q++;
            }
        }
    }

    private static void applyFaceRotation(PoseStack poseStack, Direction face) {
        switch (face) {
            case UP -> poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(-90));
            case DOWN -> poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(90));
            case NORTH -> poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180));
            case SOUTH -> {
            }
            case WEST -> poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(-90));
            case EAST -> poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(90));
        }
    }

    private static void renderSealQuad(PoseStack poseStack, SubmitNodeCollector collector, Identifier texture,
                                       float r, float g, float b, float a, long tick,
                                       float thickness) {
        Identifier actualTexture;
        if (texture == null) {
            actualTexture = DEFAULT_SEAL_TEXTURE;
        } else {

            String path = texture.getPath();
            if (!path.startsWith("textures/")) {
                path = "textures/" + path;
            }
            if (!path.endsWith(".png")) {
                path = path + ".png";
            }
            actualTexture = Identifier.fromNamespaceAndPath(texture.getNamespace(), path);
        }

        FrameStrip strip = framesFor(actualTexture);
        int step = (int) ((tick / strip.frameTime()) % strip.sequence().length);
        int frame = strip.sequence()[step] % strip.frames();
        float vTop = (float) frame / strip.frames();
        float vBottom = (float) (frame + 1) / strip.frames();

        RenderType type = TCRenderTypes.translucentPortal(actualTexture);
        float size = 0.5f;
        float zb = -thickness;
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            quad(consumer, pose, -size, -size, 0, 0, vBottom, r, g, b, a);
            quad(consumer, pose, size, -size, 0, 1, vBottom, r, g, b, a);
            quad(consumer, pose, size, size, 0, 1, vTop, r, g, b, a);
            quad(consumer, pose, -size, size, 0, 0, vTop, r, g, b, a);

            quad(consumer, pose, -size, size, zb, 1, vTop, r, g, b, a);
            quad(consumer, pose, size, size, zb, 0, vTop, r, g, b, a);
            quad(consumer, pose, size, -size, zb, 0, vBottom, r, g, b, a);
            quad(consumer, pose, -size, -size, zb, 1, vBottom, r, g, b, a);

            if (thickness > 0.0f) {
                for (int k = 0; k < 16; k++) {
                    float x0 = -size + 2.0f * size * k / 16.0f;
                    float x1 = -size + 2.0f * size * (k + 1) / 16.0f;
                    float u = (k + 0.5f) / 16.0f;
                    quad(consumer, pose, x0, -size, zb, u, vBottom, r, g, b, a);
                    quad(consumer, pose, x0, -size, 0, u, vBottom, r, g, b, a);
                    quad(consumer, pose, x0, size, 0, u, vTop, r, g, b, a);
                    quad(consumer, pose, x0, size, zb, u, vTop, r, g, b, a);
                    quad(consumer, pose, x1, size, zb, u, vTop, r, g, b, a);
                    quad(consumer, pose, x1, size, 0, u, vTop, r, g, b, a);
                    quad(consumer, pose, x1, -size, 0, u, vBottom, r, g, b, a);
                    quad(consumer, pose, x1, -size, zb, u, vBottom, r, g, b, a);
                }

                for (int k = 0; k < 16; k++) {
                    float y0 = -size + 2.0f * size * k / 16.0f;
                    float y1 = -size + 2.0f * size * (k + 1) / 16.0f;
                    float v = vTop + (vBottom - vTop) * (k + 0.5f) / 16.0f;
                    quad(consumer, pose, -size, y1, 0, 0, v, r, g, b, a);
                    quad(consumer, pose, size, y1, 0, 1, v, r, g, b, a);
                    quad(consumer, pose, size, y1, zb, 1, v, r, g, b, a);
                    quad(consumer, pose, -size, y1, zb, 0, v, r, g, b, a);
                    quad(consumer, pose, size, y0, 0, 1, v, r, g, b, a);
                    quad(consumer, pose, -size, y0, 0, 0, v, r, g, b, a);
                    quad(consumer, pose, -size, y0, zb, 0, v, r, g, b, a);
                    quad(consumer, pose, size, y0, zb, 1, v, r, g, b, a);
                }
            }
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v,
                             float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0)
                .setNormal(pose, 0, 0, 1);
    }

    private static FrameStrip framesFor(Identifier texture) {
        return FRAME_CACHE.computeIfAbsent(texture, SealRenderer::loadFrameStrip);
    }

    private static FrameStrip loadFrameStrip(Identifier texture) {
        int frames = 1;
        int frameTime = 2;
        int[] sequence = null;
        try {
            var resources = Minecraft.getInstance().getResourceManager();
            var image = resources.getResource(texture).orElse(null);
            if (image != null) {
                try (var in = image.open()) {
                    NativeImage img = NativeImage.read(in);
                    try {
                        if (img.getWidth() > 0 && img.getHeight() > img.getWidth()
                                && img.getHeight() % img.getWidth() == 0) {
                            frames = img.getHeight() / img.getWidth();
                        }
                    } finally {
                        img.close();
                    }
                }
            }
            var mcmeta = resources.getResource(
                    Identifier.fromNamespaceAndPath(texture.getNamespace(), texture.getPath() + ".mcmeta")).orElse(null);
            if (mcmeta != null) {
                try (var in = mcmeta.open()) {
                    String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    var root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                    if (root.has("animation") && root.get("animation").isJsonObject()) {
                        var anim = root.getAsJsonObject("animation");
                        if (anim.has("frametime")) {
                            frameTime = Math.max(1, anim.get("frametime").getAsInt());
                        }
                        if (anim.has("frames") && anim.get("frames").isJsonArray()) {
                            List<Integer> list = new ArrayList<>();
                            for (var el : anim.getAsJsonArray("frames")) {
                                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                                    int idx = el.getAsInt();
                                    if (idx >= 0 && idx < frames) {
                                        list.add(idx);
                                    }
                                }
                            }
                            if (!list.isEmpty()) {
                                sequence = list.stream().mapToInt(i -> i).toArray();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (sequence == null) {
            sequence = new int[frames];
            for (int i = 0; i < frames; i++) {
                sequence[i] = i;
            }
        }
        return new FrameStrip(frames, frameTime, sequence);
    }

    private record FrameStrip(int frames, int frameTime, int[] sequence) {
    }
}