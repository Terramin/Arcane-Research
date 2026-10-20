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
        // NEOFORGE_PORT (26.3): Forge stored the translated lang strings as raw page JSON (the
        // lang values are quoted JSON strings with \n escapes, parsed by the book screen) ->
        // parsed the same way here (mirrors PlayerEvents.bookPage), title capped at 32 chars
        // (26.3 codec limit), no author (blank author = no "by" line, as Forge had no author).
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<net.minecraft.server.network.Filterable<Component>> pages = new ArrayList<>();
        pages.add(net.minecraft.server.network.Filterable.passThrough(bookPage(Component.translatable("book.start.1").getString())));
        pages.add(net.minecraft.server.network.Filterable.passThrough(bookPage(Component.translatable("book.start.2").getString())));
        pages.add(net.minecraft.server.network.Filterable.passThrough(bookPage(Component.translatable("book.start.3").getString())));
        String title = Component.translatable("book.start.title").getString();
        if (title.length() > net.minecraft.world.item.component.WrittenBookContent.TITLE_MAX_LENGTH) {
            title = title.substring(0, net.minecraft.world.item.component.WrittenBookContent.TITLE_MAX_LENGTH);
        }
        book.set(net.minecraft.core.component.DataComponents.WRITTEN_BOOK_CONTENT,
                new net.minecraft.world.item.component.WrittenBookContent(
                        net.minecraft.server.network.Filterable.passThrough(title),
                        "", 3, pages, false));
        return book;
    }

    /** NEOFORGE_PORT (26.3): Forge book page = JSON text (fallback: literal), as BookViewScreen parsed it. */
    private static Component bookPage(String raw) {
        try {
            return net.minecraft.network.chat.ComponentSerialization.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(raw))
                    .getOrThrow();
        } catch (Exception e) {
            return Component.literal(raw);
        }
    }

    public static void init() {
        arcaneresearch.common.items.casters.FocusInit.registerFoci();
    }

    public static void preInitSeals() {
        arcaneresearch.common.golems.seals.SealHandler.registerDefaultSeals();
    }
}
