package arcaneresearch.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.gui.GuiSources.Registration;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Wiring tests for every block entity that has a GUI. Minecraft is never booted:
 * registries, menu constructors and screen bindings are verified from source.
 */
@DisplayName("GUI блоковых сущностей: сквозная связанность")
class GuiBlockEntityWiringTest {

    private record GuiTile(String tile, String menu, String field, String id, String screen,
                           String menuTileParameter) {
        @Override
        public String toString() {
            return tile + " -> " + menu;
        }
    }

    private static Stream<Arguments> guiTiles() {
        return Stream.of(
                new GuiTile("AuraTotemTile", "AuraTotemMenu", "AURA_TOTEM", "aura_totem", "AuraTotemScreen", "AuraTotemTile"),
                new GuiTile("DeconstructTile", "DeconstructMenu", "DECONSTRUCTION_TABLE", "deconstruction_table", "DeconstructScreen", "DeconstructTile"),
                new GuiTile("TileArcaneWorkbench", "ArcaneWorkbenchMenu", "ARCANE_WORKBENCH", "arcane_workbench", "ArcaneWorkbenchScreen", "TileArcaneWorkbench"),
                new GuiTile("TileGolemBuilder", "GolemBuilderMenu", "GOLEM_BUILDER", "golem_builder", "GolemBuilderScreen", "TileGolemBuilder"),
                new GuiTile("TileResearchTable", "ResearchTableMenu", "RESEARCH_TABLE", "research_table", "ResearchTableScreen", "TileResearchTable"),
                new GuiTile("TileSmelter", "SmelterMenu", "SMELTER", "smelter", "SmelterScreen", "TileSmelter"),
                new GuiTile("TileThaumatorium", "ThaumatoriumMenu", "THAUMATORIUM", "thaumatorium", "ThaumatoriumScreen", "TileThaumatorium"),
                new GuiTile("TileThaumatoriumTop", "ThaumatoriumMenu", "THAUMATORIUM", "thaumatorium", "ThaumatoriumScreen", "TileThaumatorium"),
                new GuiTile("TileFocalManipulator", "FocalManipulatorMenu", "FOCAL_MANIPULATOR", "focal_manipulator", "FocalManipulatorScreen", "TileFocalManipulator"),
                new GuiTile("TileHungryChest", "HungryChestMenu", "HUNGRY_CHEST", "hungry_chest", "HungryChestScreen", "TileHungryChest"),
                new GuiTile("TilePotionSprayer", "PotionSprayerMenu", "POTION_SPRAYER", "potion_sprayer", "PotionSprayerScreen", "TilePotionSprayer"),
                new GuiTile("TileSpa", "SpaMenu", "SPA", "spa", "SpaScreen", "TileSpa"),
                new GuiTile("TileVoidSiphon", "VoidSiphonMenu", "VOID_SIPHON", "void_siphon", "VoidSiphonScreen", "TileVoidSiphon"))
                .map(Arguments::of);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void blockEntityIsRegistered(GuiTile gui) {
        assertTrue(GuiSources.hasBlockEntity(gui.tile()), gui.tile() + " is not a registered block entity");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void menuTypeIsRegisteredWithTheExpectedId(GuiTile gui) {
        String source = GuiSources.compact(GuiSources.source("arcaneresearch/init/ModMenuTypes.java"));
        // DeferredHolder<MenuType<?>, MenuType<XMenu>> FIELD = MENU_TYPES.register("id", ...)
        Pattern pattern = Pattern.compile("MenuType<[\\w.]*" + gui.menu() + ">\\s*>\\s+"
                + gui.field() + "\\s*=\\s*MENU_TYPES\\.register\\(\"" + gui.id() + "\"");

        assertTrue(pattern.matcher(source).find(), "missing menu registration " + gui.id());
        assertEquals(1, GuiSources.menus().stream().filter(reg -> reg.id().equals(gui.id())).count());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void menuHasClientAndTileConstructors(GuiTile gui) {
        String menu = GuiSources.source("arcaneresearch/common/menu", gui.menu());

        assertTrue(GuiSources.hasConstructor(menu, gui.menu(), "FriendlyByteBuf"),
                gui.menu() + " needs a (int, Inventory, FriendlyByteBuf) constructor");
        assertTrue(GuiSources.hasConstructor(menu, gui.menu(), gui.menuTileParameter()),
                gui.menu() + " needs a (int, Inventory, " + gui.menuTileParameter() + ") constructor");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void menuUsesItsOwnRegisteredType(GuiTile gui) {
        String menu = GuiSources.compact(GuiSources.source("arcaneresearch/common/menu", gui.menu()));

        assertTrue(menu.contains("super(ModMenuTypes." + gui.field() + ".get(), containerId)"),
                gui.menu() + " does not use ModMenuTypes." + gui.field());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void screenExistsAndIsRegistered(GuiTile gui) {
        String screen = GuiSources.source("arcaneresearch/client/gui/screens", gui.screen());
        assertFalse(screen.isBlank());

        String mod = GuiSources.compact(GuiSources.source("arcaneresearch/Arcaneresearch.java"));
        // NeoForge binds screens in RegisterMenuScreensEvent#onRegisterMenuScreens.
        Pattern registration = Pattern.compile("event\\.register\\(\\s*(?:[\\w.]+\\.)?ModMenuTypes\\."
                + gui.field() + "\\.get\\(\\)\\s*,\\s*[\\w.]*\\." + gui.screen() + "::new\\s*\\)");

        assertTrue(registration.matcher(mod).find(), gui.screen() + " is not bound to " + gui.field());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void tileOrItsBlockOpensTheMenu(GuiTile gui) {
        String tile = GuiSources.tileSource(gui.tile());
        Pattern creator = Pattern.compile("new\\s+[\\w.]*" + gui.menu() + "\\s*\\(");
        boolean fromTile = tile.contains("createMenu") && creator.matcher(tile).find();
        boolean fromBlock = opensFromAnyBlock(gui.menu());

        assertTrue(fromTile || fromBlock, "nothing creates " + gui.menu() + " for " + gui.tile());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("guiTiles")
    void displayNameIsTranslated(GuiTile gui) {
        Set<String> english = GuiSources.englishKeys();
        Pattern translation = Pattern.compile("translatable\\(\\s*\"([^\"]+)\"");
        boolean found = containsTranslatedKey(translation, GuiSources.tileSource(gui.tile()), english)
                || opensFromAnyBlockMenuSource(translation, english);
        assertTrue(found, gui.tile() + " has no translated display name");
    }

    @Test
    void everyGuiBlockEntityIsCoveredByTheMatrix() {
        Set<String> actual = GuiSources.allGuiTileNames();
        Set<String> covered = guiTiles().map(args -> ((GuiTile) args.get()[0]).tile()).toList().stream()
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(covered, actual,
                "GUI tile list is out of date; add/remove entries in guiTiles()");
    }

    @Test
    void everyRegisteredMenuTypeHasAScreen() {
        for (Registration menu : GuiSources.menus()) {
            String screen = "arcaneresearch/client/gui/screens/"
                    + menu.type().replace("Menu", "Screen") + ".java";
            assertTrue(GuiSources.sourceExists(screen), "no screen source for " + menu.type());
        }
    }

    private static boolean opensFromAnyBlock(String menu) {
        Pattern creator = Pattern.compile("new\\s+[\\w.]*" + menu + "\\s*\\(");
        return blockSources().stream().anyMatch(source -> creator.matcher(source).find());
    }

    private static boolean opensFromAnyBlockMenuSource(Pattern translation, Set<String> english) {
        return blockSources().stream().anyMatch(source -> containsTranslatedKey(translation, source, english));
    }

    private static boolean containsTranslatedKey(Pattern translation, String source, Set<String> english) {
        var matcher = translation.matcher(source);
        while (matcher.find()) {
            if (english.contains(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> blockSources() {
        return List.of(
                "arcaneresearch/common/blocks/aura/AuraTotemBlock.java",
                "arcaneresearch/common/blocks/crafting/BlockArcaneWorkbenchCharger.java",
                "arcaneresearch/common/blocks/crafting/BlockFocalManipulator.java",
                "arcaneresearch/common/blocks/crafting/DeconstructBlock.java",
                "arcaneresearch/common/blocks/devices/BlockHungryChest.java",
                "arcaneresearch/common/blocks/devices/BlockPotionSprayer.java",
                "arcaneresearch/common/blocks/devices/BlockSpa.java",
                "arcaneresearch/common/blocks/devices/BlockVoidSiphon.java")
                .stream().map(GuiSources::source).toList();
    }
}
