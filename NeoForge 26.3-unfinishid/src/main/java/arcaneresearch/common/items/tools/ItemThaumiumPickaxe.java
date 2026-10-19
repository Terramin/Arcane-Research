package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumPickaxe extends Item {

    public ItemThaumiumPickaxe(Item.Properties props) {
        // NEOFORGE_PORT: PickaxeItem REMOVED in 26.x -> plain Item with .pickaxe(material, damage, speed).
        // Repair ingredient (thaumium ingot) is covered by the ToolMaterial repairItems tag.
        super(props
                .pickaxe(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1, -2.8F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_thaumium)
    // -> REPAIRABLE component, so no override is needed (or possible).
}
