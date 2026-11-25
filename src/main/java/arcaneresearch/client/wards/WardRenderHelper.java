package arcaneresearch.client.wards;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.util.function.Predicate;

/** Shared connected-texture rendering for warded blocks and architect preview. */
public final class WardRenderHelper {
    private WardRenderHelper() {}

    private static final int[] CONNECTED = new int[] { 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11 };

    private static ResourceLocation glass(int index) {
        return ResourceLocation.fromNamespaceAndPath("arcaneresearch",
                "textures/block/warded_glass_" + (index + 1) + ".png");
    }

    public static int textureId(boolean[] bits) {
        int id = 0;
        for (int i = 0; i < 8; i++) {
            if (bits[i]) {
                id += 1 << i;
            }
        }
        return id >= 0 && id < CONNECTED.length ? CONNECTED[id] : 0;
    }

    /** Connected neighbours around a face, using the original TC4 bit layout. */
    public static boolean[] connectionBits(BlockPos pos, Direction face, Predicate<BlockPos> connected) {
        boolean[] b = new boolean[8];
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (face == Direction.DOWN || face == Direction.UP) {
            b[0] = connected.test(new BlockPos(x - 1, y, z - 1));
            b[1] = connected.test(new BlockPos(x, y, z - 1));
            b[2] = connected.test(new BlockPos(x + 1, y, z - 1));
            b[3] = connected.test(new BlockPos(x - 1, y, z));
            b[4] = connected.test(new BlockPos(x + 1, y, z));
            b[5] = connected.test(new BlockPos(x - 1, y, z + 1));
            b[6] = connected.test(new BlockPos(x, y, z + 1));
            b[7] = connected.test(new BlockPos(x + 1, y, z + 1));
        } else if (face == Direction.NORTH || face == Direction.SOUTH) {
            int dx = face == Direction.NORTH ? 1 : -1;
            b[0] = connected.test(new BlockPos(x + dx, y + 1, z));
            b[1] = connected.test(new BlockPos(x, y + 1, z));
            b[2] = connected.test(new BlockPos(x - dx, y + 1, z));
            b[3] = connected.test(new BlockPos(x + dx, y, z));
            b[4] = connected.test(new BlockPos(x - dx, y, z));
            b[5] = connected.test(new BlockPos(x + dx, y - 1, z));
            b[6] = connected.test(new BlockPos(x, y - 1, z));
            b[7] = connected.test(new BlockPos(x - dx, y - 1, z));
        } else {
            int dz = face == Direction.EAST ? 1 : -1;
            b[0] = connected.test(new BlockPos(x, y + 1, z + dz));
            b[1] = connected.test(new BlockPos(x, y + 1, z));
            b[2] = connected.test(new BlockPos(x, y + 1, z - dz));
            b[3] = connected.test(new BlockPos(x, y, z + dz));
            b[4] = connected.test(new BlockPos(x, y, z - dz));
            b[5] = connected.test(new BlockPos(x, y - 1, z + dz));
            b[6] = connected.test(new BlockPos(x, y - 1, z));
            b[7] = connected.test(new BlockPos(x, y - 1, z - dz));
        }
        return b;
    }

    /**
     * Quad vertices (x, y, z, u, v). The UVs follow TC4's connected-texture
     * layout: uv(0,0) is bit0 of {@link #connectionBits}, uv(1,0) is bit2,
     * uv(1,1) is bit7 and uv(0,1) is bit5. Getting this wrong makes horizontal
     * edges connect but vertical edges/corners stay broken.
     */
    private static final float[][][] FACES = {
        {{0,0,0,0,0},{0,0,1,0,1},{1,0,1,1,1},{1,0,0,1,0}},       // down
        {{0,1,0,0,0},{1,1,0,1,0},{1,1,1,1,1},{0,1,1,0,1}},       // up
        {{1,1,0,0,0},{0,1,0,1,0},{0,0,0,1,1},{1,0,0,0,1}},       // north
        {{0,1,1,0,0},{1,1,1,1,0},{1,0,1,1,1},{0,0,1,0,1}},       // south
        {{0,1,0,0,0},{0,1,1,1,0},{0,0,1,1,1},{0,0,0,0,1}},       // west
        {{1,1,1,0,0},{1,1,0,1,0},{1,0,0,1,1},{1,0,1,0,1}}        // east
    };

    /** Draws one block as a translucent connected-glass cage. */
    public static void renderBlock(PoseStack pose, BlockPos pos, Predicate<BlockPos> connected,
                                   float r, float g, float b, float alpha, Vec3 camera) {
        pose.pushPose();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix = pose.last().pose();
        for (Direction face : Direction.values()) {
            if (connected.test(pos.relative(face))) {
                continue;
            }
            ResourceLocation tex = glass(textureId(connectionBits(pos, face, connected)));
            RenderSystem.setShaderTexture(0, tex);
            RenderSystem.setShaderColor(r, g, b, alpha);
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            for (float[] v : FACES[face.ordinal()]) {
                builder.vertex(matrix, v[0], v[1], v[2]).uv(v[3], v[4]).endVertex();
            }
            BufferUploader.drawWithShader(builder.end());
        }
        pose.popPose();
    }

