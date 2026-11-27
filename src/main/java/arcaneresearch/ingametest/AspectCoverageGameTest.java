package arcaneresearch.ingametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * In-game simulation that collects every vanilla item with no registered
 * aspects. Runs only via {@code ./gradlew runGameTestServer}; JUnit tests live
 * in {@code src/test/java} and are not affected by this class.
 */
@GameTestHolder(Arcaneresearch.MODID)
public final class AspectCoverageGameTest {

    /** Items that must have aspects after the tag-registration fix. */
    private static final ResourceLocation[] MUST_HAVE = {
            ResourceLocation.parse("minecraft:dirt"),
            ResourceLocation.parse("minecraft:stone"),
            ResourceLocation.parse("minecraft:cobblestone"),
            ResourceLocation.parse("minecraft:sand"),
            ResourceLocation.parse("minecraft:gravel"),
            ResourceLocation.parse("minecraft:iron_ingot"),
            ResourceLocation.parse("minecraft:gold_ingot"),
            ResourceLocation.parse("minecraft:copper_ingot"),
            ResourceLocation.parse("minecraft:redstone"),
            ResourceLocation.parse("minecraft:glowstone_dust"),
            ResourceLocation.parse("minecraft:oak_log"),
            ResourceLocation.parse("minecraft:leather")
    };

    static {
        // Forge scans and initializes GameTest classes before structures are
        // spawned, so redirect the vanilla template folder to our own.
        StructureUtils.testStructuresDir = "ingametest";
    }

    /** Technical entities that intentionally have no aspects. */
    private static final Set<String> ALLOWED_EMPTY_ENTITIES = new HashSet<>(Arrays.asList(
            "minecraft:area_effect_cloud", "minecraft:block_display", "minecraft:falling_block",
            "minecraft:interaction", "minecraft:item", "minecraft:item_display",
            "minecraft:lightning_bolt", "minecraft:marker", "minecraft:text_display",
            "minecraft:player", "minecraft:fishing_bobber"));

    private AspectCoverageGameTest() {
    }

    /**
     * Collects every vanilla item with no registered aspects and writes a report.
     * Fails when any item is still missing aspects.
     *
     * @param helper game-test helper used to report success or failure
     */
    @GameTest(template = "default_3x3x3", timeoutTicks = 20000)
    @PrefixGameTestTemplate(false)
    public static void collectVanillaItemsWithoutAspects(GameTestHelper helper) {
        int checked = 0;
        int empty = 0;
        StringBuilder all = new StringBuilder();

        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !"minecraft".equals(id.getNamespace())) {
                continue;
            }
            checked++;
            AspectList aspects = ArcaneresearchCraftingManager.getObjectTags(new ItemStack(item));
            if (aspects == null || aspects.size() == 0) {
                empty++;
                all.append(id).append(' ');
            }
        }

        for (ResourceLocation id : MUST_HAVE) {
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null || item == Items.AIR) {
                helper.fail("Missing vanilla item " + id);
                return;
            }
            AspectList aspects = ArcaneresearchCraftingManager.getObjectTags(new ItemStack(item));
            if (aspects == null || aspects.size() == 0) {
                helper.fail("No aspects for " + id + " — aspect registration is not applied");
                return;
            }
        }

        Arcaneresearch.LOGGER.info(
                "Aspect coverage: {} of {} vanilla items have no aspects.", empty, checked);
        Arcaneresearch.LOGGER.info("Items without aspects: {}", all.toString().trim());

        // Also write a plain-text report next to the run directory so it can be
        // diffed when coverage changes after a registration tweak.
        try {
            Files.writeString(Paths.get("aspect-coverage-missing.txt"),
                    "Checked " + checked + " vanilla items, " + empty + " without aspects:\n"
                            + all.toString().trim().replace(' ', '\n') + "\n");
        } catch (IOException e) {
            Arcaneresearch.LOGGER.warn("Could not write aspect coverage report", e);
        }

        if (empty > 0) {
            helper.fail(empty + " vanilla items still have no aspects; see run/aspect-coverage-missing.txt");
            return;
        }
        helper.succeed();
    }

    /**
     * Collects vanilla entities with no aspects and fails for anything outside
     * the technical whitelist.
     *
     * @param helper game-test helper used to report success or failure
     */
    @GameTest(template = "default_3x3x3", timeoutTicks = 20000)
    @PrefixGameTestTemplate(false)
    public static void collectEntitiesWithoutAspects(GameTestHelper helper) {
        int checked = 0;
        int empty = 0;
        Set<String> missing = new HashSet<>();
        StringBuilder all = new StringBuilder();

        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id == null || !"minecraft".equals(id.getNamespace())) {
                continue;
            }
            checked++;
            if (AspectHelper.getEntityAspects(id) == null) {
                empty++;
                missing.add(id.toString());
                all.append(id).append(' ');
            }
        }

        Arcaneresearch.LOGGER.info(
                "Entity aspect coverage: {} of {} vanilla entities have no aspects.", empty, checked);
        Arcaneresearch.LOGGER.info("Entities without aspects: {}", all.toString().trim());
        try {
            Files.writeString(Paths.get("entity-aspect-coverage-missing.txt"),
                    "Checked " + checked + " vanilla entities, " + empty + " without aspects:\n"
                            + all.toString().trim().replace(' ', '\n') + "\n");
        } catch (IOException e) {
            Arcaneresearch.LOGGER.warn("Could not write entity aspect coverage report", e);
        }

        Set<String> unexpected = new HashSet<>(missing);
        unexpected.removeAll(ALLOWED_EMPTY_ENTITIES);
        if (!unexpected.isEmpty()) {
            helper.fail("Entities without aspects outside the technical whitelist: " + unexpected);
            return;
        }
        helper.succeed();
    }
}
