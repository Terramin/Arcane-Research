package thaumictinker.client.gateway;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import org.joml.Quaternionf;

import thaumictinker.client.geom.SpinningCubesGeometry;
import thaumictinker.kami.gateway.WarpGateBlockEntity;

 




 
public class WarpGateRenderer implements BlockEntityRenderer<WarpGateBlockEntity> {

    private static final ResourceLocation WHITE = new ResourceLocation("thaumictinker", "textures/misc/white.png");
    private static final float SOLID_A = 1.0F;
    private static final float TRAIL_A = 0x33 / 255.0F;
    private static final int LIGHT = 15728880;

    public WarpGateRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WarpGateBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {
        double ticks = (entity.getLevel() == null ? 0L : entity.getLevel().getGameTime()) + partialTicks;
        poseStack.pushPose();
        poseStack.translate(0.5, 2.5, 0.5);
        
        poseStack.mulPose(new Quaternionf().rotationAxis((float) Math.PI,
                0.70710677F, 0.0F, 0.70710677F));
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(WHITE));
        PoseStack.Pose pose = poseStack.last();
        SpinningCubesGeometry.build(ticks,
                new Emitter(consumer, pose, SOLID_A, LIGHT),
                new Emitter(consumer, pose, TRAIL_A, LIGHT));
        poseStack.popPose();
    }

    private record Emitter(VertexConsumer consumer, PoseStack.Pose pose, float alpha, int light)
            implements SpinningCubesGeometry.ModelQuadSink {

        @Override
        public void vertex(float x, float y, float z, float u, float v) {
            consumer.vertex(pose.pose(), x, y, z);
            consumer.uv(u, v);
            consumer.color(1.0F, 1.0F, 1.0F, alpha);
            consumer.uv2(light);
            consumer.overlayCoords(0);
        }
    }
}
