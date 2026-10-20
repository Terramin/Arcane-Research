package arcaneresearch.common.items.tools;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.init.ModItems;
public class ItemThaumiumAxe extends AxeItem {
    public ItemThaumiumAxe() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 5.5F, -3.0F,
                new Item.Properties());
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
}
