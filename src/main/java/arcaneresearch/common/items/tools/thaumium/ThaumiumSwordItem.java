package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.jetbrains.annotations.NotNull;

public class ThaumiumSwordItem extends SwordItem implements ThaumiumTool {
    public ThaumiumSwordItem() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 3, -2.4F,
                ThaumiumTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ThaumiumTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
