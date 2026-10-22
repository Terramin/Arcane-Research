package arcaneresearch.discovered.note;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import net.minecraft.core.registries.BuiltInRegistries;
import arcaneresearch.discovered.table.DiscoveredTableResearch;
import arcaneresearch.discovered.table.DiscoveredTableSetup;

 











 
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class DiscoveredNoteSetup {

    public static final String NOTE_ID = "research_notes";
    public static final String DISCOVERY_ID = "discovery";

    private static Item noteItem;
    private static Item discoveryItem;

    public static Item getNoteItem() {
        return noteItem != null ? noteItem : Item.byId(0);
    }

    public static Item getDiscoveryItem() {
        return discoveryItem != null ? discoveryItem : Item.byId(0);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) {
            
            
            Identifier noteId = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, NOTE_ID);
            Identifier discoveryId = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, DISCOVERY_ID);
            
            
            noteItem = new ItemDiscoveredResearchNote(new Item.Properties()
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    .setId(ResourceKey.create(Registries.ITEM, noteId)));
            Item noteForReg = noteItem;
            event.register(Registries.ITEM, noteId, () -> noteForReg);
            discoveryItem = new ItemDiscoveredResearchNote(new Item.Properties()
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)
                    .setId(ResourceKey.create(Registries.ITEM, discoveryId)));
            Item discoveryForReg = discoveryItem;
            event.register(Registries.ITEM, discoveryId, () -> discoveryForReg);
        }
    }

    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "arcaneresearch_tab"));
        if (event.getTabKey().equals(tab) && noteItem != null) {
            
            
            
            LinkedHashSet<String> keys = new LinkedHashSet<>(NOTE_KEYS);
            keys.addAll(DiscoveredTableResearch.discoveryKeys());
            for (String key : keys) {
                ItemStack note = makeNote(key);
                if (note != null) {
                    event.accept(note);
                }
            }
            if (DiscoveredTableSetup.getSingleTableItem() != null) {
                event.accept(DiscoveredTableSetup.getSingleTableItem());
            }
        }
    }

     


 
    private static final List<String> NOTE_KEYS = Arrays.asList(
            "ALUMENTUM", "ARCANESPA", "BATHSALTS", "BOOTSTRAVELLER", "CENTRIFUGE",
            "FOCUSADVANCED", "FOCUSBOLT", "FOCUSBREAK", "FOCUSCLOUD", "FOCUSCURSE",
            "FOCUSELEMENTAL", "FOCUSEXCHANGE", "FOCUSFLUX", "FOCUSGREATER", "FOCUSHEAL",
            "FOCUSMINE", "FOCUSPLAN", "FOCUSPROJECTILE", "FOCUSRIFT", "FOCUSSCATTER",
            "FOCUSSPELLBAT", "FOCUSSPLIT", "INFUSION", "INFUSIONBOOST", "INFUSIONSTABLE",
            "METALPURIFICATION", "POTIONSPRAYER", "RECHARGEPEDESTAL", "SANESOAP",
            "THAUMATORIUM", "VISAMULET", "VISBATTERY", "WORKBENCHCHARGER");

     



 
    private static ItemStack makeNote(String key) {
        AspectList required = DiscoveredTableResearch.requiredAspects(key);
        if (required == null || required.size() <= 0) {
            return null;
        }
        return DiscoveredNotes.createNote(key, required);
    }

    
    
    
    static int noteColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1 || !(stack.getItem() instanceof ItemDiscoveredResearchNote)) {
            return 0xFFFFFF;
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        for (int i = 0; i < data.tags.length; i++) {
            Aspect aspect = data.tags[i];
            int got = i < data.have.length ? data.have[i] : 0;
            if (aspect != null && got > 0) {
                return aspect.getColor();
            }
        }
        if (data.tags.length > 0 && data.tags[0] != null) {
            return data.tags[0].getColor();
        }
        return 0xFFFFFF;
    }
}
