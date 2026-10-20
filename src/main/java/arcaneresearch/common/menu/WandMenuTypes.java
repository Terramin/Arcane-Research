package arcaneresearch.common.menu;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension; // NEOFORGE_PORT: same create(factory) shape
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries; // NEOFORGE_PORT: was ForgeRegistries
// NEOFORGE_PORT: RegistryObject<MenuType<X>> -> DeferredHolder<MenuType<?>, MenuType<X>> below.
import arcaneresearch.Arcaneresearch;

public class WandMenuTypes {
    public static final DeferredRegister<MenuType<?>> TYPES =
            DeferredRegister.create(Registries.MENU, Arcaneresearch.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<WandFocusPouchMenu>> WAND_FOCUS_POUCH =
            TYPES.register("wand_focus_pouch", () -> IMenuTypeExtension.create(WandFocusPouchMenu::new));

    private WandMenuTypes() {
    }
}
