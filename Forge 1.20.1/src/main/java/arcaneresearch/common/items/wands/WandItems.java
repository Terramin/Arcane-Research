package arcaneresearch.common.items.wands;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.items.wands.foci.*;
import arcaneresearch.common.lib.wands.WandRegistry;
public class WandItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Arcaneresearch.MODID);
    public static final RegistryObject<Item> WAND = ITEMS.register("wand", ItemWand::new);
    public static final RegistryObject<Item> WAND_CAP = ITEMS.register("wand_cap", ItemWandCap::new);
    public static final RegistryObject<Item> WAND_FOCUS_POUCH = ITEMS.register("wand_focus_pouch", ItemFocusPouch::new);
    public static final RegistryObject<Item> WAND_FOCUS_FIRE = ITEMS.register("wand_focus_fire", ItemFocusFire::new);
    public static final RegistryObject<Item> WAND_FOCUS_FROST = ITEMS.register("wand_focus_frost", ItemFocusFrost::new);
    public static final RegistryObject<Item> WAND_FOCUS_SHOCK = ITEMS.register("wand_focus_shock", ItemFocusShock::new);
    public static final RegistryObject<Item> WAND_FOCUS_EXCAVATION = ITEMS.register("wand_focus_excavation", ItemFocusExcavation::new);
    public static final RegistryObject<Item> WAND_FOCUS_BUILDER = ITEMS.register("wand_focus_builder", ItemFocusBuilder::new);
    public static final RegistryObject<Item> WAND_FOCUS_TRADE = ITEMS.register("wand_focus_trade", ItemFocusTrade::new);
    public static final RegistryObject<Item> WAND_FOCUS_HOLE = ITEMS.register("wand_focus_hole", ItemFocusPortableHole::new);
    public static final RegistryObject<Item> WAND_FOCUS_GRAPPLE = ITEMS.register("wand_focus_grapple", ItemFocusGrapple::new);
    public static final RegistryObject<Item> WAND_FOCUS_SHARD = ITEMS.register("wand_focus_shard", ItemFocusShard::new);
    public static final RegistryObject<Item> WAND_FOCUS_PRIMAL = ITEMS.register("wand_focus_primal", ItemFocusPrimal::new);
    public static final RegistryObject<Item> WAND_FOCUS_HELLBAT = ITEMS.register("wand_focus_hellbat", ItemFocusHellbat::new);
    public static final RegistryObject<Item> WAND_FOCUS_PECH = ITEMS.register("wand_focus_pech", ItemFocusPech::new);
    public static final RegistryObject<Item> WAND_PECH_BLAST = ITEMS.register("wand_pech_blast",
            () -> new Item(new Item.Properties()));
    private WandItems() {
    }
    public static ItemStack createWandStack() {
        try {
            return new ItemStack(WAND.get());
        } catch (Exception e) {
            return new ItemStack(new ItemWand());
        }
    }
    public static void initWands() {
        WandRegistry.initWands();
        WandTriggers.registerTriggers();
    }
}
