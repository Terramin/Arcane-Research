package arcaneresearch.client.renderers;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.item.JarLabelPlacement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paper label and aspect icon shared by the jar block and item renderers.
 *
 * <p>The block and the item both pass a pose whose origin is the centre of the
 * jar. The label is then placed at an explicit point in that frame, so the item
 * can anchor it to the real baked body face instead of guessing coordinates.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class JarLabelRenderer {
    private static final ResourceLocation LABEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/label.png");
    private static final RenderType LABEL_TYPE = RenderType.entityTranslucent(LABEL_TEXTURE);
    private static final Map<ResourceLocation, RenderType> ICON_TYPES = new ConcurrentHashMap<>();
    private static final int LIGHT_FULL = 0x00F000F0;

    /** The icon is 8 * 0.03 * 0.5 = 0.12, i.e. 0.48 of the paper half-size. */
    private static final float ICON_RATIO = 0.48F;
    /** 0.01 in the 0.5-scaled block frame. */
    private static final float ICON_FORWARD = 0.005F;

    private JarLabelRenderer() {
    }

    /** Block renderer entry point: yaw from the stored facing, optional tilt. */
    public static void render(int yawDegrees, @Nullable Float crookedDegrees, Aspect aspect,
                              PoseStack poseStack, MultiBufferSource buffer, int paperLight) {
        poseStack.pushPose();
        try {
            poseStack.mulPose(Axis.YP.rotationDegrees(yawDegrees));
            JarLabelPlacement.Placement place = JarLabelPlacement.fallback(false);
            renderPlaced(place.x(), place.y(), place.z(), place.half(),
                    crookedDegrees, aspect, poseStack, buffer, paperLight);
        } finally {
            poseStack.popPose();
        }
    }

    /**
     * Draws the label at an explicit point in the (already face-oriented) pose.
     * The item renderer uses this with the bounds of the real body quad.
     */
    public static void renderPlaced(float x, float y, float z, float halfSize,
                                    @Nullable Float crookedDegrees, Aspect aspect,
                                    PoseStack poseStack, MultiBufferSource buffer, int paperLight) {
        poseStack.pushPose();
        try {
            poseStack.translate(x, y, z);
            if (crookedDegrees != null) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(crookedDegrees));
            }
            drawQuad(buffer.getBuffer(LABEL_TYPE), poseStack, halfSize,
                    1.0F, 1.0F, 1.0F, paperLight);

            ResourceLocation image = aspect.getImage();
            if (image == null) {
                return;
            }
            ResourceLocation stripped = UtilsFX.strippedAspectTexture(image);
            if (stripped == null) {
                return;
            }

            poseStack.translate(0.0F, 0.0F, ICON_FORWARD);
            VertexConsumer iconConsumer = buffer.getBuffer(
                    ICON_TYPES.computeIfAbsent(stripped, RenderType::entityTranslucent));
            int color = aspect.getColor();
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            drawQuad(iconConsumer, poseStack, halfSize * ICON_RATIO, r, g, b, LIGHT_FULL);
        } finally {
            poseStack.popPose();
        }
    }

    /** Quad on local +Z with UVs (0,1),(1,1),(1,0),(0,0), as in the block renderer. */
    private static void drawQuad(VertexConsumer consumer, PoseStack poseStack, float size,
                                 float r, float g, float b, int light) {
        Matrix4f matrix = poseStack.last().pose();
        consumer.vertex(matrix, -size, -size, 0).color(r, g, b, 1.0F)
                .uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, size, -size, 0).color(r, g, b, 1.0F)
                .uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, size, size, 0).color(r, g, b, 1.0F)
                .uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, -size, size, 0).color(r, g, b, 1.0F)
                .uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0, 0, 1).endVertex();
    }
}
