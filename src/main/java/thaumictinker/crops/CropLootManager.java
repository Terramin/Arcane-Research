package thaumictinker.crops;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.items.resources.ItemWispEssence;
import arcaneresearch.init.ModItems;
import net.minecraftforge.registries.ForgeRegistries;

 




 
public final class CropLootManager {

    private static final String[] SAPLINGS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};

    private CropLootManager() {
    }

    public record Entry(ItemStack stack, int weight) {
    }

    public static ItemStack getLootForAspect(Aspect aspect, RandomSource random) {
        List<Entry> entries = entries(aspect);
        int sum = 0;
        for (Entry entry : entries) {
            sum += entry.weight();
        }
        if (sum <= 0) {
            return ItemStack.EMPTY;
        }
        int roll = random.nextInt(sum);
        for (Entry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.stack().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    public static List<Entry> entries(Aspect aspect) {
        List<Entry> entries = new ArrayList<>();
        String tag = tag(aspect);
        if (aspect == Aspect.AIR) {
            add(entries, "thaumictinker:aer_infused_grain", 1, 1);
        } else if (aspect == Aspect.FIRE) {
            add(entries, "thaumictinker:ignis_infused_grain", 1, 1);
        } else if (aspect == Aspect.EARTH) {
            add(entries, "thaumictinker:terra_infused_grain", 1, 1);
        } else if (aspect == Aspect.WATER) {
            add(entries, "thaumictinker:aqua_infused_grain", 1, 1);
        } else if (aspect == Aspect.ORDER) {
            add(entries, "minecraft:glass", 64, 1);
        } else if (aspect == Aspect.ENTROPY) {
            add(entries, "minecraft:sand", 64, 1);
        } else if (tag.equals("alienis")) {
            add(entries, "minecraft:ender_pearl", 4, 10);
            add(entries, "minecraft:ender_eye", 4, 5);
        } else if (tag.equals("auram")) {
            for (Aspect tagAspect : Aspect.aspects.values()) {
                entries.add(new Entry(ItemWispEssence.createStack(ModItems.WISP_ESSENCE.get(), tagAspect, 1), 1));
            }
        } else if (tag.equals("bestia")) {
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item instanceof SpawnEggItem) {
                    entries.add(new Entry(new ItemStack(item), 1));
                }
            }
        } else if (tag.equals("cognitio")) {
            add(entries, "minecraft:paper", 64, 15);
            add(entries, "minecraft:book", 32, 10);
            add(entries, "minecraft:bookshelf", 16, 5);
            for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS.getValues()) {
                entries.add(new Entry(EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(enchantment, 1)), 1));
            }
        } else if (tag.equals("fabrico")) {
            add(entries, "thaumictinker:dark_quartz_block", 32, 1);
            add(entries, "arcaneresearch:infernal_furnace", 16, 1);
        } else if (tag.equals("gelum")) {
            add(entries, "minecraft:snowball", 16, 1);
        } else if (tag.equals("herba")) {
            addTag(entries, ItemTags.SAPLINGS);
            for (String sapling : SAPLINGS) {
                add(entries, "minecraft:" + sapling + "_sapling", 1, 1);
            }
        } else if (tag.equals("humanus")) {
            
        } else if (tag.equals("instrumentum")) {
            add(entries, "arcaneresearch:thaumium_pick", 1, 1);
            add(entries, "arcaneresearch:thaumium_shovel", 1, 1);
            add(entries, "arcaneresearch:thaumium_axe", 1, 1);
            add(entries, "arcaneresearch:thaumium_hoe", 1, 1);
        } else if (tag.equals("desiderium")) {
            add(entries, "minecraft:gold_ingot", 4, 1);
        } else if (tag.equals("lux")) {
            add(entries, "minecraft:glowstone_dust", 16, 5);
            add(entries, "thaumictinker:bright_nitor", 1, 1);
        } else if (tag.equals("machina")) {
            add(entries, "minecraft:piston", 8, 1);
        } else if (tag.equals("metallum")) {
            add(entries, "minecraft:iron_ingot", 4, 100);
            addTag(entries, commonTag("ingots/iron"));
            addTag(entries, commonTag("nuggets/iron"));
            addTag(entries, commonTag("ores/iron"));
            addTag(entries, commonTag("storage_blocks/iron"));
        } else if (tag.equals("mortuus")) {
            add(entries, "minecraft:bone", 32, 1);
        } else if (tag.equals("motus")) {
            add(entries, "minecraft:rail", 1, 10);
            add(entries, "minecraft:activator_rail", 1, 1);
        } else if (tag.equals("pannus")) {
            add(entries, "minecraft:white_wool", 16, 30);
            add(entries, "minecraft:string", 15, 10);
            for (DyeColor color : DyeColor.values()) {
                add(entries, "minecraft:" + color.getName() + "_wool", 4, 1);
            }
        } else if (tag.equals("permutatio")) {
            addTag(entries, commonTag("ingots/copper"), 4);
            add(entries, "arcaneresearch:cluster_cinnabar", 4, 1);
        } else if (tag.equals("potentia")) {
            add(entries, "arcaneresearch:alumentum", 12, 1);
        } else if (tag.equals("sensus")) {
            add(entries, "minecraft:lapis_lazuli", 20, 1);
        } else if (tag.equals("spiritus")) {
            add(entries, "minecraft:soul_sand", 64, 2);
            add(entries, "minecraft:netherrack", 64, 2);
            add(entries, "minecraft:nether_bricks", 1, 1);
        } else if (tag.equals("tenebrae")) {
            add(entries, "minecraft:obsidian", 10, 1);
        } else if (tag.equals("vacuos")) {
            add(entries, "minecraft:bucket", 1, 1);
            add(entries, "minecraft:water_bucket", 1, 1);
            add(entries, "minecraft:lava_bucket", 1, 1);
            add(entries, "minecraft:milk_bucket", 1, 1);
        } else if (tag.equals("victus")) {
            add(entries, "minecraft:egg", 8, 1);
        } else if (tag.equals("vinculum")) {
            add(entries, "minecraft:cobweb", 4, 1);
        } else if (tag.equals("vitium")) {
            add(entries, "arcaneresearch:bottle_taint", 4, 1);
        } else if (tag.equals("vitreus")) {
            add(entries, "minecraft:diamond", 1, 1);
        }
        return entries;
    }

    private static String tag(Aspect aspect) {
        return aspect.getTag().toLowerCase(Locale.ROOT);
    }

    private static Item item(String id) {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(id));
        return item == Items.AIR ? null : item;
    }

    private static void add(List<Entry> entries, String id, int count, int weight) {
        Item item = item(id);
        if (item != null) {
            entries.add(new Entry(new ItemStack(item, count), weight));
        }
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(BuiltInRegistries.ITEM.key(), new ResourceLocation("c", path));
    }

    private static void addTag(List<Entry> entries, TagKey<Item> tag) {
        addTag(entries, tag, 1);
    }

    private static void addTag(List<Entry> entries, TagKey<Item> tag, int count) {
        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(holder ->
                entries.add(new Entry(new ItemStack(holder.value(), count), 1)));
    }
}
