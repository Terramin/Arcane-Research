package arcaneresearch.helper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
public final class MenuHelper {
    private MenuHelper() {
    }
    public interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory playerInventory, FriendlyByteBuf buf);
    }
    public static <T extends AbstractContainerMenu> MenuType<T> createType(MenuFactory<T> factory) {
        net.minecraftforge.network.IContainerFactory<T> forge = factory::create;
        return IForgeMenuType.create(forge);
    }
}
