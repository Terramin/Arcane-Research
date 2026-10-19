package arcaneresearch.lib.lighting;
import arcaneresearch.lib.colour.ColourRGBA;
import arcaneresearch.lib.render.CCRenderState;
public class PlanarLightModel implements CCRenderState.IVertexOperation {
    public static PlanarLightModel standardLightModel;
    static {
        PlanarLightModel.standardLightModel = LightModel.standardLightModel.reducePlanar();
    }
    public int[] colours;
    public PlanarLightModel(int[] colours) {
        this.colours = colours;
    }
    @Override
    public boolean load() {
        CCRenderState.pipeline.addDependency(CCRenderState.sideAttrib);
        CCRenderState.pipeline.addDependency(CCRenderState.colourAttrib);
        return true;
    }
    @Override
    public void operate() {
        CCRenderState.setColour(ColourRGBA.multiply(CCRenderState.colour, colours[CCRenderState.side]));
    }
    @Override
    public int operationID() {
        return LightModel.operationIndex;
    }
}
