package arcaneresearch.common.items.tools.elemental;

import arcaneresearch.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public interface ElementalTool {

    static Item.Properties createProperties() {
        return new Item.Properties().rarity(Rarity.RARE);
    }

    static boolean canRepair(ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get());
    }
}
