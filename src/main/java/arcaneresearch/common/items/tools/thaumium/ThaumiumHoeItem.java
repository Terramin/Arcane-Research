package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ThaumiumHoeItem extends HoeItem implements ThaumiumTool {
    public ThaumiumHoeItem() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, -2, -1.0F,
                ThaumiumTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ThaumiumTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
