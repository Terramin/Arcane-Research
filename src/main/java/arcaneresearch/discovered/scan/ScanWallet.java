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
        String key = aspect.getTag();
        if (key == null) {
            return 0;
        }
        return aspectsTag(player).getInt(key);
    }

    public static void add(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return;
        }
        String key = aspect.getTag();
        if (key == null) {
            return;
        }
        CompoundTag tags = aspectsTag(player);
        tags.putInt(key, tags.getInt(key) + amount);
    }

    public static int remove(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return 0;
        }
        String key = aspect.getTag();
        if (key == null) {
            return 0;
        }
        CompoundTag tags = aspectsTag(player);
        int have = tags.getInt(key);
        int take = Math.min(have, amount);
        if (take > 0) {
            int left = have - take;
            if (left > 0) {
                tags.putInt(key, left);
            } else {
                tags.remove(key);
            }
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
        if (player == null || key == null) {
            return;
        }
        CompoundTag root = root(player);
        ListTag list;
        if (root.contains(SCANNED, Tag.TAG_LIST)) {
            list = root.getList(SCANNED, Tag.TAG_STRING);
            for (Tag tag : list) {
                if (key.equals(tag.getAsString())) {
                    return;
                }
            }
        } else {
            list = new ListTag();
        }
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

    public static synchronized int viewAmount(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        String key = aspect.getTag();
        if (key == null) {
            return 0;
        }
        Integer amount = VIEW.get(key);
        return amount == null ? 0 : amount;
    }

    public static synchronized int viewBonus(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        String key = aspect.getTag();
        if (key == null) {
            return 0;
        }
        Integer amount = VIEW_BONUS.get(key);
        return amount == null ? 0 : amount;
    }
}