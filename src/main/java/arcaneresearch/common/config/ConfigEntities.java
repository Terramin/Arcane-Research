package arcaneresearch.common.config;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.registries.BuiltInRegistries; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.entities.monster.EntityPech;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.proxies.CommonProxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigEntities {

    public static final Map<Integer, List<TradeEntry>> tradeInventory = new HashMap<>();

    public static void initPechTrades() {
        tradeInventory.clear();
        EntityPech.addValuedItem(Items.ENDER_PEARL, 15);

        List<TradeEntry> forInv = new ArrayList<>();
        forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_IRON.get())));
        forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_GOLD.get())));
        forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_CINNABAR.get())));
        forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_QUARTZ.get())));
        if (ModConfig.foundCopperIngot) {
            forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_COPPER.get())));
        }
        if (ModConfig.foundTinIngot) {
            forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_TIN.get())));
        }
        if (ModConfig.foundSilverIngot) {
            forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_SILVER.get())));
        }
        if (ModConfig.foundLeadIngot) {
            forInv.add(entry(1, new ItemStack(ModItems.CLUSTER_LEAD.get())));
        }
        forInv.add(entry(2, new ItemStack(Items.BLAZE_ROD)));
        forInv.add(entry(2, new ItemStack(ModBlocks.GREATWOOD_SAPLING.get())));
        forInv.add(entry(2, new ItemStack(Items.DRAGON_BREATH)));
        forInv.add(entry(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forInv.add(entry(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forInv.add(entry(3, new ItemStack(Items.GOLDEN_APPLE)));
        forInv.add(entry(4, new ItemStack(ModItems.THAUMIUM_PICK.get())));
        forInv.add(entry(4, new ItemStack(ModItems.THAUMIUM_AXE.get())));
        forInv.add(entry(4, new ItemStack(ModItems.THAUMIUM_HOE.get())));
        forInv.add(entry(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
        forInv.add(entry(5, new ItemStack(ModBlocks.SILVERWOOD_SAPLING.get())));
        forInv.add(entry(5, new ItemStack(ModItems.CURIO_KNOWLEDGE.get())));

        tradeInventory.put(0, forInv);

        List<TradeEntry> forMag = new ArrayList<>();
        forMag.add(entry(1, ArcaneresearchApiHelper.makeCrystal(Aspect.AIR)));
        forMag.add(entry(1, ArcaneresearchApiHelper.makeCrystal(Aspect.EARTH)));
        forMag.add(entry(1, ArcaneresearchApiHelper.makeCrystal(Aspect.FIRE)));
        forMag.add(entry(1, ArcaneresearchApiHelper.makeCrystal(Aspect.WATER)));
        forMag.add(entry(1, ArcaneresearchApiHelper.makeCrystal(Aspect.ORDER)));
        forMag.add(entry(1, ArcaneresearchApiHelper.makeCrystal(Aspect.ENTROPY)));

        
        forMag.add(entry(2, PotionContents.createItemStack(Items.POTION, Potions.SWIFTNESS)));
        forMag.add(entry(2, PotionContents.createItemStack(Items.POTION, Potions.HEALING)));
        forMag.add(entry(2, ArcaneresearchApiHelper.makeCrystal(Aspect.FLUX)));
        forMag.add(entry(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forMag.add(entry(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forMag.add(entry(3, ArcaneresearchApiHelper.makeCrystal(Aspect.AURA)));
        forMag.add(entry(3, new ItemStack(Items.GOLDEN_APPLE)));
        forMag.add(entry(4, new ItemStack(ModItems.CLOTH_BOOTS.get())));
        forMag.add(entry(4, new ItemStack(ModItems.CLOTH_CHEST.get())));
        forMag.add(entry(4, new ItemStack(ModItems.CLOTH_LEGS.get())));
        forMag.add(entry(5, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
        forMag.add(entry(5, new ItemStack(ModItems.PECH_WAND.get())));
        forMag.add(entry(5, new ItemStack(ModItems.CURIO_KNOWLEDGE.get())));
        forMag.add(entry(5, new ItemStack(ModItems.AMULET_VIS_FOUND.get())));
        forInv.add(entry(5, new ItemStack(Items.TOTEM_OF_UNDYING)));
        tradeInventory.put(1, forMag);

        List<TradeEntry> forArc = new ArrayList<>();

        for (DyeColor dye : DyeColor.values()) {
            if (dye == DyeColor.WHITE) {
                continue;
            }
            forArc.add(entry(1, new ItemStack(arcaneresearch.api.blocks.BlocksTC.candles.get(dye).get())));
        }
        forArc.add(entry(2, new ItemStack(Items.GHAST_TEAR)));
        forInv.add(entry(2, new ItemStack(Items.COMPASS)));
        forArc.add(entry(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forArc.add(entry(3, new ItemStack(Items.EXPERIENCE_BOTTLE)));
        forArc.add(entry(3, new ItemStack(Items.GOLDEN_APPLE)));

        forArc.add(entry(4, new ItemStack(Items.ENDER_EYE)));
        forArc.add(entry(4, new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
        forInv.add(entry(4, new ItemStack(Items.SPECTRAL_ARROW)));
        forArc.add(entry(5, new ItemStack(ModItems.BAUBLE_RING_APPRENTICE.get())));
        
        forArc.add(entry(5, new ItemStack(ModItems.RUNIC_RING_LESSER.get())));
        forArc.add(entry(5, new ItemStack(ModItems.CURIO_KNOWLEDGE.get())));
        tradeInventory.put(2, forArc);

        Arcaneresearch.LOGGER.info("Registered pech trade tables ({} miner, {} mage, {} forager entries)",
                forInv.size(), forMag.size(), forArc.size());
    }

    private static TradeEntry entry(int cost, ItemStack stack) {
        return new TradeEntry(cost, stack);
    }

    
    
    
    
    
    
    
    private static final List<TradeEntry> bookTrades = new ArrayList<>();

    public static void initPechBookTrades(HolderLookup.Provider registries) {
        var lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        List<TradeEntry> forArc = tradeInventory.get(2);
        if (forArc == null) {
            return;
        }
        forArc.removeAll(bookTrades);
        bookTrades.clear();
        bookTrades.add(entry(2, enchantedBook(lookup, Enchantments.POWER, 1)));
        bookTrades.add(entry(5, enchantedBook(lookup, Enchantments.FLAME, 1)));
        bookTrades.add(entry(5, enchantedBook(lookup, Enchantments.INFINITY, 1)));
        forArc.addAll(bookTrades);
        Arcaneresearch.LOGGER.info("Registered pech enchanted-book trades");
    }

    private static ItemStack enchantedBook(HolderLookup.RegistryLookup<Enchantment> lookup,
            net.minecraft.resources.ResourceKey<Enchantment> key, int level) {
        Holder<Enchantment> holder = lookup.getOrThrow(key);
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(holder, level);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return book;
    }

    public static void postInitEntitySpawns() {
        ModConfig.updateHalloweenFlag();
        whitelistChampion("minecraft", "zombie", 0);
        whitelistChampion("minecraft", "spider", 0);
        whitelistChampion("minecraft", "blaze", 0);
        whitelistChampion("minecraft", "enderman", 0);
        whitelistChampion("minecraft", "skeleton", 0);
        whitelistChampion("minecraft", "witch", 1);
        whitelistChampion("arcaneresearch", "eldritch_crab", 0);
        whitelistChampion("arcaneresearch", "taintacle", 2);
        whitelistChampion("arcaneresearch", "inhabited_zombie", 3);
        Arcaneresearch.LOGGER.info("Registered {} champion whitelist entries", CommonProxy.championModWhitelist.size());
    }

    private static void whitelistChampion(String namespace, String path, int level) {
        try {
            Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
            
            
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)
                    && BuiltInRegistries.ENTITY_TYPE.getValue(id) instanceof EntityType) {
                CommonProxy.championModWhitelist.put(id, level);
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("Failed to whitelist [{}:{}] with level {}.", namespace, path, level);
        }
    }

    public record TradeEntry(int cost, ItemStack stack) {
    }
}
