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
@OnlyIn(Dist.CLIENT)
final class ObeliskCapModels {
    private static final ResourceLocation CAP_MODEL =
            new ResourceLocation("arcaneresearch", "models/obj/obelisk_cap.obj");
    private static volatile TurretObjModel MODEL = null;
    private static volatile boolean TRIED = false;
    private ObeliskCapModels() {
    }
    static TurretObjModel get() {
        if (!TRIED) {
            synchronized (ObeliskCapModels.class) {
                if (!TRIED) {
                    TRIED = true;
                    try {
                        MODEL = TurretObjModel.load(CAP_MODEL);
                    } catch (Throwable ignored) {
                        MODEL = null;
                    }
                }
            }
        }
        return MODEL;
    }
    static void renderCap(PoseStack poseStack, MultiBufferSource buffer, ResourceLocation texture,
                          int packedLight, int packedOverlay) {
        TurretObjModel model = get();
        if (model == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
        VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.objSolidCulled(texture));
        model.renderGroup(poseStack, consumer, "Cap", packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