    /** Textured crossed axis arrows, as in TC4 drawArchitectAxis. */
    public static void renderAxis(PoseStack pose, BlockPos pos, Vec3 camera,
                                  boolean dx, boolean dy, boolean dz, float partialTick) {
        if (!dx && !dy && !dz) return;
        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        float tick = player != null ? player.tickCount + partialTick : 0F;
        float r = (float) Math.sin(tick / 4F + pos.getX()) * 0.2F + 0.3F;
        float g = (float) Math.sin(tick / 3F + pos.getY()) * 0.2F + 0.3F;
        float b = (float) Math.sin(tick / 2F + pos.getZ()) * 0.2F + 0.8F;
        ResourceLocation arrows = ResourceLocation.fromNamespaceAndPath("arcaneresearch",
                "textures/misc/architect_arrows.png");
        pose.pushPose();
        pose.translate(pos.getX() + 0.5 - camera.x, pos.getY() + 0.5 - camera.y,
                pos.getZ() + 0.5 - camera.z);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, arrows);
        RenderSystem.setShaderColor(1F, 1F, 1F, 0.33F);
        Matrix4f matrix = pose.last().pose();
        if (dx) {
            drawArrow(matrix, 1F, 0F, 0F, r, g, b);
            drawArrow(matrix, -1F, 0F, 0F, r, g, b);
        }
        if (dy) {
            drawArrow(matrix, 0F, 1F, 0F, r, g, b);
            drawArrow(matrix, 0F, -1F, 0F, r, g, b);
        }
        if (dz) {
            drawArrow(matrix, 0F, 0F, 1F, r, g, b);
            drawArrow(matrix, 0F, 0F, -1F, r, g, b);
        }
        pose.popPose();
    }

    private static void drawArrow(Matrix4f matrix, float nx, float ny, float nz,
                                  float r, float g, float b) {
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        RenderSystem.setShaderColor(r, g, b, 0.33F);
        // two crossed quads along the axis, centred on the block
        float s = 0.5F;
        if (nx != 0) {
            builder.vertex(matrix, nx * s, -s, 0).uv(0, 0).endVertex();
            builder.vertex(matrix, nx * s, s, 0).uv(1, 0).endVertex();
            builder.vertex(matrix, -nx * s, s, 0).uv(1, 1).endVertex();
            builder.vertex(matrix, -nx * s, -s, 0).uv(0, 1).endVertex();
            builder.vertex(matrix, nx * s, 0, -s).uv(0, 0).endVertex();
            builder.vertex(matrix, nx * s, 0, s).uv(1, 0).endVertex();
            builder.vertex(matrix, -nx * s, 0, s).uv(1, 1).endVertex();
            builder.vertex(matrix, -nx * s, 0, -s).uv(0, 1).endVertex();
        } else if (ny != 0) {
            builder.vertex(matrix, -s, ny * s, 0).uv(0, 0).endVertex();
            builder.vertex(matrix, s, ny * s, 0).uv(1, 0).endVertex();
            builder.vertex(matrix, s, -ny * s, 0).uv(1, 1).endVertex();
            builder.vertex(matrix, -s, -ny * s, 0).uv(0, 1).endVertex();
            builder.vertex(matrix, 0, ny * s, -s).uv(0, 0).endVertex();
            builder.vertex(matrix, 0, ny * s, s).uv(1, 0).endVertex();
            builder.vertex(matrix, 0, -ny * s, s).uv(1, 1).endVertex();
            builder.vertex(matrix, 0, -ny * s, -s).uv(0, 1).endVertex();
        } else {
            builder.vertex(matrix, -s, 0, nz * s).uv(0, 0).endVertex();
            builder.vertex(matrix, s, 0, nz * s).uv(1, 0).endVertex();
            builder.vertex(matrix, s, 0, -nz * s).uv(1, 1).endVertex();
            builder.vertex(matrix, -s, 0, -nz * s).uv(0, 1).endVertex();
            builder.vertex(matrix, 0, -s, nz * s).uv(0, 0).endVertex();
            builder.vertex(matrix, 0, s, nz * s).uv(1, 0).endVertex();
            builder.vertex(matrix, 0, s, -nz * s).uv(1, 1).endVertex();
            builder.vertex(matrix, 0, -s, -nz * s).uv(0, 1).endVertex();
        }
        BufferUploader.drawWithShader(builder.end());
    }
}
