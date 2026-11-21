package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.Item;

public class ThaumiumSwordItem extends Item implements ThaumiumTool {
    public ThaumiumSwordItem(Item.Properties props) {
        super(ThaumiumTool.createProperties(props).sword(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 3, -2.4F));
    }
}