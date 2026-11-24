package arcaneresearch.nbt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NBT: energized node aspect sync")
class EnergizedNodeSyncTest {

    @Test
    void aspectsSurviveUpdateTagRoundTrip() {
        TileNodeEnergized server = new TileNodeEnergized(null, BlockPos.ZERO, null);
        server.setAspects(new AspectList()
                .add(Aspect.AIR, 40)
                .add(Aspect.FIRE, 30)
                .add(Aspect.ORDER, 20));
        server.setupNode();

        CompoundTag update = server.getUpdateTag();
        assertTrue(update.contains("AEB"), "AEB key must be present in the update tag");
        assertTrue(update.getList("AEB", net.minecraft.nbt.Tag.TAG_COMPOUND).size() > 0);

        TileNodeEnergized client = new TileNodeEnergized(null, BlockPos.ZERO, null);
        client.load(update);

        AspectList restored = client.getAspects();
        assertEquals(server.getAspects().size(), restored.size(),
                "client must see the same aspects as the server");
        for (Aspect a : server.getAspects().getAspects()) {
            assertEquals(server.getAspects().getAmount(a), restored.getAmount(a));
        }
    }

    @Test
    void aspectsSurviveEvenWhenCustomAebKeyIsMissing() {
        // Simulates a client that received the aura ("Aspects") but never got
        // the derived vis list ("AEB"), e.g. because the BE update packet was
        // emitted before setupNode() ran during conversion.
        TileNodeEnergized server = new TileNodeEnergized(null, BlockPos.ZERO, null);
        server.setAspects(new AspectList()
                .add(Aspect.AIR, 40)
                .add(Aspect.FIRE, 30)
                .add(Aspect.ORDER, 20));
        server.setupNode();

        CompoundTag update = server.getUpdateTag();
        update.remove("AEB");

        TileNodeEnergized client = new TileNodeEnergized(null, BlockPos.ZERO, null);
        client.load(update);

        assertTrue(client.getAspects().size() > 0,
                "goggles popup must still have aspects when only the aura synced");
    }
}
