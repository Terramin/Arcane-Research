package arcaneresearch.client.renderers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.api.items.IArchitect;
import java.util.ArrayList;
@OnlyIn(Dist.CLIENT)
public class ArchitectOverlayRenderer {
    private static final ResourceLocation CFRAME =
            new ResourceLocation("arcaneresearch", "textures/misc/frame_corner.png");
    private static final ResourceLocation SFRAME =
            new ResourceLocation("arcaneresearch", "textures/misc/frame_side.png");
    private static final int[][] MOS = {
            {4, 5, 6, 7}, {0, 1, 2, 3}, {0, 1, 4, 5},
            {2, 3, 6, 7}, {0, 2, 4, 6}, {1, 3, 5, 7}};
    private static final int[][] ROTMAT = {
            {0, 90, 270, 180}, {270, 180, 0, 90}, {180, 90, 270, 0},
            {0, 270, 90, 180}, {270, 180, 0, 90}, {180, 270, 90, 0}};
    public static boolean renderArchitect(PoseStack poseStack, Player player,
                                          ItemStack stack, float partialTick) {
        if (!(stack.getItem() instanceof IArchitect architect)) {
            return false;
        }
        Level level = player.level();
        BlockHitResult target = architect.getArchitectMOP(stack, level, player);
        if (target == null || target.getBlockPos() == null) {
            return false;
        }
        ArrayList<BlockPos> blocks = architect.getArchitectBlocks(stack, level,
                target.getBlockPos(), target.getDirection(), player);
        if (blocks == null || blocks.isEmpty()) {
            return false;
        }
        drawArchitectAxis(poseStack, player, target.getBlockPos(), partialTick,
                architect.showAxis(stack, level, player, target.getDirection(), IArchitect.EnumAxis.X),
                architect.showAxis(stack, level, player, target.getDirection(), IArchitect.EnumAxis.Y),
                architect.showAxis(stack, level, player, target.getDirection(), IArchitect.EnumAxis.Z));
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        for (BlockPos pos : blocks) {
            drawOverlayBlock(poseStack, pos, blocks, cameraPos);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        return true;
    }
    private static boolean isConnectedBlock(ArrayList<BlockPos> blocks, BlockPos pos) {
        return blocks.contains(pos);
    }
    private static boolean[] getConnectedSides(ArrayList<BlockPos> blocks, BlockPos pos) {
        boolean[] bitMatrix = new boolean[8];
        bitMatrix[0] = !isConnectedBlock(blocks, pos.offset(-1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, -1))
                && !isConnectedBlock(blocks, pos.offset(0, 1, 0));
        bitMatrix[1] = !isConnectedBlock(blocks, pos.offset(1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, -1))
                && !isConnectedBlock(blocks, pos.offset(0, 1, 0));
        bitMatrix[2] = !isConnectedBlock(blocks, pos.offset(-1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, 1))
                && !isConnectedBlock(blocks, pos.offset(0, 1, 0));
        bitMatrix[3] = !isConnectedBlock(blocks, pos.offset(1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, 1))
                && !isConnectedBlock(blocks, pos.offset(0, 1, 0));
        bitMatrix[4] = !isConnectedBlock(blocks, pos.offset(-1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, -1))
                && !isConnectedBlock(blocks, pos.offset(0, -1, 0));
        bitMatrix[5] = !isConnectedBlock(blocks, pos.offset(1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, -1))
                && !isConnectedBlock(blocks, pos.offset(0, -1, 0));
        bitMatrix[6] = !isConnectedBlock(blocks, pos.offset(-1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, 1))
                && !isConnectedBlock(blocks, pos.offset(0, -1, 0));
        bitMatrix[7] = !isConnectedBlock(blocks, pos.offset(1, 0, 0))
                && !isConnectedBlock(blocks, pos.offset(0, 0, 1))
                && !isConnectedBlock(blocks, pos.offset(0, -1, 0));
        return bitMatrix;
    }
    private static void drawOverlayBlock(PoseStack poseStack, BlockPos pos,
                                         ArrayList<BlockPos> blocks, Vec3 cameraPos) {
        boolean[] bitMatrix = getConnectedSides(blocks, pos);
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5D - cameraPos.x,
                pos.getY() + 0.5D - cameraPos.y, pos.getZ() + 0.5D - cameraPos.z);
        for (Direction face : Direction.values()) {
            if (isConnectedBlock(blocks, pos.relative(face))) {
                continue;
            }
            poseStack.pushPose();
            org.joml.Vector3f axis = new org.joml.Vector3f(
                    -face.getStepY(), face.getStepX(), -face.getStepZ());
            poseStack.mulPose(Axis.of(axis).rotationDegrees(90.0F));
            if (face.getStepZ() < 0) {
                poseStack.translate(0.0D, 0.0D, -0.5D);
            } else {
                poseStack.translate(0.0D, 0.0D, 0.5D);
            }
            poseStack.mulPose(Axis.ZN.rotationDegrees(90));
            drawTexturedQuad(poseStack, SFRAME, 0.1F);
            for (int a = 0; a < 4; a++) {
                if (bitMatrix[MOS[face.ordinal()][a]]) {
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.ZP.rotationDegrees(ROTMAT[face.ordinal()][a]));
                    drawTexturedQuad(poseStack, CFRAME, 0.66F);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    private static void drawTexturedQuad(PoseStack poseStack, ResourceLocation texture, float alpha) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.vertex(matrix, -0.5F, 0.5F, 0).uv(1, 1).endVertex();
        builder.vertex(matrix, 0.5F, 0.5F, 0).uv(1, 0).endVertex();
        builder.vertex(matrix, 0.5F, -0.5F, 0).uv(0, 0).endVertex();
        builder.vertex(matrix, -0.5F, -0.5F, 0).uv(0, 1).endVertex();
        BufferUploader.drawWithShader(builder.end());
    }
    private static void drawArchitectAxis(PoseStack poseStack, Player player, BlockPos pos,
                                          float partialTick, boolean dx, boolean dy, boolean dz) {
        if (!dx && !dy && !dz) {
            return;
        }
        float r = (float) (0.3 + 0.2 * Math.sin(player.tickCount / 4f + pos.getX()));
        float g = (float) (0.3 + 0.2 * Math.sin(player.tickCount / 3f + pos.getY()));
        float b = (float) (0.8 + 0.2 * Math.sin(player.tickCount / 2f + pos.getZ()));
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var buffer = bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.lines());
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5 - cameraPos.x, pos.getY() + 0.5 - cameraPos.y,
                pos.getZ() + 0.5 - cameraPos.z);
        if (dx) {
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(poseStack, buffer,
                    -6, 0.48, 0.48, 7, 0.52, 0.52, r, g, b, 0.6f);
        }
        if (dy) {
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(poseStack, buffer,
                    0.48, -6, 0.48, 0.52, 7, 0.52, r, g, b, 0.6f);
        }
        if (dz) {
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(poseStack, buffer,
                    0.48, 0.48, -6, 0.52, 0.52, 7, r, g, b, 0.6f);
        }
        poseStack.popPose();
        bufferSource.endBatch(net.minecraft.client.renderer.RenderType.lines());
    }
    public static ItemStack getHeldArchitectStack(Player player) {
        ItemStack main = player.getMainHandItem();
        if (!main.isEmpty() && main.getItem() instanceof IArchitect) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && off.getItem() instanceof IArchitect) {
            return off;
        }
        return ItemStack.EMPTY;
    }
    public static Direction fallbackSide(BlockHitResult target) {
        return target != null ? target.getDirection() : Direction.UP;
    }
}
