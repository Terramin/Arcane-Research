package arcaneresearch.common.config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.OreDictionaryEntries;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.init.ModCreativeTabs;
import java.util.ArrayList;
import java.util.List;
public class ConfigItems {
    public static RegistryObject<CreativeModeTab> TABTC = ModCreativeTabs.ARCANERESEARCH_TAB;
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
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        net.minecraft.nbt.CompoundTag contents = new net.minecraft.nbt.CompoundTag();
        contents.putInt("generation", 3);
        contents.putString("title", Component.translatable("book.start.title").getString());
        net.minecraft.nbt.ListTag pages = new net.minecraft.nbt.ListTag();
        pages.add(net.minecraft.nbt.StringTag.valueOf(Component.translatable("book.start.1").getString()));
        pages.add(net.minecraft.nbt.StringTag.valueOf(Component.translatable("book.start.2").getString()));
        pages.add(net.minecraft.nbt.StringTag.valueOf(Component.translatable("book.start.3").getString()));
        contents.put("pages", pages);
        book.setTag(contents);
        return book;
    }
    public static void init() {
        arcaneresearch.common.items.casters.FocusInit.registerFoci();
    }
    public static void preInitSeals() {
        arcaneresearch.common.golems.seals.SealHandler.registerDefaultSeals();
    }
}
