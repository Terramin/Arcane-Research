package arcaneresearch.lib.render.uv;
import arcaneresearch.lib.render.CCRenderState;
import arcaneresearch.lib.vec.ITransformation;
public abstract class UVTransformation extends ITransformation<UV, UVTransformation> implements CCRenderState.IVertexOperation {
    public static int operationIndex;
    static {
        operationIndex = CCRenderState.registerOperation();
    }
    @Override
    public UVTransformation at(UV point) {
        return new UVTransformationList(new UVTranslation(-point.u, -point.v), this, new UVTranslation(point.u, point.v));
    }
    @Override
    public UVTransformationList with(UVTransformation t) {
        return new UVTransformationList(this, t);
    }
    @Override
    public boolean load() {
        return !isRedundant();
    }
    @Override
    public void operate() {
        apply(CCRenderState.vert.uv);
    }
    @Override
    public int operationID() {
        return UVTransformation.operationIndex;
    }
}
