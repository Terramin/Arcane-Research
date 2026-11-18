package arcaneresearch.common.items.tools;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.init.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import org.jetbrains.annotations.NotNull;

public class ItemThaumiumShovel extends ShovelItem {
    public ItemThaumiumShovel() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1.5F, -3.0F,
                new Item.Properties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
}
