package arcaneresearch.api.internal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.item.ItemStack;
public class WeightedRandomLoot {
    public ItemStack item;
    public final int itemWeight;
    public WeightedRandomLoot(ItemStack stack, int weight) {
        this.item = stack;
        this.itemWeight = weight;
    }
    public int getWeight() {
        return itemWeight;
    }
    public static List<WeightedRandomLoot> lootBagCommon = new ArrayList<>();
    public static List<WeightedRandomLoot> lootBagUncommon = new ArrayList<>();
    public static List<WeightedRandomLoot> lootBagRare = new ArrayList<>();
    public static WeightedRandomLoot getRandomItem(List<WeightedRandomLoot> list, Random random) {
        if (list.isEmpty()) {
            return null;
        }
        int totalWeight = 0;
        for (WeightedRandomLoot entry : list) {
            totalWeight += entry.itemWeight;
        }
        if (totalWeight <= 0) {
            return null;
        }
        int selected = random.nextInt(totalWeight);
        int currentWeight = 0;
        for (WeightedRandomLoot entry : list) {
            currentWeight += entry.itemWeight;
            if (selected < currentWeight) {
                return entry;
            }
        }
        return list.get(list.size() - 1);
    }
}
