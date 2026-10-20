package arcaneresearch.common.golems.client;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.parts.PartModel;
@OnlyIn(Dist.CLIENT)
@Deprecated
public class PartModelDarts extends arcaneresearch.client.renderers.entity.golem.PartModelDarts {
    public PartModelDarts(ResourceLocation objModel, ResourceLocation objTexture, PartModel.EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
}
