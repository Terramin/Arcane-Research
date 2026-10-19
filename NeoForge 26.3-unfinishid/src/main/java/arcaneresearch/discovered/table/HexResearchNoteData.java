package arcaneresearch.discovered.table;

import java.util.HashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.note.DiscoveredNoteData;

/**
 * Порт {@code thaumcraft.common.lib.research.ResearchNoteData} (TC 4.2.3.5).
 * Только DTO + NBT; вся логика — в {@link HexResearchGame}.
 * Имена NBT-тегов 1-в-1 с оригиналом ({@code key/color/complete/copies/
 * hexgrid/{hexq,hexr,type,aspect}}).
 * Живёт в едином {@code DiscoveredNote}-слое предмета (см. getData).
 *
 * NEOFORGE_PORT: ItemStack NBT (hasTag/getTag) удалён в 26.x ->
 * CUSTOM_DATA через ArcaneresearchInvHelper (getCustomTag — копия,
 * правки только с write-back через setCustomTag); геттеры CompoundTag/
 * ListTag — OrEmpty-варианты 26.x; NBT-формат 1:1.
 */
public class HexResearchNoteData {

    /** Ячейка поля. type: 0 — свободна, 1 — стартовая цель, 2 — поставлена игроком. */
    public static class HexEntry {
        public Aspect aspect;
        public int type;

        public HexEntry(Aspect aspect, int type) {
            this.aspect = aspect;
            this.type = type;
        }
    }

    public String key;
    public int color;
    public HashMap<String, HexEntry> hexEntries = new HashMap<>();
    public HashMap<String, HexUtils.Hex> hexes = new HashMap<>();
    public boolean complete;
    public int copies;

    public boolean isComplete() {
        return this.complete;
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putString("key", key != null ? key : "");
        nbt.putInt("color", color);
        nbt.putBoolean("complete", complete);
        nbt.putInt("copies", copies);
        ListTag grid = new ListTag();
        for (String k : hexEntries.keySet()) {
            HexUtils.Hex hex = hexes.get(k);
            HexEntry entry = hexEntries.get(k);
            if (hex == null || entry == null) {
                continue;
            }
            CompoundTag cell = new CompoundTag();
            cell.putByte("hexq", (byte) hex.q);
            cell.putByte("hexr", (byte) hex.r);
            cell.putByte("type", (byte) entry.type);
            if (entry.aspect != null) {
                cell.putString("aspect", entry.aspect.getTag());
            }
            grid.add(cell);
        }
        nbt.put("hexgrid", grid);
    }

    public void readFromNBT(CompoundTag nbt) {
        hexEntries.clear();
        hexes.clear();
        if (nbt == null) {
            return;
        }
        key = nbt.getStringOr("key", "");
        color = nbt.getIntOr("color", 0);
        complete = nbt.getBooleanOr("complete", false);
        copies = nbt.getIntOr("copies", 0);
        ListTag grid = nbt.getListOrEmpty("hexgrid");
        for (int i = 0; i < grid.size(); i++) {
            CompoundTag cell = grid.getCompoundOrEmpty(i);
            int q = cell.getByteOr("hexq", (byte) 0);
            int r = cell.getByteOr("hexr", (byte) 0);
            int type = cell.getByteOr("type", (byte) 0);
            Aspect aspect = null;
            if (cell.contains("aspect")) {
                try {
                    aspect = Aspect.getAspect(cell.getStringOr("aspect", ""));
                } catch (Exception ignored) {
                }
            }
            HexUtils.Hex hex = new HexUtils.Hex(q, r);
            hexes.put(hex.toString(), hex);
            hexEntries.put(hex.toString(), new HexEntry(aspect, type));
        }
    }

    /** Чтение записки из NBT предмета; null если тега нет. */
    public static HexResearchNoteData getData(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !ArcaneresearchInvHelper.hasCustomTag(stack)) {
            return null;
        }
        CompoundTag layer = layer(stack, false);
        HexResearchNoteData data = new HexResearchNoteData();
        boolean haveField = layer != null && layer.getListOrEmpty("hexgrid").size() > 0;
        if (haveField) {
            data.readFromNBT(layer);
        } else {
            // Legacy: поле в корне тега — читаем и мигрируем в единый слой.
            data.readFromNBT(ArcaneresearchInvHelper.getCustomTag(stack));
            if (data.key == null || data.key.isEmpty()) {
                return null;
            }
            migrateToLayer(stack, data);
        }
        if (data.key == null || data.key.isEmpty()) {
            return null;
        }
        return data;
    }

    /** Слой записки (единый компаунд {@code DiscoveredNote}). */
    private static CompoundTag layer(ItemStack stack, boolean create) {
        try {
            CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
            if (custom == null) {
                if (!create) {
                    return null;
                }
                custom = new CompoundTag();
            }
            if (create && !custom.contains(DiscoveredNoteData.NBT_ROOT)) {
                custom.put(DiscoveredNoteData.NBT_ROOT, new CompoundTag());
                ArcaneresearchInvHelper.setCustomTag(stack, custom);
                custom = ArcaneresearchInvHelper.getCustomTag(stack);
                if (custom == null) {
                    return null;
                }
            }
            // Отсоединённая копия: вызыватели пишут назад через putCompound-логику
            // updateData/migrateToLayer.
            return custom.getCompound(DiscoveredNoteData.NBT_ROOT).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /** Перенос legacy-поля из корня в единый слой (с зачисткой корня). */
    private static void migrateToLayer(ItemStack stack, HexResearchNoteData data) {
        try {
            CompoundTag layer = layer(stack, true);
            if (layer == null) {
                return;
            }
            data.writeToNBT(layer);
            CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
            if (custom == null) {
                custom = new CompoundTag();
            }
            custom.put(DiscoveredNoteData.NBT_ROOT, layer);
            custom.remove("hexgrid");
            custom.remove("pool");
            custom.remove("color");
            custom.remove("complete");
            custom.remove("copies");
            custom.remove("key");
            ArcaneresearchInvHelper.setCustomTag(stack, custom);
        } catch (Exception ignored) {
        }
    }

    /** Запись записки в NBT предмета (единый слой). */
    public static void updateData(ItemStack stack, HexResearchNoteData data) {
        if (stack == null || stack.isEmpty() || data == null) {
            return;
        }
        CompoundTag layer = layer(stack, true);
        if (layer == null) {
            return;
        }
        data.writeToNBT(layer);
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
        if (custom == null) {
            custom = new CompoundTag();
        }
        custom.put(DiscoveredNoteData.NBT_ROOT, layer);
        ArcaneresearchInvHelper.setCustomTag(stack, custom);
    }
}
