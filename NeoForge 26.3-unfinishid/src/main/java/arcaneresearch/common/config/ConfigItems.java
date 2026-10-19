package arcaneresearch.common.config;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder; // NEOFORGE_PORT: was RegistryObject
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.OreDictionaryEntries;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModCreativeTabs;

import java.util.ArrayList;
import java.util.List;

public class ConfigItems {

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> TABTC = ModCreativeTabs.ARCANERESEARCH_TAB;

    public static List<Object> ITEM_VARIANT_HOLDERS = new ArrayList<>();

    public static ItemStack startBook = new ItemStack(Items.WRITTEN_BOOK);

    public static ItemStack AIR_CRYSTAL;
    public static ItemStack FIRE_CRYSTAL;
    public static ItemStack WATER_CRYSTAL;
    public static ItemStack EARTH_CRYSTAL;
    public static ItemStack ORDER_CRYSTAL;
    public static ItemStack ENTROPY_CRYSTAL;
    public static ItemStack FLUX_CRYSTAL;

    public static void initMisc() {
        OreDictionaryEntries.initializeOreDictionary();
        AIR_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.AIR);
        FIRE_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.FIRE);
        WATER_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.WATER);
        EARTH_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.EARTH);
        ORDER_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.ORDER);
        ENTROPY_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.ENTROPY);
        FLUX_CRYSTAL = ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX);
        startBook = createStartBook();
        Arcaneresearch.LOGGER.info("Initialized Arcaneresearch item behaviour defaults");
    }

    public static ItemStack createStartBook() {
        // NEOFORGE_PORT: root-tag book NBT (setTag) removed in 26.x ->
        // WRITTEN_BOOK_CONTENT component (Filterable title + Component pages, same text).
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<net.minecraft.server.network.Filterable<Component>> pages = new ArrayList<>();
        pages.add(net.minecraft.server.network.Filterable.passThrough(Component.translatable("book.start.1")));
        pages.add(net.minecraft.server.network.Filterable.passThrough(Component.translatable("book.start.2")));
        pages.add(net.minecraft.server.network.Filterable.passThrough(Component.translatable("book.start.3")));
        book.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT,
                new net.minecraft.world.item.component.WrittenBookContent(
                        net.minecraft.server.network.Filterable.passThrough(Component.translatable("book.start.title").getString()),
                        "", 3, pages, false));
        return book;
    }

    public static void init() {
        arcaneresearch.common.items.casters.FocusInit.registerFoci();
    }

    public static void preInitSeals() {
        arcaneresearch.common.golems.seals.SealHandler.registerDefaultSeals();
    }
}
