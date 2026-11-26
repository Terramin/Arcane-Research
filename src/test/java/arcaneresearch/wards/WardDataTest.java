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
    void connectedGroupFollowsFaceAdjacentWardsOfTheSameOwnerOnly() {
        Map<BlockPos, WardEntry> wards = new HashMap<>();
        BlockPos origin = new BlockPos(0, 0, 0);
        BlockPos east = new BlockPos(1, 0, 0);
        BlockPos above = new BlockPos(1, 1, 0);
        BlockPos chain = new BlockPos(2, 1, 0);
        BlockPos foreign = new BlockPos(1, 0, 1);
        BlockPos beyondForeign = new BlockPos(1, 0, 2);
        BlockPos isolated = new BlockPos(5, 5, 5);
        wards.put(origin, new WardEntry(ALICE, "Alice"));
        wards.put(east, new WardEntry(ALICE, "Alice"));
        wards.put(above, new WardEntry(ALICE, "Alice"));
        wards.put(chain, new WardEntry(ALICE, "Alice"));
        wards.put(foreign, new WardEntry(BOB, "Bob"));
        wards.put(beyondForeign, new WardEntry(ALICE, "Alice"));
        wards.put(isolated, new WardEntry(ALICE, "Alice"));

        Set<BlockPos> group = Set.copyOf(WardData.connectedGroup(wards, origin, ALICE, 64));
        assertEquals(Set.of(origin, east, above, chain), group);
        assertFalse(group.contains(foreign));
        assertFalse(group.contains(beyondForeign));
        assertFalse(group.contains(isolated));
        assertTrue(WardData.connectedGroup(wards, origin, BOB, 64).isEmpty());
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
