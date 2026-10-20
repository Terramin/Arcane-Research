package arcaneresearch.common.golems.client;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.parts.PartModel;

@Deprecated
public class PartModelDarts extends arcaneresearch.client.renderers.entity.golem.PartModelDarts {
    public PartModelDarts(Identifier objModel, Identifier objTexture, PartModel.EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
}