package arcaneresearch.helper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

 



 
public final class MenuHelper {

    private MenuHelper() {
    }

      
    public interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf);
    }

      
    public static <T extends AbstractContainerMenu> MenuType<T> createType(MenuFactory<T> factory) {
        net.neoforged.neoforge.network.IContainerFactory<T> neo = factory::create;
        return IMenuTypeExtension.create(neo);
    }
}
