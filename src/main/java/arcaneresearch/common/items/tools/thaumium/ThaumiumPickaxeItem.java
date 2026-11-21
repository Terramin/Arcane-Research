package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.Item;

public class ThaumiumPickaxeItem extends Item implements ThaumiumTool {
    public ThaumiumPickaxeItem(Item.Properties props) {
        super(ThaumiumTool.createProperties(props).pickaxe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1, -2.8F));
    }
}