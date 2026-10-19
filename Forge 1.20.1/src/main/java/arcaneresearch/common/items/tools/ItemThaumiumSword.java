package arcaneresearch.common.items.tools;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.init.ModItems;
public class ItemThaumiumSword extends SwordItem {
    public ItemThaumiumSword() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 3, -2.4F,
                new Item.Properties());
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
}
