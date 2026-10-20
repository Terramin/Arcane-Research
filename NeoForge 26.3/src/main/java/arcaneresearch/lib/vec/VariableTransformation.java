package arcaneresearch.lib.vec;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.api.distmarker.Dist;

public abstract class VariableTransformation extends Transformation {
    public Matrix4 mat;

    public VariableTransformation(Matrix4 mat) {
        this.mat = mat;
    }

    @Override
    public void applyN(Vector3 normal) {
        apply(normal);
    }

    @Override
    public void apply(Matrix4 mat) {
        mat.multiply(this.mat);
    }

    @Override
    public void apply(PoseStack poseStack) {
        mat.apply(poseStack);
    }
}