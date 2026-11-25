package arcaneresearch.wards;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.common.wards.WardData;
import arcaneresearch.common.wards.WardEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Укреплённые блоки: чистые данные NBT и правила владельца")
class WardDataTest {
    private static final UUID ALICE = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID BOB = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    @Test
    void wardsSurviveAnNbtRoundTripWithOwnerAndPosition() {
        Map<BlockPos, WardEntry> original = new HashMap<>();
        original.put(new BlockPos(12, -4, 7), new WardEntry(ALICE, "Alice"));
        original.put(new BlockPos(-30, 64, 9), new WardEntry(BOB, "Bob"));

        CompoundTag tag = WardData.serialize(original);
        Map<BlockPos, WardEntry> restored = WardData.deserialize(tag);

        assertEquals(original, restored);
        assertEquals(ALICE, UUID.fromString(restored.get(new BlockPos(12, -4, 7)).getOwnerUuid()));
        assertEquals("Bob", restored.get(new BlockPos(-30, 64, 9)).getOwnerName());
    }

    @Test
    void deserializeToleratesMissingOrEmptyData() {
        assertTrue(WardData.deserialize(null).isEmpty());
        assertTrue(WardData.deserialize(new CompoundTag()).isEmpty());
        assertEquals(new WardEntry("", ""), WardEntry.fromNbt(null));
    }

    @Test
    void onlyTheOwnerCanLiftAWardAndNobodyCanOverwriteIt() {
        Map<BlockPos, WardEntry> wards = new HashMap<>();
        BlockPos pos = new BlockPos(1, 2, 3);

        assertTrue(WardData.canAdd(wards, pos, ALICE));
        wards.put(pos, new WardEntry(ALICE, "Alice"));

        assertFalse(WardData.canAdd(wards, pos, BOB));
        assertFalse(WardData.canRemove(wards, pos, BOB));
        assertTrue(WardData.canRemove(wards, pos, ALICE));
        assertTrue(wards.get(pos).isOwnedBy(ALICE));
        assertFalse(wards.get(pos).isOwnedBy(BOB));
    }

    @Test
    void explosionsNeverIncludeWardedCoordinates() {
        List<BlockPos> affected = List.of(new BlockPos(0, 0, 0), new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0));
        Set<BlockPos> protectedPositions = Set.of(new BlockPos(1, 0, 0));

        assertEquals(List.of(new BlockPos(0, 0, 0), new BlockPos(2, 0, 0)),
                WardData.filterProtected(affected, protectedPositions));
    }
}
