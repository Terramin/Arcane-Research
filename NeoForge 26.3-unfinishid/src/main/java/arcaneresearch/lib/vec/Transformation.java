package arcaneresearch.lib.vec;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.lib.render.CCRenderState;

public abstract class Transformation extends ITransformation<Vector3, Transformation> implements CCRenderState.IVertexOperation {
    public static int operationIndex;

    static {
        operationIndex = CCRenderState.registerOperation();
    }

    public abstract void applyN(Vector3 p0);

    public abstract void apply(Matrix4 p0);

    @Override
    public Transformation at(Vector3 point) {
        return new TransformationList(new Translation(-point.x, -point.y, -point.z), this, point.translation());
    }

    @Override
    public TransformationList with(Transformation t) {
        return new TransformationList(this, t);
    }

    public abstract void apply(PoseStack poseStack);

    @Override
    public boolean load() {
        CCRenderState.pipeline.addRequirement(CCRenderState.normalAttrib.operationID());
        return !isRedundant();
    }

    @Override
    public void operate() {
        apply(CCRenderState.vert.vec);
        if (CCRenderState.normalAttrib.active) {
            applyN(CCRenderState.normal);
        }
    }

    @Override
    public int operationID() {
        return Transformation.operationIndex;
    }
}