package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.Item;

public class ThaumiumHoeItem extends Item implements ThaumiumTool {
    public ThaumiumHoeItem(Item.Properties props) {
        super(ThaumiumTool.createProperties(props).hoe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, -2, -1.0F));
    }
}