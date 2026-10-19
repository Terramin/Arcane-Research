package arcaneresearch.client.models.armor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class VoidRobeArmorModel extends CultistRobeArmorModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "void_robe_armor"), "main");
    public VoidRobeArmorModel(ModelPart root) {
        super(root);
    }
    public static LayerDefinition createBodyLayer() {
        return CultistRobeArmorModel.createBodyLayer();
    }
}
