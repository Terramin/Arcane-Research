package arcaneresearch.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.ArcaneresearchInvHelper;

/**
 * Единая точка доступа к корневому NBT предметов (порт из Forge 1.20.1).
 *
 * <p>26.3: корневого NBT нет — всё лежит в компоненте
 * {@code minecraft:custom_data}, чтение даёт КОПИЮ. Поэтому геттеры
 * возвращают отсоединённые копии: любая правка требует обратного
 * {@link #putCompound} (write-back), иначе теряется. Вызыватели Batch 5/6
 * уже так пишут (как на Forge).</p>
 */
public final class StackNbtHelper {

    private StackNbtHelper() {
    }

    /** Есть ли вложенный компаунд {@code key} (проверка чтения). */
    public static boolean hasCompound(ItemStack stack, String key) {
        try {
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            return root != null && root.contains(key);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Вложенный компаунд или {@code null}. Отсоединённая копия:
     * правки — только через {@link #putCompound}.
     */
    public static CompoundTag getCompound(ItemStack stack, String key) {
        try {
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null || !root.contains(key)) {
                return null;
            }
            return root.getCompound(key).orElse(null);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Вложенный компаунд с созданием цепочки или {@code null}.
     * Отсоединённая копия: правки — только через {@link #putCompound}.
     */
    public static CompoundTag getOrCreateCompound(ItemStack stack, String key) {
        try {
            if (stack == null || stack.isEmpty()) {
                return null;
            }
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null) {
                root = new CompoundTag();
            }
            CompoundTag nested = root.getCompound(key).orElse(null);
            if (nested == null) {
                nested = new CompoundTag();
                root.put(key, nested);
                ArcaneresearchInvHelper.setCustomTag(stack, root);
                nested = new CompoundTag();
            }
            return nested;
        } catch (Exception e) {
            return null;
        }
    }

    /** Положить вложенный компаунд в корень (write-back). */
    public static void putCompound(ItemStack stack, String key, CompoundTag tag) {
        try {
            if (stack == null || stack.isEmpty() || tag == null) {
                return;
            }
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null) {
                root = new CompoundTag();
            }
            root.put(key, tag);
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        } catch (Exception ignored) {
        }
    }

    /** Удалить ключ из корня (миграция legacy-полей). */
    public static void removeStackKey(ItemStack stack, String key) {
        try {
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
            if (root == null) {
                return;
            }
            root.remove(key);
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        } catch (Exception ignored) {
        }
    }

    /** Скопировать весь корневой тег (компонент иммутабелен — ссылка безопасна). */
    public static void copyStackTag(ItemStack from, ItemStack to) {
        try {
            if (from == null || to == null) {
                return;
            }
            CompoundTag root = ArcaneresearchInvHelper.getCustomTag(from);
            if (root == null) {
                return;
            }
            ArcaneresearchInvHelper.setCustomTag(to, root);
        } catch (Exception ignored) {
        }
    }
}
