package arcaneresearch.common.golems.client;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.parts.PartModel;
@OnlyIn(Dist.CLIENT)
@Deprecated
public class PartModelClaws extends arcaneresearch.client.renderers.entity.golem.PartModelClaws {
    public PartModelClaws(ResourceLocation objModel, ResourceLocation objTexture, PartModel.EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
}
