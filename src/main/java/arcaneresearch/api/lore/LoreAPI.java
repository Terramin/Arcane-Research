package arcaneresearch.api.lore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class LoreAPI {

    public static final String HOLD_SHIFT_KEY = "lore.arcaneresearch.hold_shift";

    private static final Map<Item, String> LORE_KEYS = new ConcurrentHashMap<>();

    private LoreAPI() {
    }

    public static void register(ItemLike item, String loreKey) {
        LORE_KEYS.put(item.asItem(), loreKey);
    }

    public static boolean hasLore(ItemStack stack) {
        return !stack.isEmpty() && LORE_KEYS.containsKey(stack.getItem());
    }

    @Nullable
    public static String getLoreKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return LORE_KEYS.get(stack.getItem());
    }

    @Nullable
    public static String getLoreKey(Item item) {
        return LORE_KEYS.get(item);
    }
}
