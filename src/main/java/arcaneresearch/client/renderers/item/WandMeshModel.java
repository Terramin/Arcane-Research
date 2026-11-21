package arcaneresearch.client.renderers.item;

import arcaneresearch.api.wands.FocusItem;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.client.lib.obj.MeshModel;

/**
 * The legacy {@code BakedModel}-based wand mesh was replaced on NeoForge 26.x
 * by {@link WandSpecialRenderer}, which uses the SpecialModelRenderer API and
 * the shared OBJ loader.  This holder is retained so callers still compile.
 */
public class WandMeshModel {
    static final java.util.Map<String, WandMeshModel> variants =
            new java.util.concurrent.ConcurrentHashMap<>();

    private final MeshModel sourceMesh;
    private final String model;

    public WandMeshModel(String variant, WandCap cap) {
        this(variant, cap, false);
    }

    public WandMeshModel(String variant, FocusItem focus) {
        this.sourceMesh = null;
        this.model = variant;
    }

    public WandMeshModel(String variant, WandRod rod) {
        this.sourceMesh = null;
        this.model = variant;
    }

    public WandMeshModel(String variant, WandCap cap, boolean staff) {
        this.sourceMesh = null;
        this.model = variant;
    }

    public WandMeshModel(String itemName) {
        this.sourceMesh = null;
        this.model = itemName;
    }

    public MeshModel getSourceMesh() {
        return sourceMesh;
    }

    public String getModel() {
        return model;
    }
}
