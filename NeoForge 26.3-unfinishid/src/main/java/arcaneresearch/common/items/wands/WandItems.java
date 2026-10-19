package arcaneresearch.common.items.wands;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.items.wands.foci.*;
import arcaneresearch.common.lib.wands.WandRegistry;

public class WandItems {
    // NEOFORGE_PORT: DeferredRegister.create(ForgeRegistries.ITEMS, MODID) ->
    // DeferredRegister.createItems(MODID); RegistryObject<Item> -> DeferredItem<Item> (same .get()).
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Arcaneresearch.MODID);

    public static final DeferredItem<Item> WAND = ITEMS.registerItem("wand", ItemWand::new);
    public static final DeferredItem<Item> WAND_CAP = ITEMS.registerItem("wand_cap", ItemWandCap::new);
    public static final DeferredItem<Item> WAND_FOCUS_POUCH = ITEMS.registerItem("wand_focus_pouch", ItemFocusPouch::new);

    public static final DeferredItem<Item> WAND_FOCUS_FIRE = ITEMS.registerItem("wand_focus_fire", ItemFocusFire::new);
    public static final DeferredItem<Item> WAND_FOCUS_FROST = ITEMS.registerItem("wand_focus_frost", ItemFocusFrost::new);
    public static final DeferredItem<Item> WAND_FOCUS_SHOCK = ITEMS.registerItem("wand_focus_shock", ItemFocusShock::new);
    public static final DeferredItem<Item> WAND_FOCUS_EXCAVATION = ITEMS.registerItem("wand_focus_excavation", ItemFocusExcavation::new);
    public static final DeferredItem<Item> WAND_FOCUS_BUILDER = ITEMS.registerItem("wand_focus_builder", ItemFocusBuilder::new);
    public static final DeferredItem<Item> WAND_FOCUS_TRADE = ITEMS.registerItem("wand_focus_trade", ItemFocusTrade::new);
    public static final DeferredItem<Item> WAND_FOCUS_HOLE = ITEMS.registerItem("wand_focus_hole", ItemFocusPortableHole::new);
    public static final DeferredItem<Item> WAND_FOCUS_GRAPPLE = ITEMS.registerItem("wand_focus_grapple", ItemFocusGrapple::new);
    public static final DeferredItem<Item> WAND_FOCUS_SHARD = ITEMS.registerItem("wand_focus_shard", ItemFocusShard::new);
    public static final DeferredItem<Item> WAND_FOCUS_PRIMAL = ITEMS.registerItem("wand_focus_primal", ItemFocusPrimal::new);
    public static final DeferredItem<Item> WAND_FOCUS_HELLBAT = ITEMS.registerItem("wand_focus_hellbat", ItemFocusHellbat::new);
    public static final DeferredItem<Item> WAND_FOCUS_PECH = ITEMS.registerItem("wand_focus_pech", ItemFocusPech::new);

    public static final DeferredItem<Item> WAND_PECH_BLAST = ITEMS.registerItem("wand_pech_blast",
            props -> new Item(props));

    private WandItems() {
    }

    public static ItemStack createWandStack() {
        try {
            return new ItemStack(WAND.get());
        } catch (Exception e) {
            return new ItemStack(new ItemWand(new Item.Properties()));
        }
    }

    public static void initWands() {
        WandRegistry.initWands();
        WandTriggers.registerTriggers();
    }
}
