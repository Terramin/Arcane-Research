package arcaneresearch.common.wards;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.UUID;

/**
 * The owner information stored for one warded block coordinate.
 *
 * <p>The UUID is authoritative; the name is only kept so that the owner can be
 * shown in tooltips/debug output. Entries are immutable and safe to keep in a
 * level data storage map.</p>
 */
public final class WardEntry {
    private final String ownerUuid;
    private final String ownerName;

    public WardEntry(UUID ownerUuid, String ownerName) {
        this(ownerUuid == null ? "" : ownerUuid.toString(), ownerName == null ? "" : ownerName);
    }

    public WardEntry(String ownerUuid, String ownerName) {
        this.ownerUuid = ownerUuid == null ? "" : ownerUuid;
        this.ownerName = ownerName == null ? "" : ownerName;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isOwnedBy(UUID uuid) {
        return uuid != null && ownerUuid.equals(uuid.toString());
    }

    public boolean isOwnedBy(Player player) {
        return player != null && isOwnedBy(player.getUUID());
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString(WardData.TAG_OWNER, ownerUuid);
        tag.putString(WardData.TAG_NAME, ownerName);
        return tag;
    }

    public static WardEntry fromNbt(CompoundTag tag) {
        if (tag == null) {
            return new WardEntry("", "");
        }
        return new WardEntry(tag.getString(WardData.TAG_OWNER), tag.getString(WardData.TAG_NAME));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WardEntry entry)) {
            return false;
        }
        return ownerUuid.equals(entry.ownerUuid) && ownerName.equals(entry.ownerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerUuid, ownerName);
    }

    @Override
    public String toString() {
        return "WardEntry[owner=" + ownerName + ", uuid=" + ownerUuid + "]";
    }
}
