package arcaneresearch.helper;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

/**
 * Единая точка создания {@code MenuType} с сетевой фабрикой
 * (порт из Forge 1.20.1: {@code IForgeMenuType} -&gt;
 * {@code IMenuTypeExtension}, буфер -&gt; {@code RegistryFriendlyByteBuf}).
 */
public final class MenuHelper {

    private MenuHelper() {
    }

    /** Сетевая фабрика меню: та же тройка, что у {@code IContainerFactory}. */
    public interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf);
    }

    /** {@code MenuType} с фабрикой чтения из сети. */
    public static <T extends AbstractContainerMenu> MenuType<T> createType(MenuFactory<T> factory) {
        net.neoforged.neoforge.network.IContainerFactory<T> neo = factory::create;
        return IMenuTypeExtension.create(neo);
    }
}
