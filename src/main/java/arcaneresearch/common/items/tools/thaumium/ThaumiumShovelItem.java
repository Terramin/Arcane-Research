package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.Item;

public class ThaumiumShovelItem extends Item implements ThaumiumTool {
    public ThaumiumShovelItem(Item.Properties props) {
        super(ThaumiumTool.createProperties(props).shovel(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1.5F, -3.0F));
    }
}