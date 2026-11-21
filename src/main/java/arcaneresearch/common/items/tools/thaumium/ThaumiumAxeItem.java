package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.Item;

public class ThaumiumAxeItem extends Item implements ThaumiumTool {
    public ThaumiumAxeItem(Item.Properties props) {
        super(ThaumiumTool.createProperties(props).axe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 5.5F, -3.0F));
    }
}