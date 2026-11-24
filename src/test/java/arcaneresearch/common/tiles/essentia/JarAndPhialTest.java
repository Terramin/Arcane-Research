package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.tiles.aura.TileJarNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Jars and phials")
class JarAndPhialTest {
    private static TileJar jar() {
        return new TileJar(null, BlockPos.ZERO, null);
    }

    @Test
    void jarAcceptsOnlyItsAspectAndClampsToCapacity() {
        TileJar jar = jar();
        assertEquals(0, jar.getAmount());
        assertNull(jar.getAspect());

        assertEquals(0, jar.addToContainer(Aspect.FIRE, 10));
        assertSame(Aspect.FIRE, jar.getAspect());
        assertEquals(10, jar.getAmount());

        assertEquals(10, jar.addToContainer(Aspect.WATER, 10));
        assertEquals(10, jar.getAmount());

        assertEquals(60, jar.addToContainer(Aspect.FIRE, 300));
        assertEquals(TileJar.CAPACITY, jar.getAmount());

        assertTrue(jar.takeFromContainer(Aspect.FIRE, TileJar.CAPACITY));
        assertEquals(0, jar.getAmount());
        assertNull(jar.getAspect());
    }

    @Test
    void jarFilterIsCheckedByCallers() {
        TileJar jar = jar();
        jar.setAspectFilter(Aspect.WATER);

        assertFalse(jar.doesContainerAccept(Aspect.FIRE));
        assertTrue(jar.doesContainerAccept(Aspect.WATER));
        assertEquals(0, jar.addToContainer(Aspect.WATER, 5));
        assertSame(Aspect.WATER, jar.getAspect());
        // A filled jar refuses a different aspect at the tile level too.
        assertEquals(5, jar.addToContainer(Aspect.FIRE, 5));
        assertEquals(5, jar.getAmount());
    }

    @Test
    void jarNbtKeepsFacingAndFills() {
        TileJar jar = jar();
        jar.setFacing(5);
        jar.addToContainer(Aspect.ENTROPY, 42);

        CompoundTag tag = jar.getUpdateTag();
        assertEquals(5, tag.getInt("facing"));
        assertEquals(42, tag.getInt("Amount"));
        assertEquals(Aspect.ENTROPY.getTag(), tag.getString("Aspect"));

        TileJar copy = jar();
        tag.putInt("Amount", 9999);
        copy.load(tag);
        assertEquals(TileJar.CAPACITY, copy.getAmount());
    }

    @Test
    void voidJarClampsToCapacity() {
        TileJarVoid jar = new TileJarVoid(null, BlockPos.ZERO, null);
        assertEquals(0, jar.addToContainer(Aspect.FIRE, 10_000));
        assertEquals(TileJar.CAPACITY, jar.getAmount());
        assertSame(Aspect.FIRE, jar.getAspect());

        assertEquals(7, jar.addToContainer(Aspect.WATER, 7));
        assertEquals(TileJar.CAPACITY, jar.getAmount());
    }

    @Test
    void auraJarNodeKeepsEssentiaWithinItsBase() {
        TileJarNode node = new TileJarNode(null, BlockPos.ZERO, null);
        node.setAspects(new AspectList().add(Aspect.WATER, 10));
        assertEquals(10, node.getAspectsBase().getAmount(Aspect.WATER));

        assertTrue(node.takeFromContainer(Aspect.WATER, 4));
        assertEquals(0, node.addToContainer(Aspect.WATER, 4));
        assertEquals(10, node.getAspects().getAmount(Aspect.WATER));
        assertEquals(5, node.addToContainer(Aspect.WATER, 5));
        assertEquals(10, node.getAspects().getAmount(Aspect.WATER));
    }

    @Test
    void phialStorageRoundTripsThroughNbt() {
        // Compile-time constant: does not load the item class or registries.
        assertEquals(10, arcaneresearch.common.items.consumables.ItemPhial.PHIAL_CAPACITY);

        AspectList stored = new AspectList().add(Aspect.WATER, 10);
        CompoundTag tag = new CompoundTag();
        stored.writeToNBT(tag);

        AspectList restored = new AspectList();
        restored.readFromNBT(tag);
        assertEquals(1, restored.size());
        assertEquals(10, restored.getAmount(Aspect.WATER));
    }

    @Test
    void jarFacingUsesLegacyOrdinalSides() {
        TileJar jar = jar();
        assertSame(Direction.NORTH, Direction.values()[jar.getFacing()]);
        jar.setFacing(4);
        assertSame(Direction.WEST, Direction.values()[jar.getFacing()]);
    }
}
