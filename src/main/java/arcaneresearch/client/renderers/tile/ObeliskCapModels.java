package arcaneresearch.client.renderers.tile;

import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

 







 
final class ObeliskCapModels {
    private static final Identifier CAP_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/obelisk_cap.obj");

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

     




 
    static void renderCapSubmit(PoseStack poseStack, SubmitNodeCollector collector, Identifier texture,
                                int packedLight, int packedOverlay) {
        TurretObjModel model = get();
        if (model == null) {
            return;
        }
        poseStack.pushPose();
        
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.rotate(Axis.XN.rotationDegrees(90.0F));
        RenderType type = TCRenderTypes.objSolid(texture);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            model.renderGroup(work, consumer, "Cap", packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);
        });
        poseStack.popPose();
    }
}
