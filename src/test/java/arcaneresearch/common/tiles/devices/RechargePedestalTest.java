package arcaneresearch.common.tiles.devices;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Recharge pedestal: casting tools charge at twice the hand rate")
class RechargePedestalTest {
    private static final Path JAVA = Path.of(System.getProperty(
            "arcaneresearch.resources.dir", "src/main/resources")).getParent().resolve("java");

    private static String source(String relative) throws Exception {
        return Files.readString(JAVA.resolve("arcaneresearch/" + relative), StandardCharsets.UTF_8);
    }

    @Test
    void pedestalAcceptsWandsAndCasters() throws Exception {
        String tile = source("common/tiles/devices/TileRechargePedestal.java");
        assertTrue(tile.contains("stack.getItem() instanceof ItemWand"));
        assertTrue(tile.contains("stack.getItem() instanceof ItemCaster"));
        String block = source("common/blocks/devices/BlockRechargePedestal.java");
        assertTrue(block.contains("TileRechargePedestal.canCharge(heldItem)"));
    }

    @Test
    void genericRechargableItemsAreStillAccepted() throws Exception {
        String tile = source("common/tiles/devices/TileRechargePedestal.java");
        assertTrue(tile.contains("stack.getItem() instanceof IRechargable"));
        assertTrue(tile.contains("public static boolean canCharge(ItemStack stack)"));
    }

    @Test
    void castingToolsUseTheDoubledHandRate() throws Exception {
        String tile = source("common/tiles/devices/TileRechargePedestal.java");
        assertTrue(tile.contains("HAND_PERIOD = 5"));
        assertTrue(tile.contains("PEDESTAL_MULTIPLIER = 2"));
        assertTrue(tile.contains("HAND_RATE * PEDESTAL_MULTIPLIER"));
        // Ordinary IRechargable items keep their original slow recharge path.
        assertTrue(tile.contains("rechargeItem(level, stack, pos, null, 5) > 0"));
    }
}
