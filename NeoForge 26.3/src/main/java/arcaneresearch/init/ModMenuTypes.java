package arcaneresearch.init;
import arcaneresearch.common.menu.AuraTotemMenu;

import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension; // NEOFORGE_PORT: same create(factory) shape
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries; // NEOFORGE_PORT: was ForgeRegistries
// NEOFORGE_PORT: RegistryObject<MenuType<X>> -> DeferredHolder<MenuType<?>, MenuType<X>> below.
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.*;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 init/ModMenuTypes.java (PORTED 90%).
// REQUIRED changes:
//   DeferredRegister.create(ForgeRegistries.MENU_TYPES, ...) -> create(Registries.MENU, ...)
//   RegistryObject -> DeferredHolder; IForgeMenuType.create -> IMenuTypeExtension.create
//   (identical (windowId, inv, data) factory shape; data is RegistryFriendlyByteBuf now).
// All 19 entries ported in ORIGINAL ORDER with IDENTICAL registry names.
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Arcaneresearch.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<GolemBuilderMenu>> GOLEM_BUILDER =
            MENU_TYPES.register("golem_builder",
                    () -> IMenuTypeExtension.create(GolemBuilderMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SealMenu>> SEAL =
            MENU_TYPES.register("seal",
                    () -> IMenuTypeExtension.create(SealMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneWorkbenchMenu>> ARCANE_WORKBENCH =
            MENU_TYPES.register("arcane_workbench",
                    () -> IMenuTypeExtension.create(ArcaneWorkbenchMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ThaumatoriumMenu>> THAUMATORIUM =
            MENU_TYPES.register("thaumatorium",
                    () -> IMenuTypeExtension.create(ThaumatoriumMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ResearchTableMenu>> RESEARCH_TABLE =
            MENU_TYPES.register("research_table",
                    () -> IMenuTypeExtension.create(ResearchTableMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<DeconstructMenu>> DECONSTRUCTION_TABLE =
            MENU_TYPES.register("deconstruction_table",
                    () -> IMenuTypeExtension.create(DeconstructMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SmelterMenu>> SMELTER =
            MENU_TYPES.register("smelter",
                    () -> IMenuTypeExtension.create(SmelterMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FocalManipulatorMenu>> FOCAL_MANIPULATOR =
            MENU_TYPES.register("focal_manipulator",
                    () -> IMenuTypeExtension.create(FocalManipulatorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FocusPouchMenu>> FOCUS_POUCH =
            MENU_TYPES.register("focus_pouch",
                    () -> IMenuTypeExtension.create(FocusPouchMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HandMirrorMenu>> HAND_MIRROR =
            MENU_TYPES.register("hand_mirror",
                    () -> IMenuTypeExtension.create(HandMirrorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HungryChestMenu>> HUNGRY_CHEST =
            MENU_TYPES.register("hungry_chest",
                    () -> IMenuTypeExtension.create(HungryChestMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PotionSprayerMenu>> POTION_SPRAYER =
            MENU_TYPES.register("potion_sprayer",
                    () -> IMenuTypeExtension.create(PotionSprayerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SpaMenu>> SPA =
            MENU_TYPES.register("spa",
                    () -> IMenuTypeExtension.create(SpaMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<VoidSiphonMenu>> VOID_SIPHON =
            MENU_TYPES.register("void_siphon",
                    () -> IMenuTypeExtension.create(VoidSiphonMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TurretMenu>> TURRET_BASIC =
            MENU_TYPES.register("turret_basic",
                    () -> IMenuTypeExtension.create(TurretMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TurretMenu>> TURRET_ADVANCED =
            MENU_TYPES.register("turret_advanced",
                    () -> IMenuTypeExtension.create(TurretMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ArcaneBoreMenu>> ARCANE_BORE =
            MENU_TYPES.register("arcane_bore",
                    () -> IMenuTypeExtension.create(ArcaneBoreMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PechMenu>> PECH_TRADING =
            MENU_TYPES.register("pech_trading",
                    () -> IMenuTypeExtension.create(PechMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<LogisticsMenu>> LOGISTICS =
            MENU_TYPES.register("logistics",
                    () -> IMenuTypeExtension.create(LogisticsMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<arcaneresearch.common.menu.AuraTotemMenu>> AURA_TOTEM =
            MENU_TYPES.register("aura_totem",
                    () -> IMenuTypeExtension.create(arcaneresearch.common.menu.AuraTotemMenu::new));
}
