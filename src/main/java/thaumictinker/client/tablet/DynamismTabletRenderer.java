package thaumictinker.client.tablet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import thaumictinker.tablet.DynamismTabletBlockEntity;

 



 
public class DynamismTabletRenderer implements BlockEntityRenderer<DynamismTabletBlockEntity> {

    private static final ResourceLocation CENTER = id("textures/misc/atcenter.png");
    private static final ResourceLocation LEFT = id("textures/misc/atleft.png");
    private static final ResourceLocation RIGHT = id("textures/misc/atright.png");
    private static final ResourceLocation INDENT = id("textures/misc/atfacingindent.png");
    private static final int LIGHT = 15728880;

    public DynamismTabletRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("thaumictinker", path);
    }

    private static int rotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 270;
            case SOUTH -> 90;
            case WEST -> 0;
            default -> 180;
        };
    }

    private static float[] translation(Direction facing) {
        return switch (facing) {
            case NORTH -> new float[] {0.0F, 0.0F, -1.0F};
            case SOUTH -> new float[] {-1.0F, 0.0F, 0.0F};
            case WEST -> new float[] {0.0F, 0.0F, 0.0F};
            default -> new float[] {-1.0F, 0.0F, -1.0F};
        };
    }

    @Override
    public void render(DynamismTabletBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {
        if (entity.getLevel() == null) {
            return;
        }
        ItemStack stack = entity.heldStack();
        boolean leftClick = entity.isLeftClick();
        int rotation = rotation(entity.facing());
        double ticks = entity.ticksExisted() + partialTicks;
        float swing = Math.max(0.0F, entity.swingProgress() + entity.swingMod() * partialTicks);
        long millis = System.currentTimeMillis();

        overlay(poseStack, bufferSource, ticks, CENTER, -1, 0.65D, 0.13F, 0.0F);
        overlay(poseStack, bufferSource, ticks, leftClick ? LEFT : RIGHT, 1, 1.0D, leftClick ? 0.13F : 0.131F, 0.0F);
        overlay(poseStack, bufferSource, ticks, INDENT, 0, 0.5D, 0.13F, rotation + 90.0F);
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(0.1D, 0.2D + Math.cos(millis / 600.0D) / 18.0D, 0.5D);
        float[] translation = translation(entity.facing());
        poseStack.translate(translation[0], translation[1], translation[2]);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(swing));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        poseStack.translate(-swing / 250.0F, swing / 1000.0F, 0.0F);
        poseStack.mulPose(new org.joml.Quaternionf().rotateAxis(
                (float) Math.toRadians(Math.cos(millis / 400.0F) * 5.0F), 0.70710677F, 0.0F, 0.70710677F));
        poseStack.translate(0.5F, 0.55F, 0.0F);
        float scale = stack.getItem() instanceof BlockItem ? 2.5F : 1.5F;
        poseStack.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, entity.getLevel(), (int) entity.getBlockPos().asLong());
        poseStack.popPose();
    }

    private void overlay(PoseStack poseStack, MultiBufferSource buffers, double ticks, ResourceLocation texture,
                         int rotationMod, double size, float height, float forced) {
        poseStack.pushPose();
        poseStack.translate(0.5F, height, 0.5F);
        float degrees = rotationMod == 0 ? forced : (float) (ticks * rotationMod % 360.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityTranslucentEmissive(texture));
        float[] color = {1.0F, 1.0F, 1.0F, 1.0F};
        TabletGeometry.overlay(new TabletSink() {
            @Override
            public void vertex(float x, float y, float z, float u, float v) {
                consumer.vertex(poseStack.last().pose(), x, y, z);
                consumer.uv(u, v);
                consumer.color(color[0], color[1], color[2], color[3]);
                consumer.uv2(LIGHT);
                consumer.overlayCoords(OverlayTexture.NO_OVERLAY);
            }
        }, size);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(DynamismTabletBlockEntity entity) {
        return true;
    }
}
