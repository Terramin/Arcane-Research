package arcaneresearch.common.lib.utils;

import java.util.List;
import java.util.Random;

public class RandomItemChooser {
    private final Random random;

    /** Uses the global random source (legacy behaviour). */
    public RandomItemChooser() {
        this(null);
    }

    /** Uses an explicit source so selection is reproducible in tests and simulations. */
    public RandomItemChooser(Random random) {
        this.random = random;
    }

    public Item chooseOnWeight(List<Item> items) {
        double completeWeight = 0.0;
        for (Item item : items) {
            completeWeight += item.getWeight();
        }
        double r = nextRandom() * completeWeight;
        double countWeight = 0.0;
        for (Item item2 : items) {
            countWeight += item2.getWeight();
            if (countWeight >= r) {
                return item2;
            }
        }
        throw new RuntimeException("Should never be shown.");
    }

    private double nextRandom() {
        return random != null ? random.nextDouble() : Math.random();
    }

    public interface Item {
        double getWeight();
    }
}
