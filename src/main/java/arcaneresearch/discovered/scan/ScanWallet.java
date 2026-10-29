package arcaneresearch.discovered.scan;

import net.minecraft.nbt.CompoundTag;
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
        CompoundTag sub = persistent.getCompound(ROOT).orElse(null);
        if (sub == null) {
            sub = new CompoundTag();
            persistent.put(ROOT, sub);
        }
        return sub;
    }

    private static CompoundTag aspectsTag(Player player) {
        CompoundTag root = root(player);
        CompoundTag sub = root.getCompound(ASPECTS).orElse(null);
        if (sub == null) {
            sub = new CompoundTag();
            root.put(ASPECTS, sub);
        }
        return sub;
    }

    private static CompoundTag scannedTag(Player player) {
        CompoundTag root = root(player);
        CompoundTag sub = root.getCompound(SCANNED).orElse(null);
        if (sub == null) {
            sub = new CompoundTag();
            root.put(SCANNED, sub);
        }
        return sub;
    }


    public static int getAmount(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return 0;
        }
        String key = aspect.getTag();
        if (key == null) {
            return 0;
        }
        try {
            return aspectsTag(player).getIntOr(key, 0);
        } catch (Exception e) {
            return 0;
        }
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
        tags.putInt(key, tags.getIntOr(key, 0) + amount);
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
        int have = tags.getIntOr(key, 0);
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
        try {
            CompoundTag root = root(player);
            CompoundTag scanned = root.getCompound(SCANNED).orElse(null);
            return scanned != null && scanned.getBooleanOr(key, false);
        } catch (Exception e) {
            return true;
        }
    }


    public static void markScanned(Player player, String key) {
        if (player == null || key == null || hasScanned(player, key)) {
            return;
        }
        scannedTag(player).putBoolean(key, true);
    }


    public static java.util.Map<String, Integer> snapshot(Player player) {
        java.util.LinkedHashMap<String, Integer> out = new java.util.LinkedHashMap<>();
        if (player == null) {
            return out;
        }
        try {
            CompoundTag tags = aspectsTag(player);
            for (String key : tags.keySet()) {
                int amount = tags.getIntOr(key, 0);
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
            for (java.util.Map.Entry<String, Integer> e : wallet.entrySet()) {
                String k = e.getKey();
                Integer v = e.getValue();
                if (k != null && v != null && v > 0) {
                    VIEW.put(k, v);
                }
            }
        }
        if (bonus != null) {
            for (java.util.Map.Entry<String, Integer> e : bonus.entrySet()) {
                String k = e.getKey();
                Integer v = e.getValue();
                if (k != null && v != null) {
                    VIEW_BONUS.put(k, v);
                }
            }
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
