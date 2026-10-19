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

/**
 * Автоподключение записки без правок вне {@code temp}:
 * регистрация предмета, вкладка креатива и тинт иконки.
 * Паритет тинта: TC3 ItemResearchNotes (второй проход цветом аспекта)
 * + reforged ResearchNoteTint (цвет первичного тега).
 *
 * NEOFORGE_PORT: net.minecraftforge.* -> net.neoforged.* (EventBusSubscriber,
 * RegisterEvent с тем же register(ResourceKey, Identifier, Supplier));
 * ForgeRegistries.Keys.ITEMS -> Registries.ITEM, значения через
 * BuiltInRegistries.ITEM; ResourceLocation -> Identifier.
 * Тинт иконки отложен в ItemTintSource-дата (как остальные тинты, см. главный
 * onRegisterItemColors TODO) — RegisterColorHandlersEvent.Item удалён в 26.3.
 */
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
            // NEOFORGE_PORT (26.3): direct RegisterEvent does not inject the
            // item id; set it explicitly.
            Identifier noteId = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, NOTE_ID);
            Identifier discoveryId = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, DISCOVERY_ID);
            noteItem = new ItemDiscoveredResearchNote(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, noteId)));
            Item noteForReg = noteItem;
            event.register(Registries.ITEM, noteId, () -> noteForReg);
            discoveryItem = new ItemDiscoveredResearchNote(new Item.Properties()
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
            // Только записки под открытия, требуемые исследованиями проекта:
            // скан загруженных стадий + статический список на случай,
            // если категории ещё не загружены. Пустых записок нет.
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

    /**
     * Ключи открытий из required_discovery исследований проекта
     * (статический список — страховка, динамику добирает скан стадий).
     */
    private static final List<String> NOTE_KEYS = Arrays.asList(
            "ALUMENTUM", "ARCANESPA", "BATHSALTS", "BOOTSTRAVELLER", "CENTRIFUGE",
            "FOCUSADVANCED", "FOCUSBOLT", "FOCUSBREAK", "FOCUSCLOUD", "FOCUSCURSE",
            "FOCUSELEMENTAL", "FOCUSEXCHANGE", "FOCUSFLUX", "FOCUSGREATER", "FOCUSHEAL",
            "FOCUSMINE", "FOCUSPLAN", "FOCUSPROJECTILE", "FOCUSRIFT", "FOCUSSCATTER",
            "FOCUSSPELLBAT", "FOCUSSPLIT", "INFUSION", "INFUSIONBOOST", "INFUSIONSTABLE",
            "METALPURIFICATION", "POTIONSPRAYER", "RECHARGEPEDESTAL", "SANESOAP",
            "THAUMATORIUM", "VISAMULET", "VISBATTERY", "WORKBENCHCHARGER");

    /**
     * Записка под ключ открытия: точный набор или формула категории
     * (для ключа без своего исследования — категория требующего исследования).
     * Нет аспектов — записки нет (не тихая заглушка).
     */
    private static ItemStack makeNote(String key) {
        AspectList required = DiscoveredTableResearch.requiredAspects(key);
        if (required == null || required.size() <= 0) {
            return null;
        }
        return DiscoveredNotes.createNote(key, required);
    }

    // DONE (tint rework): тинт записки/открытия — client/renderers/item/NoteTintSources
    // (arcaneresearch:note, @tint1), паритет цвета — TintHelper.researchNoteColor.
    // Локальный noteColor оставлен как прямое зеркало Forge (тесты паритета).
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
