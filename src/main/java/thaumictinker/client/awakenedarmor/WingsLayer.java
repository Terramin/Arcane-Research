package thaumictinker.client.awakenedarmor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import thaumictinker.awakenedarmor.AwakenedArmorFeature;
import thaumictinker.client.geom.WingsGeometry;

 







 
public class WingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("thaumictinker", "textures/model/wings.png");
    private static final int WHITE = 0xFFFFFFFF;

    public WingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float netHeadYaw, float headPitch,
                       float modelScale) {
        if (player.isInvisible() || !player.getItemBySlot(EquipmentSlot.CHEST).is(AwakenedArmorFeature.ROBES)) {
            return;
        }
        boolean flying = player.getAbilities().flying;
        float ageInTicks = player.tickCount + partialTick;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);
        wing(consumer, poseStack, packedLight, WingsGeometry.LEFT_PIVOT_X,
                WingsGeometry.leftAngle(flying, ageInTicks), true);
        wing(consumer, poseStack, packedLight, WingsGeometry.RIGHT_PIVOT_X,
                WingsGeometry.rightAngle(flying, ageInTicks), false);
        poseStack.popPose();
    }

    private static void wing(VertexConsumer consumer, PoseStack poseStack, int light, float pivotX, float angle,
                              boolean left) {
        poseStack.pushPose();
        poseStack.translate(pivotX * WingsGeometry.PIXEL, WingsGeometry.PIVOT_Y * WingsGeometry.PIXEL,
                WingsGeometry.PIVOT_Z * WingsGeometry.PIXEL);
        poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(angle)));
        if (left) {
            WingsGeometry.leftWing(sink(consumer), WHITE, light);
        } else {
            WingsGeometry.rightWing(sink(consumer), WHITE, light);
        }
        poseStack.popPose();
    }

    private static WingsGeometry.Sink sink(VertexConsumer consumer) {
        
        
        return (x, y, z, u, v, nx, ny, nz, argb, light) -> consumer
                .vertex(x, y, z)
                .color(argb)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(nx, ny, nz)
                .endVertex();
    }
}
