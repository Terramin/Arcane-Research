package arcaneresearch.common.golems.client;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.parts.PartModel;

@Deprecated
public class PartModelWheel extends arcaneresearch.client.renderers.entity.golem.PartModelWheel {
    public PartModelWheel(Identifier objModel, Identifier objTexture, PartModel.EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
}