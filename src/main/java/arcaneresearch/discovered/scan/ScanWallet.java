package arcaneresearch.discovered.scan;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.aspects.Aspect;
public final class ScanWallet {
    private static final String ROOT = "HexScanWallet";
    private static final String ASPECTS = "aspects";
    private static final String SCANNED = "scanned";
    private ScanWallet() {
    }
    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }
    private static CompoundTag aspectsTag(Player player) {
        CompoundTag root = root(player);
        if (!root.contains(ASPECTS, Tag.TAG_COMPOUND)) {
            root.put(ASPECTS, new CompoundTag());
        }
        return root.getCompound(ASPECTS);
    }
    public static int getAmount(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return 0;
        }
        try {
            return aspectsTag(player).getInt(aspect.getTag());
        } catch (Exception e) {
            return 0;
        }
    }
    public static void add(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return;
        }
        CompoundTag tags = aspectsTag(player);
        tags.putInt(aspect.getTag(), tags.getInt(aspect.getTag()) + amount);
    }
    public static int remove(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return 0;
        }
        CompoundTag tags = aspectsTag(player);
        int have = tags.getInt(aspect.getTag());
        int take = Math.min(have, amount);
        if (take > 0) {
            tags.putInt(aspect.getTag(), have - take);
        }
        return take;
    }
    public static boolean hasScanned(Player player, String key) {
        if (player == null || key == null) {
            return true;
        }
        CompoundTag root = root(player);
        if (!root.contains(SCANNED, Tag.TAG_LIST)) {
            return false;
        }
        for (Tag tag : root.getList(SCANNED, Tag.TAG_STRING)) {
            if (key.equals(tag.getAsString())) {
                return true;
            }
        }
        return false;
    }
    public static void markScanned(Player player, String key) {
        if (player == null || key == null || hasScanned(player, key)) {
            return;
        }
        CompoundTag root = root(player);
        ListTag list = root.contains(SCANNED, Tag.TAG_LIST)
                ? root.getList(SCANNED, Tag.TAG_STRING) : new ListTag();
        list.add(StringTag.valueOf(key));
        root.put(SCANNED, list);
    }
    public static java.util.Map<String, Integer> snapshot(Player player) {
        java.util.LinkedHashMap<String, Integer> out = new java.util.LinkedHashMap<>();
        if (player == null) {
            return out;
        }
        try {
            CompoundTag tags = aspectsTag(player);
            for (String key : tags.getAllKeys()) {
                int amount = tags.getInt(key);
                if (amount > 0) {
                    out.put(key, amount);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }
    private static final java.util.Map<String, Integer> VIEW = new java.util.HashMap<>();
    private static final java.util.Map<String, Integer> VIEW_BONUS = new java.util.HashMap<>();
    public static synchronized void applyView(java.util.Map<String, Integer> wallet,
            java.util.Map<String, Integer> bonus) {
        VIEW.clear();
        VIEW_BONUS.clear();
        if (wallet != null) {
            VIEW.putAll(wallet);
        }
        if (bonus != null) {
            VIEW_BONUS.putAll(bonus);
        }
    }
    public static int viewAmount(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        Integer amount = VIEW.get(aspect.getTag());
        return amount == null ? 0 : amount;
    }
    public static int viewBonus(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        Integer amount = VIEW_BONUS.get(aspect.getTag());
        return amount == null ? 0 : amount;
    }
}
