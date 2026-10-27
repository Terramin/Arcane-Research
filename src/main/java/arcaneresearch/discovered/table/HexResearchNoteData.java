package arcaneresearch.discovered.table;

import java.util.Map;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.helper.StackNbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class HexResearchNoteData {
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
    public final java.util.HashMap<String, HexEntry> hexEntries = new java.util.HashMap<>();
    public final java.util.HashMap<String, HexUtils.Hex> hexes = new java.util.HashMap<>();
    public boolean complete;
    public int copies;

    public boolean isComplete() {
        return this.complete;
    }

    public void writeToNBT(CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        nbt.putString("key", key != null ? key : "");
        nbt.putInt("color", color);
        nbt.putBoolean("complete", complete);
        nbt.putInt("copies", copies);
        ListTag grid = new ListTag();
        for (Map.Entry<String, HexEntry> e : hexEntries.entrySet()) {
            HexEntry entry = e.getValue();
            HexUtils.Hex hex = hexes.get(e.getKey());
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
        key = nbt.getString("key");
        color = nbt.getInt("color");
        complete = nbt.getBoolean("complete");
        copies = nbt.getInt("copies");
        ListTag grid = nbt.getList("hexgrid", Tag.TAG_COMPOUND);
        final int n = grid.size();
        for (int i = 0; i < n; i++) {
            CompoundTag cell = grid.getCompound(i);
            int q = cell.getByte("hexq");
            int r = cell.getByte("hexr");
            int type = cell.getByte("type");
            Aspect aspect = null;
            if (cell.contains("aspect")) {
                try {
                    aspect = Aspect.getAspect(cell.getString("aspect"));
                } catch (Exception ignored) {
                }
            }
            HexUtils.Hex hex = new HexUtils.Hex(q, r);
            String hexKey = hex.toString();
            hexes.put(hexKey, hex);
            hexEntries.put(hexKey, new HexEntry(aspect, type));
        }
    }

    public static HexResearchNoteData getData(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return null;
        }
        CompoundTag layer = layer(stack, false);
        HexResearchNoteData data = new HexResearchNoteData();
        boolean haveField = layer != null && layer.contains("hexgrid", Tag.TAG_LIST);
        if (haveField) {
            data.readFromNBT(layer);
            if (data.key == null || data.key.isEmpty()) {
                return null;
            }
        } else {
            data.readFromNBT(stack.getTag());
            if (data.key == null || data.key.isEmpty()) {
                return null;
            }
            migrateToLayer(stack, data);
        }
        return data;
    }

    private static CompoundTag layer(ItemStack stack, boolean create) {
        try {
            if (create) {
                return StackNbtHelper.getOrCreateCompound(stack, DiscoveredNoteData.NBT_ROOT);
            }
            return StackNbtHelper.getCompound(stack, DiscoveredNoteData.NBT_ROOT);
        } catch (Exception e) {
            return null;
        }
    }

    private static void migrateToLayer(ItemStack stack, HexResearchNoteData data) {
        try {
            CompoundTag layer = layer(stack, true);
            if (layer == null) {
                return;
            }
            data.writeToNBT(layer);
            StackNbtHelper.putCompound(stack, DiscoveredNoteData.NBT_ROOT, layer);
            StackNbtHelper.removeStackKey(stack, "hexgrid");
            StackNbtHelper.removeStackKey(stack, "pool");
            StackNbtHelper.removeStackKey(stack, "color");
            StackNbtHelper.removeStackKey(stack, "complete");
            StackNbtHelper.removeStackKey(stack, "copies");
            StackNbtHelper.removeStackKey(stack, "key");
        } catch (Exception ignored) {
        }
    }

    public static void updateData(ItemStack stack, HexResearchNoteData data) {
        if (stack == null || stack.isEmpty() || data == null) {
            return;
        }
        CompoundTag layer = layer(stack, true);
        if (layer == null) {
            return;
        }
        data.writeToNBT(layer);
        StackNbtHelper.putCompound(stack, DiscoveredNoteData.NBT_ROOT, layer);
    }
}