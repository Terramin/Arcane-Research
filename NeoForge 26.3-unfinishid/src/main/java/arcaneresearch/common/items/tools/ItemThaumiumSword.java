package arcaneresearch.common.items.tools;

import net.minecraft.world.item.Item;
import arcaneresearch.api.ArcaneresearchMaterials;

public class ItemThaumiumSword extends Item {

    public ItemThaumiumSword(Item.Properties props) {
        // NEOFORGE_PORT: SwordItem REMOVED in 26.x -> plain Item with .sword(material, damage, speed).
        // Repair ingredient (thaumium ingot) is covered by the ToolMaterial repairItems tag.
        super(props
                .sword(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 3, -2.4F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_thaumium)
    // -> REPAIRABLE component, so no override is needed (or possible).
}
