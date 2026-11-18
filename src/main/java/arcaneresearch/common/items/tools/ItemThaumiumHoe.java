package arcaneresearch.common.items.tools;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.init.ModItems;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemThaumiumHoe extends HoeItem {
    public ItemThaumiumHoe() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, -2, -1.0F,
                new Item.Properties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
}
