package arcaneresearch.lib.vec;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
public class RedundantTransformation extends Transformation {
    @Override
    public void apply(Vector3 vec) {
    }
    @Override
    public void apply(Matrix4 mat) {
    }
    @Override
    public void applyN(Vector3 normal) {
    }
    @Override
    public Transformation at(Vector3 point) {
        return this;
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void apply(PoseStack poseStack) {
    }
    @Override
    public Transformation inverse() {
        return this;
    }
    @Override
    public Transformation merge(Transformation next) {
        return next;
    }
    @Override
    public boolean isRedundant() {
        return true;
    }
    @Override
    public String toString() {
        return "Nothing()";
    }
}
