package arcaneresearch.client.models.armor;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;


public class VoidRobeArmorModel extends CultistRobeArmorModel {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "void_robe_armor"), "main");

    public VoidRobeArmorModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return CultistRobeArmorModel.createBodyLayer();
    }
}