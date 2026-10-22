package thaumictinker.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

 



 
public final class AttractorRendering {

    private static final ResourceLocation KINETIC_PULL = id("textures/model/magnets.png");
    private static final ResourceLocation KINETIC_PUSH = id("textures/model/magnetn.png");
    private static final ResourceLocation CORPOREAL_PULL = id("textures/model/mobmagnets.png");
    private static final ResourceLocation CORPOREAL_PUSH = id("textures/model/mobmagnetn.png");
    private static final ResourceLocation RING = id("textures/item/lightningring.png");
    private static final int RING_FRAMES = 16;
    private static final int WHITE = 0xFFFFFFFF;

    private AttractorRendering() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("thaumictinker", path);
    }

    private static ResourceLocation texture(boolean corporeal, boolean pull) {
        if (corporeal) {
            return pull ? CORPOREAL_PULL : CORPOREAL_PUSH;
        }
        return pull ? KINETIC_PULL : KINETIC_PUSH;
    }

    public static void submit(PoseStack poseStack, MultiBufferSource buffers, boolean corporeal, boolean pull,
                              int redstone, int light, long ticks, long millis) {
        
        VertexConsumer model = buffers.getBuffer(RenderType.entityCutout(texture(corporeal, pull)));
        float[] color = rgba(WHITE, light);
        AttractorGeometry.model(new AttractorGeometry.ModelSink() {
            @Override
            public void vertex(float x, float y, float z, float u, float v) {
                model.vertex(poseStack.last().pose(), x, y, z);
                model.uv(u, v);
                model.color(color[0], color[1], color[2], color[3]);
                model.uv2(light);
                model.overlayCoords(0);
            }
        });

        
        int frame = (int) Math.floorMod(ticks, (long) RING_FRAMES);
        int red = pull ? 0 : 255;
        int blue = pull ? 255 : 0;
        int colorPacked = (230 << 24) | (red << 16) | blue;
        float scale = redstone / 15.0F * 0.7F + (redstone == 0 ? 0.0F : 0.4F);
        VertexConsumer ring = buffers.getBuffer(RenderType.entityTranslucentEmissive(RING));
        float[] ringColor = rgba(colorPacked, 225);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate(0.0F, 0.0F, -0.6F);
        for (int index = 0; index < 3; index++) {
            for (int side = 0; side < 2; side++) {
                poseStack.scale(1.0F, -1.0F, 1.0F);
                poseStack.scale(scale, scale, scale);
                AttractorGeometry.ring(new AttractorGeometry.ModelSink() {
                    @Override
                    public void vertex(float x, float y, float z, float u, float v) {
                        ring.vertex(poseStack.last().pose(), x, y, z);
                        ring.uv(u, v);
                        ring.color(ringColor[0], ringColor[1], ringColor[2], ringColor[3]);
                        ring.uv2(225);
                        ring.overlayCoords(0);
                    }
                }, frame, RING_FRAMES);
            }
            poseStack.translate(0.0D, 0.0D, -((double) Math.cos(millis / 500.0F) + 1.0D) * 0.09D - 0.1D);
        }
        poseStack.popPose();
    }

      
    private static float[] rgba(int packed, int light) {
        int a = (packed >>> 24) & 0xFF;
        int r = (packed >>> 16) & 0xFF;
        int g = (packed >>> 8) & 0xFF;
        int b = packed & 0xFF;
        
        return new float[] {r / 255.0F, g / 255.0F, b / 255.0F, a / 255.0F};
    }
}
