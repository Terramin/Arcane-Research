package arcaneresearch.common.golems.client;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.parts.PartModel;

@Deprecated
public class PartModelHauler extends arcaneresearch.client.renderers.entity.golem.PartModelHauler {
    public PartModelHauler(Identifier objModel, Identifier objTexture, PartModel.EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
}