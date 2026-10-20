package arcaneresearch.discovered.scan;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import arcaneresearch.api.aspects.Aspect;

/**
 * Личный запас аспектов игрока + множество уже изученного.
 *
 * Порт серверной половины {@code PlayerKnowledge} (TC 4.2.3.5):
 * {@code aspectsDiscovered} (discovery + пул) и
 * {@code completeScanned{Object,Entity}} (защита от повторного фарма).
 * Хранится в {@code player.getPersistentData()} — регистрации capability
 * и правок файлов проекта не нужно.
 *
 * NEOFORGE_PORT: Entity#getPersistentData жив; CompoundTag-аксессоры —
 * новые (getCompound -> Optional, getInt/getBoolean -> getIntOr/
 * getBooleanOr, contains(key), keySet). Множество изученного хранится
 * компаундом флагов ({@code SCANNED} ключ -&gt; true) вместо ListTag
 * строк: у CompoundTag.getList нет проверенного аналога в 26.3,
 * shape тот же по смыслу (ключи 1:1).
 */
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

    /** Пул аспекта в запасе (паритет {@code getAspectPoolFor}). */
    public static int getAmount(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return 0;
        }
        try {
            return aspectsTag(player).getIntOr(aspect.getTag(), 0);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Начислить в запас (паритет {@code addAspectPool}, только +). */
    public static void add(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return;
        }
        CompoundTag tags = aspectsTag(player);
        tags.putInt(aspect.getTag(), tags.getIntOr(aspect.getTag(), 0) + amount);
    }

    /**
     * Списать из запаса (паритет {@code addAspectPool} с отрицательным
     * количеством). Возвращает реально снятое (не больше остатка).
     */
    public static int remove(Player player, Aspect aspect, int amount) {
        if (player == null || aspect == null || amount <= 0) {
            return 0;
        }
        CompoundTag tags = aspectsTag(player);
        int have = tags.getIntOr(aspect.getTag(), 0);
        int take = Math.min(have, amount);
        if (take > 0) {
            tags.putInt(aspect.getTag(), have - take);
        }
        return take;
    }

    /** Уже изучалось (паритет {@code hasBeenScanned}). */
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

    /** Пометить изученным (паритет {@code completeScanned*}). */
    public static void markScanned(Player player, String key) {
        if (player == null || key == null || hasScanned(player, key)) {
            return;
        }
        scannedTag(player).putBoolean(key, true);
    }

    /** Снимок запаса для сетевого синка (тег -&gt; количество). */
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

    // ---- клиентское зеркало (только чтение для GUI; пишет MsgWalletSync) ----

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

    /** Видимый запас аспекта (кошелёк игрока). */
    public static int viewAmount(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        Integer amount = VIEW.get(aspect.getTag());
        return amount == null ? 0 : amount;
    }

    /** Видимый бонус аспекта (тайл открытого стола). */
    public static int viewBonus(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        Integer amount = VIEW_BONUS.get(aspect.getTag());
        return amount == null ? 0 : amount;
    }
}
