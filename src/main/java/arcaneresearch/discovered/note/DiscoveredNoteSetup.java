package arcaneresearch.discovered.note;

import arcaneresearch.helper.IDsHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.table.DiscoveredTableResearch;
import arcaneresearch.discovered.table.DiscoveredTableSetup;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
        if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
            event.register(ForgeRegistries.Keys.ITEMS,
                    IDsHelper.mod(NOTE_ID),
                    ItemDiscoveredResearchNote::new);
            event.register(ForgeRegistries.Keys.ITEMS,
                    IDsHelper.mod(DISCOVERY_ID),
                    ItemDiscoveredResearchNote::new);
            noteItem = ForgeRegistries.ITEMS.getValue(IDsHelper.mod(NOTE_ID));
            discoveryItem = ForgeRegistries.ITEMS.getValue(IDsHelper.mod(DISCOVERY_ID));
        }
    }

    @SubscribeEvent
    public static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                IDsHelper.mod("arcaneresearch_tab"));
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
        ItemStack stack = DiscoveredNotes.createNote(key, required);
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        data.revealed = true;
        DiscoveredNoteData.write(stack, data);
        return stack;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        if (noteItem == null) {
            return;
        }
        net.minecraft.client.color.item.ItemColor tint = DiscoveredNoteSetup::noteColor;
        event.register(tint, noteItem);
        if (discoveryItem != null) {
            event.register(tint, discoveryItem);
        }
    }

    private static int noteColor(ItemStack stack, int tintIndex) {
        try {
            return arcaneresearch.helper.TintHelper.researchNoteColor(
                    stack, tintIndex, DiscoveredNoteData.read(stack));
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }

    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD,
            value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                if (noteItem == null) {
                    return;
                }
                ItemProperties.register(noteItem,
                        IDsHelper.mod("complete"),
                        (stack, level, entity, seed) ->
                                DiscoveredNoteData.read(stack).isComplete() ? 1.0F : 0.0F);
            });
        }
    }
}
