package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ThaumiumTool {

    static Item.Properties createProperties(Item.Properties props) {
        return props;
    }

    static boolean canRepair(ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get());
    }
}
