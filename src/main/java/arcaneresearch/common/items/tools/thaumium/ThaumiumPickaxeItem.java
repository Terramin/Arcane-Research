package arcaneresearch.common.items.tools.thaumium;

import arcaneresearch.api.ArcaneresearchMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import org.jetbrains.annotations.NotNull;

public class ThaumiumPickaxeItem extends PickaxeItem implements ThaumiumTool {
    public ThaumiumPickaxeItem() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1, -2.8F,
                ThaumiumTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ThaumiumTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
