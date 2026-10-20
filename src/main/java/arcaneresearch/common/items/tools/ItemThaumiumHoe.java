package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumHoe extends Item {

    public ItemThaumiumHoe(Item.Properties props) {
        // NEOFORGE_PORT: HoeItem REMOVED in 26.x -> plain Item with .hoe(material, damage, speed).
        // Repair ingredient (thaumium ingot) is covered by the ToolMaterial repairItems tag.
        super(props
                .hoe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, -2, -1.0F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_thaumium)
    // -> REPAIRABLE component, so no override is needed (or possible).
}
