package arcaneresearch.common.wards;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Per-dimension registry of warded blocks, persisted with the level data.
 * The block itself is never replaced, so the registry is the single source of
 * truth for both protection and the client-side outline FX.
 */
public final class WardStorage extends SavedData {
    private final Map<BlockPos, WardEntry> wards = new HashMap<>();

    public WardStorage() {
    }

    public static WardStorage load(CompoundTag tag) {
        WardStorage storage = new WardStorage();
        storage.wards.putAll(WardData.deserialize(tag));
        return storage;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.put(WardData.TAG_WARDS, WardData.serialize(wards).getList(WardData.TAG_WARDS,
                net.minecraft.nbt.Tag.TAG_COMPOUND));
        return tag;
    }

    public boolean isWarded(BlockPos pos) {
        return wards.containsKey(pos);
    }

    public WardEntry get(BlockPos pos) {
        return wards.get(pos);
    }

    public Map<BlockPos, WardEntry> entries() {
        return Collections.unmodifiableMap(wards);
    }

    public boolean add(BlockPos pos, Player player) {
        UUID uuid = player == null ? null : player.getUUID();
        if (!WardData.canAdd(wards, pos, uuid)) {
            return false;
        }
        WardEntry previous = wards.put(pos, new WardEntry(uuid,
                player == null ? "" : player.getGameProfile().getName()));
        if (previous == null || !previous.equals(wards.get(pos))) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean remove(BlockPos pos, Player player) {
        UUID uuid = player == null ? null : player.getUUID();
        if (!WardData.canRemove(wards, pos, uuid)) {
            return false;
        }
        wards.remove(pos);
        setDirty();
        return true;
    }

    /** Creative-mode breaking drops the ward regardless of who owned it. */
    public boolean forceRemove(BlockPos pos) {
        if (wards.remove(pos) != null) {
            setDirty();
            return true;
        }
        return false;
    }

}
