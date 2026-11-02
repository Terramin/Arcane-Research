package arcaneresearch.client.renderers.tile;

import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
final class ObeliskCapModels {
    private static final ResourceLocation CAP_MODEL =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "models/obj/obelisk_cap.obj");
    private static final Quaternionf ROT_XN_90 = Axis.XN.rotationDegrees(90.0F);

    private static final class Holder {
        static final TurretObjModel MODEL = load();

        private static TurretObjModel load() {
            try {
                return TurretObjModel.load(CAP_MODEL);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private ObeliskCapModels() {
    }

    static TurretObjModel get() {
        return Holder.MODEL;
    }

    static void renderCap(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture,
                          int packedLight, int packedOverlay) {
        if (poseStack == null || buffer == null || texture == null) return;
        TurretObjModel model = get();
        if (model == null) return;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(ROT_XN_90);
            VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.objSolidCulled(texture));
            model.renderGroup(poseStack, consumer, "Cap", packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            poseStack.popPose();
        }
    }
}