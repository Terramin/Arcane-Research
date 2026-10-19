package arcaneresearch.client.renderers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
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
@OnlyIn(Dist.CLIENT)
public class SealRenderer {
    private static final ResourceLocation DEFAULT_SEAL_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/item/seals/seal_blank.png");
    private static final ResourceLocation MIDDLE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/seal_area.png");
    private static final ResourceLocation CFRAME =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/frame_corner.png");
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
    private static final Map<ResourceLocation, FrameStrip> FRAME_CACHE = new ConcurrentHashMap<>();
    public static void renderSeals(PoseStack poseStack, float partialTick, Player player) {
        if (player == null || player.level() == null) return;
        String dimKey = player.level().dimension().location().toString();
        ConcurrentHashMap<SealPos, SealEntity> seals = SealHandler.sealEntities.get(dimKey);
        if (seals == null || seals.isEmpty()) return;
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        boolean sneaking = player.isShiftKeyDown();
        if (sneaking) {
            RenderSystem.disableDepthTest();
        }
        poseStack.pushPose();
        try {
            for (ISealEntity seal : seals.values()) {
                if (seal.getSeal() == null || seal.getSealPos() == null) continue;
                BlockPos pos = seal.getSealPos().pos;
                if (!player.level().isLoaded(pos)) continue;
                double distSq = cameraPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (distSq <= MAX_RENDER_DIST_SQ) {
                    float alpha = 1.0f - (float) (distSq / MAX_RENDER_DIST_SQ);
                    boolean inactive = seal.isStoppedByRedstone(player.level());
                    if (inactive && player.level().random.nextFloat() < partialTick / 12.0f) {
                        Direction face = seal.getSealPos().face;
                        FXDispatcher.INSTANCE.spark(
                                pos.getX() + 0.5f + face.getStepX() * 0.66f,
                                pos.getY() + 0.5f + face.getStepY() * 0.66f,
                                pos.getZ() + 0.5f + face.getStepZ() * 0.66f,
                                2.0f, 0.8f - player.level().random.nextFloat() * 0.2f, 0.0f, 0.0f, 1.0f);
                    }
                    renderSeal(poseStack, seal, cameraPos, alpha, inactive);
                    drawSealArea(poseStack, buffers, player, seal, alpha, partialTick);
                }
            }
            buffers.endBatch(TCRenderTypes.translucentPortal(MIDDLE));
            RenderSystem.depthMask(false);
            buffers.endBatch(TCRenderTypes.translucentPortal(CFRAME));
        } finally {
            poseStack.popPose();
            RenderSystem.depthMask(true);
            if (sneaking) {
                RenderSystem.enableDepthTest();
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }
    private static void renderSeal(PoseStack poseStack, ISealEntity seal, Vec3 cameraPos,
                                   float alpha, boolean inactive) {
        SealPos sealPos = seal.getSealPos();
        BlockPos pos = sealPos.pos;
        Direction face = sealPos.face;
        poseStack.pushPose();
        poseStack.translate(
                pos.getX() + 0.5 - cameraPos.x,
                pos.getY() + 0.5 - cameraPos.y,
                pos.getZ() + 0.5 - cameraPos.z
        );
        applyFaceRotation(poseStack, face);
        poseStack.translate(0, 0, 0.55);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        float brightness = inactive ? 0.5f : 1.0f;
        Player viewer = Minecraft.getInstance().player;
        float thickness = (viewer != null && viewer.isShiftKeyDown()) ? 0.0f : 0.1f;
        renderSealQuad(poseStack, seal.getSeal().getSealIcon(), brightness, brightness, brightness, alpha,
                viewer != null ? viewer.tickCount : 0L, thickness);
        poseStack.popPose();
    }
    private static void drawSealArea(PoseStack poseStack, MultiBufferSource buffers,
                                     Player player, ISealEntity seal, float alpha, float partialTick) {
        float r;
        float g;
        float b;
        if (seal.getColor() > 0) {
            float[] diffuse = DyeColor.byId(seal.getColor() - 1).getTextureDiffuseColors();
            r = diffuse[0];
            g = diffuse[1];
            b = diffuse[2];
        } else {
            float t = player.tickCount + partialTick;
            BlockPos sp = seal.getSealPos().pos;
            r = 0.7f + Mth.sin((t + sp.getX()) / 4.0f) * 0.1f;
            g = 0.7f + Mth.sin((t + sp.getY()) / 5.0f) * 0.1f;
            b = 0.7f + Mth.sin((t + sp.getZ()) / 6.0f) * 0.1f;
        }
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos pos = seal.getSealPos().pos;
        Direction face = seal.getSealPos().face;
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5 - cameraPos.x,
                pos.getY() + 0.5 - cameraPos.y, pos.getZ() + 0.5 - cameraPos.z);
        GolemRenderHooks.rotateDeg(poseStack, 90.0f,
                -face.getStepY(), face.getStepX(), -face.getStepZ());
        poseStack.translate(0.0, 0.0, face.getStepZ() < 0 ? -0.51 : 0.51);
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(player.tickCount % 360 + partialTick));
        UtilsFX.renderQuadCentered(poseStack, buffers, MIDDLE, 0.9f, r, g, b, 200, 771, alpha * 0.8f);
        poseStack.popPose();
        if (seal.getSeal() instanceof ISealConfigArea) {
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
                poseStack.translate(loc[0] + 0.5 - cameraPos.x,
                        loc[1] + 0.5 - cameraPos.y, loc[2] + 0.5 - cameraPos.z);
                int w = 0;
                for (Direction cornerFace : ROT_FACES[q]) {
                    poseStack.pushPose();
                    GolemRenderHooks.rotateDeg(poseStack, 90.0f,
                            -cornerFace.getStepY(), cornerFace.getStepX(), -cornerFace.getStepZ());
                    poseStack.translate(0.0, 0.0, cornerFace.getStepZ() < 0 ? -0.49 : 0.49);
                    GolemRenderHooks.rotateDeg(poseStack, 90.0f, 0.0f, 0.0f, -1.0f);
                    GolemRenderHooks.rotateDeg(poseStack, ROT_MAT[q][w], 0.0f, 0.0f, 1.0f);
                    UtilsFX.renderQuadCentered(poseStack, buffers, CFRAME, 1.0f,
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
            case UP -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));
            case DOWN -> poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));
            case NORTH -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));
            case SOUTH -> {
            }
            case WEST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
            case EAST -> poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        }
    }
    private static void renderSealQuad(PoseStack poseStack, ResourceLocation texture,
                                       float r, float g, float b, float a, long tick,
                                       float thickness) {
        ResourceLocation actualTexture;
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
            actualTexture = new ResourceLocation(texture.getNamespace(), path);
        }
        FrameStrip strip = framesFor(actualTexture);
        int step = (int) ((tick / strip.frameTime()) % strip.sequence().length);
        int frame = strip.sequence()[step] % strip.frames();
        float vTop = (float) frame / strip.frames();
        float vBottom = (float) (frame + 1) / strip.frames();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, actualTexture);
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float size = 0.5f;
        float zb = -thickness;
        buffer.vertex(matrix, -size, -size, 0).uv(0, vBottom).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, size, -size, 0).uv(1, vBottom).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, size, size, 0).uv(1, vTop).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, -size, size, 0).uv(0, vTop).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, -size, size, zb).uv(1, vTop).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, size, size, zb).uv(0, vTop).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, size, -size, zb).uv(0, vBottom).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, -size, -size, zb).uv(1, vBottom).color(r, g, b, a).endVertex();
        if (thickness > 0.0f) {
            for (int k = 0; k < 16; k++) {
                float x0 = -size + 2.0f * size * k / 16.0f;
                float x1 = -size + 2.0f * size * (k + 1) / 16.0f;
                float u = (k + 0.5f) / 16.0f;
                buffer.vertex(matrix, x0, -size, zb).uv(u, vBottom).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x0, -size, 0).uv(u, vBottom).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x0, size, 0).uv(u, vTop).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x0, size, zb).uv(u, vTop).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, size, zb).uv(u, vTop).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, size, 0).uv(u, vTop).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, -size, 0).uv(u, vBottom).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, -size, zb).uv(u, vBottom).color(r, g, b, a).endVertex();
            }
            for (int k = 0; k < 16; k++) {
                float y0 = -size + 2.0f * size * k / 16.0f;
                float y1 = -size + 2.0f * size * (k + 1) / 16.0f;
                float v = vTop + (vBottom - vTop) * (k + 0.5f) / 16.0f;
                buffer.vertex(matrix, -size, y1, 0).uv(0, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, size, y1, 0).uv(1, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, size, y1, zb).uv(1, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, -size, y1, zb).uv(0, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, size, y0, 0).uv(1, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, -size, y0, 0).uv(0, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, -size, y0, zb).uv(0, v).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, size, y0, zb).uv(1, v).color(r, g, b, a).endVertex();
            }
        }
        tesselator.end();
    }
    private static FrameStrip framesFor(ResourceLocation texture) {
        return FRAME_CACHE.computeIfAbsent(texture, SealRenderer::loadFrameStrip);
    }
    private static FrameStrip loadFrameStrip(ResourceLocation texture) {
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
                    new ResourceLocation(texture.getNamespace(), texture.getPath() + ".mcmeta")).orElse(null);
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
