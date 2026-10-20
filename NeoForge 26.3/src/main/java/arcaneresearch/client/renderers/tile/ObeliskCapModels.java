package arcaneresearch.client.renderers.tile;

import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Общая модель obelisk_cap.obj (как в TC4: один AdvancedModelLoader-файл
 * на все капы) и трансформация TC4 TileEldritchCapRenderer:
 * translate(x+0.5, y, z+0.5); glRotated(90, -1, 0, 0) — файловая ось Z
 * (0..0.8) смотрит ВВЕРХ, базовая плита 1x1 лежит на дне блока.
 * Эта трансформация используется и пьедесталом (cap), и алтарём
 * (altar-текстура в TC4), и низом пилона обелиска.
 * Лежит в arcaneresearch.arcaneresearch.client.renderers.tile.
 */
final class ObeliskCapModels {
    private static final Identifier CAP_MODEL =
            Identifier.fromNamespaceAndPath("arcaneresearch", "models/obj/obelisk_cap.obj");

    private static volatile TurretObjModel MODEL = null;
    private static volatile boolean TRIED = false;

    private ObeliskCapModels() {
    }

    /** Double-checked locking: рендер могут дёргать параллельно (как в EldritchObeliskRenderer). */
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

    /**
     * NEOFORGE_PORT (Forge 1:1, submit-phase): тот же кап через
     * {@code submitCustomGeometry} — {@code MultiBufferSource} удалён в 26.3,
     * геометрия уходит в {@link SubmitNodeCollector}. Используется рендером
     * алтаря (сиденье алтаря = кап obelisk_cap_altar).
     */
    static void renderCapSubmit(PoseStack poseStack, SubmitNodeCollector collector, Identifier texture,
                                int packedLight, int packedOverlay) {
        TurretObjModel model = get();
        if (model == null) {
            return;
        }
        poseStack.pushPose();
        // TC4 TileEldritchCapRenderer: translate(x+0.5, y, z+0.5); rotX -90.
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
