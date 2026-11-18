package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import org.jetbrains.annotations.NotNull;

public class ThaumiumShovelItem extends ShovelItem implements ThaumiumTool {
    public ThaumiumShovelItem() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1.5F, -3.0F,
                ThaumiumTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ThaumiumTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
