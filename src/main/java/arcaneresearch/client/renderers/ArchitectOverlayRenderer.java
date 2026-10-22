package arcaneresearch.client.renderers;


import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.IArchitect;

import java.util.ArrayList;




public class ArchitectOverlayRenderer {

    private static final Identifier CFRAME =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/frame_corner.png");
    private static final Identifier SFRAME =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/frame_side.png");

    private static final int[][] MOS = {
            {4, 5, 6, 7}, {0, 1, 2, 3}, {0, 1, 4, 5},
            {2, 3, 6, 7}, {0, 2, 4, 6}, {1, 3, 5, 7}};

    private static final int[][] ROTMAT = {
            {0, 90, 270, 180}, {270, 180, 0, 90}, {180, 90, 270, 0},
            {0, 270, 90, 180}, {270, 180, 0, 90}, {180, 270, 90, 0}};

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onSubmitGeometry(
                net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) {
                return;
            }
            ItemStack archStack = getHeldArchitectStack(player);
            if (archStack.isEmpty()) {
                return;
            }
            float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            renderArchitect(event.getSubmitNodeCollector(), player, archStack, partialTick);
        }
    }

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
        return blocks != null && !blocks.isEmpty();
    }

    private static boolean renderArchitect(net.minecraft.client.renderer.SubmitNodeCollector collector,
                                           Player player, ItemStack stack, float partialTick) {
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
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        drawArchitectAxis(poseStack, collector, player, target.getBlockPos(), partialTick,
                architect.showAxis(stack, level, player, target.getDirection(), IArchitect.EnumAxis.X),
                architect.showAxis(stack, level, player, target.getDirection(), IArchitect.EnumAxis.Y),
                architect.showAxis(stack, level, player, target.getDirection(), IArchitect.EnumAxis.Z));
        for (BlockPos pos : blocks) {
            drawOverlayBlock(poseStack, collector, pos, blocks);
        }
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

    private static void drawOverlayBlock(PoseStack poseStack,
                                         net.minecraft.client.renderer.SubmitNodeCollector collector,
                                         BlockPos pos,
                                         ArrayList<BlockPos> blocks) {
        boolean[] bitMatrix = getConnectedSides(blocks, pos);
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5D,
                pos.getY() + 0.5D, pos.getZ() + 0.5D);
        for (Direction face : Direction.values()) {
            if (isConnectedBlock(blocks, pos.relative(face))) {
                continue;
            }
            poseStack.pushPose();

            org.joml.Vector3f axis = new org.joml.Vector3f(
                    -face.getStepY(), face.getStepX(), -face.getStepZ());
            poseStack.rotate(Axis.of(axis).rotationDegrees(90.0F));
            if (face.getStepZ() < 0) {
                poseStack.translate(0.0D, 0.0D, -0.5D);
            } else {
                poseStack.translate(0.0D, 0.0D, 0.5D);
            }

            poseStack.rotate(Axis.ZN.rotationDegrees(90));
            drawTexturedQuad(poseStack, collector, SFRAME, 0.1F);
            for (int a = 0; a < 4; a++) {
                if (bitMatrix[MOS[face.ordinal()][a]]) {
                    poseStack.pushPose();
                    poseStack.rotate(Axis.ZP.rotationDegrees(ROTMAT[face.ordinal()][a]));
                    drawTexturedQuad(poseStack, collector, CFRAME, 0.66F);
                    poseStack.popPose();
                }
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void drawTexturedQuad(PoseStack poseStack,
                                         net.minecraft.client.renderer.SubmitNodeCollector collector,
                                         Identifier texture, float alpha) {
        
        
        
        net.minecraft.client.renderer.rendertype.RenderType type =
                TCRenderTypes.additiveTexturedNoCullNoDepth(texture);
        int tint = net.minecraft.util.ARGB.colorFromFloat(alpha, 1, 1, 1);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            
            
            consumer.addVertex(pose, -0.5F, 0.5F, 0).setUv(1, 1).setColor(tint)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, 0.5F, 0.5F, 0).setUv(1, 0).setColor(tint)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, 0.5F, -0.5F, 0).setUv(0, 0).setColor(tint)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, -0.5F, -0.5F, 0).setUv(0, 1).setColor(tint)
                    .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0, 0, 1);
        });
    }

    private static void drawArchitectAxis(PoseStack poseStack,
                                          net.minecraft.client.renderer.SubmitNodeCollector collector,
                                          Player player, BlockPos pos,
                                          float partialTick, boolean dx, boolean dy, boolean dz) {
        if (!dx && !dy && !dz) {
            return;
        }
        float r = (float) (0.3 + 0.2 * Math.sin(player.tickCount / 4f + pos.getX()));
        float g = (float) (0.3 + 0.2 * Math.sin(player.tickCount / 3f + pos.getY()));
        float b = (float) (0.8 + 0.2 * Math.sin(player.tickCount / 2f + pos.getZ()));
        int color = net.minecraft.util.ARGB.colorFromFloat(0.6f, r, g, b);
        
        
        poseStack.pushPose();
        poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5);
        net.minecraft.client.renderer.rendertype.RenderType lineType =
                net.minecraft.client.renderer.rendertype.RenderTypes.lines();
        if (dx) {
            collector.submitShapeOutline(poseStack,
                    net.minecraft.world.phys.shapes.Shapes.create(-6, 0.48, 0.48, 7, 0.52, 0.52),
                    lineType, color, 1.0f, false);
        }
        if (dy) {
            collector.submitShapeOutline(poseStack,
                    net.minecraft.world.phys.shapes.Shapes.create(0.48, -6, 0.48, 0.52, 7, 0.52),
                    lineType, color, 1.0f, false);
        }
        if (dz) {
            collector.submitShapeOutline(poseStack,
                    net.minecraft.world.phys.shapes.Shapes.create(0.48, 0.48, -6, 0.52, 0.52, 7),
                    lineType, color, 1.0f, false);
        }
        poseStack.popPose();
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