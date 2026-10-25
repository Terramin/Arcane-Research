package arcaneresearch.common.menu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
public class WandMenuTypes {
    public static final DeferredRegister<MenuType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<MenuType<WandFocusPouchMenu>> WAND_FOCUS_POUCH =
            TYPES.register("wand_focus_pouch", () -> IForgeMenuType.create(WandFocusPouchMenu::new));
    private WandMenuTypes() {
    }
}
