package arcaneresearch.init;
import arcaneresearch.common.menu.AuraTotemMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.*;
public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Arcaneresearch.MODID);
    public static final RegistryObject<MenuType<GolemBuilderMenu>> GOLEM_BUILDER =
            MENU_TYPES.register("golem_builder",
                    () -> IForgeMenuType.create(GolemBuilderMenu::new));
    public static final RegistryObject<MenuType<SealMenu>> SEAL =
            MENU_TYPES.register("seal",
                    () -> IForgeMenuType.create(SealMenu::new));
    public static final RegistryObject<MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH =
            MENU_TYPES.register("arcane_workbench",
                    () -> IForgeMenuType.create(ArcaneWorkbenchMenu::new));
    public static final RegistryObject<MenuType<ThaumatoriumMenu>> THAUMATORIUM =
            MENU_TYPES.register("thaumatorium",
                    () -> IForgeMenuType.create(ThaumatoriumMenu::new));
    public static final RegistryObject<MenuType<ResearchTableMenu>> RESEARCH_TABLE =
            MENU_TYPES.register("research_table",
                    () -> IForgeMenuType.create(ResearchTableMenu::new));
    public static final RegistryObject<MenuType<DeconstructMenu>> DECONSTRUCTION_TABLE =
            MENU_TYPES.register("deconstruction_table",
                    () -> IForgeMenuType.create(DeconstructMenu::new));
    public static final RegistryObject<MenuType<SmelterMenu>> SMELTER =
            MENU_TYPES.register("smelter",
                    () -> IForgeMenuType.create(SmelterMenu::new));
    public static final RegistryObject<MenuType<FocalManipulatorMenu>> FOCAL_MANIPULATOR =
            MENU_TYPES.register("focal_manipulator",
                    () -> IForgeMenuType.create(FocalManipulatorMenu::new));
    public static final RegistryObject<MenuType<FocusPouchMenu>> FOCUS_POUCH =
            MENU_TYPES.register("focus_pouch",
                    () -> IForgeMenuType.create(FocusPouchMenu::new));
    public static final RegistryObject<MenuType<HandMirrorMenu>> HAND_MIRROR =
            MENU_TYPES.register("hand_mirror",
                    () -> IForgeMenuType.create(HandMirrorMenu::new));
    public static final RegistryObject<MenuType<HungryChestMenu>> HUNGRY_CHEST =
            MENU_TYPES.register("hungry_chest",
                    () -> IForgeMenuType.create(HungryChestMenu::new));
    public static final RegistryObject<MenuType<PotionSprayerMenu>> POTION_SPRAYER =
            MENU_TYPES.register("potion_sprayer",
                    () -> IForgeMenuType.create(PotionSprayerMenu::new));
    public static final RegistryObject<MenuType<SpaMenu>> SPA =
            MENU_TYPES.register("spa",
                    () -> IForgeMenuType.create(SpaMenu::new));
    public static final RegistryObject<MenuType<VoidSiphonMenu>> VOID_SIPHON =
            MENU_TYPES.register("void_siphon",
                    () -> IForgeMenuType.create(VoidSiphonMenu::new));
    public static final RegistryObject<MenuType<TurretMenu>> TURRET_BASIC =
            MENU_TYPES.register("turret_basic",
                    () -> IForgeMenuType.create(TurretMenu::new));
    public static final RegistryObject<MenuType<TurretMenu>> TURRET_ADVANCED =
            MENU_TYPES.register("turret_advanced",
                    () -> IForgeMenuType.create(TurretMenu::new));
    public static final RegistryObject<MenuType<ArcaneBoreMenu>> ARCANE_BORE =
            MENU_TYPES.register("arcane_bore",
                    () -> IForgeMenuType.create(ArcaneBoreMenu::new));
    public static final RegistryObject<MenuType<PechMenu>> PECH_TRADING =
            MENU_TYPES.register("pech_trading",
                    () -> IForgeMenuType.create(PechMenu::new));
    public static final RegistryObject<MenuType<LogisticsMenu>> LOGISTICS =
            MENU_TYPES.register("logistics",
                    () -> IForgeMenuType.create(LogisticsMenu::new));
    public static final RegistryObject<MenuType<arcaneresearch.common.menu.AuraTotemMenu>> AURA_TOTEM =
            MENU_TYPES.register("aura_totem",
                    () -> IForgeMenuType.create(arcaneresearch.common.menu.AuraTotemMenu::new));
}
