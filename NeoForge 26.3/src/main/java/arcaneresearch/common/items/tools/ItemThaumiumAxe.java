package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumAxe extends Item {

    public ItemThaumiumAxe(Item.Properties props) {
        // NEOFORGE_PORT: AxeItem REMOVED in 26.x -> plain Item with .axe(material, damage, speed).
        // Repair ingredient (thaumium ingot) is covered by the ToolMaterial repairItems tag.
        super(props
                .axe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 5.5F, -3.0F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_thaumium)
    // -> REPAIRABLE component, so no override is needed (or possible).
}
