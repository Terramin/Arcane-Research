package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ThaumiumAxeItem extends AxeItem implements ThaumiumTool {
    public ThaumiumAxeItem() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 5.5F, -3.0F,
                ThaumiumTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ThaumiumTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
