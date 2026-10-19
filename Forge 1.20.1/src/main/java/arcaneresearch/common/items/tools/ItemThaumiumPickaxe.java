package arcaneresearch.common.items.tools;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.init.ModItems;
public class ItemThaumiumPickaxe extends PickaxeItem {
    public ItemThaumiumPickaxe() {
        super(ArcaneresearchMaterials.TOOLMAT_THAUMIUM, 1, -2.8F,
                new Item.Properties());
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.THAUMIUM_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }
}
