package arcaneresearch.common.lib.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import arcaneresearch.common.lib.utils.RandomItemChooser.Item;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RandomItemChooser: взвешенный выбор с управляемым источником случайности")
class RandomItemChooserTest {

    private static final Item FIRST = () -> 1.0;
    private static final Item SECOND = () -> 3.0;

    @Test
    void choosesItemContainingTheRandomRoll() {
        List<Item> items = List.of(FIRST, SECOND);

        assertEquals(FIRST, chooserWithRoll(0.0).chooseOnWeight(items));
        assertEquals(FIRST, chooserWithRoll(0.24).chooseOnWeight(items));
        // total weight = 4: 0.25 lands exactly on the first item's upper bound.
        assertEquals(FIRST, chooserWithRoll(0.25).chooseOnWeight(items));
        assertEquals(SECOND, chooserWithRoll(0.250001).chooseOnWeight(items));
        assertEquals(SECOND, chooserWithRoll(0.9999).chooseOnWeight(items));
    }

    @Test
    void zeroAndNegativeWeightsDoNotBreakTheCumulativeSum() {
        Item empty = () -> 0.0;
        assertEquals(empty, chooserWithRoll(0.0).chooseOnWeight(List.of(empty, SECOND)));

        Item negative = () -> -1.0;
        assertEquals(SECOND, chooserWithRoll(0.5).chooseOnWeight(List.of(negative, SECOND)));
    }

    @Test
    void emptyListHasNoCandidate() {
        assertThrows(RuntimeException.class, () -> chooserWithRoll(0.5).chooseOnWeight(List.of()));
    }

    @Test
    void defaultConstructorStillUsesTheGlobalRandomSource() {
        assertNotNull(new RandomItemChooser().chooseOnWeight(List.of(FIRST, SECOND)));
    }

    /** A source with a fixed roll makes boundary cases deterministic. */
    private static RandomItemChooser chooserWithRoll(double roll) {
        return new RandomItemChooser(new Random() {
            @Override
            public double nextDouble() {
                return roll;
            }
        });
    }
}
