package arcaneresearch.api.research;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.concurrent.ConcurrentHashMap;
public class ScanOreDictionary implements IScanThing {
    private final String research;
    private final String[] entries;
    private final ConcurrentHashMap<Integer, Boolean> cache = new ConcurrentHashMap<>();
    public ScanOreDictionary(String research, String... entries) {
        this.research = research;
        this.entries = entries;
    }
    @Override
    public boolean checkThing(Player player, Object obj) {
        ItemStack stack = null;
        if (obj != null) {
            if (obj instanceof BlockPos pos) {
                BlockState state = player.level().getBlockState(pos);
                stack = state.getBlock().getCloneItemStack(player.level(), pos, state);
            } else if (obj instanceof ItemStack) {
                stack = (ItemStack) obj;
            } else if (obj instanceof ItemEntity itemEntity && !itemEntity.getItem().isEmpty()) {
                stack = itemEntity.getItem();
            }
        }
        if (stack != null && !stack.isEmpty()) {
            int hid = stack.hashCode();
            if (cache.containsKey(hid)) {
                return cache.get(hid);
            }
            var tags = ForgeRegistries.ITEMS.tags();
            for (String entry : entries) {
                var tagKey = net.minecraft.tags.TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),
                    new net.minecraft.resources.ResourceLocation(translateEntry(entry)));
                if (stack.is(tagKey)) {
                    cache.put(hid, true);
                    return true;
                }
            }
            cache.put(hid, false);
        }
        return false;
    }
    static String translateEntry(String entry) {
        if (entry.contains(":") || entry.contains("/")) {
            return entry;
        }
        String[][] prefixes = {
            { "ore", "ores" }, { "ingot", "ingots" }, { "block", "storage_blocks" },
            { "plate", "plates" }, { "gem", "gems" }, { "dust", "dusts" },
            { "nugget", "nuggets" }
        };
        for (String[] prefix : prefixes) {
            if (entry.startsWith(prefix[0]) && entry.length() > prefix[0].length()) {
                String rest = entry.substring(prefix[0].length());
                return "forge:" + prefix[1] + "/" + Character.toLowerCase(rest.charAt(0)) + rest.substring(1);
            }
        }
        return "forge:" + entry;
    }
    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}