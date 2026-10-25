package arcaneresearch.common.lib;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.init.ModCreativeTabs;
import arcaneresearch.init.ModItems;
@Deprecated
public class CreativeTabArcaneresearch {
    public static final String TAB_ID = "arcaneresearch_tab";
    public CreativeTabArcaneresearch(int par1, String par2Str) {
    }
    public CreativeTabArcaneresearch(String label) {
    }
    public static Object getTab() {
        return ModCreativeTabs.ARCANERESEARCH_TAB.get();
    }
    public ItemStack getTabIconItem() {
        return new ItemStack(ModItems.GOGGLES.get());
    }
}
