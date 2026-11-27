package arcaneresearch.api.aspects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * Pure rules for item-aspect lookup keys.
 *
 * <p>The 1.12.2 original looks up an exact stack id first, then a wildcard
 * damage variant (32767), then a fully stripped stack. 1.20.1 has no metadata
 * damage, so damage lives in the {@code Damage} NBT component; the equivalent
 * cascade is exact tag, tag without {@code Damage}, then no tag at all. Compound
 * keys are sorted so that two equal tags written in a different order produce
 * the same key.
 */
public final class AspectLookupRules {

    /** NBT field holding the damage of a damageable item in 1.20.1. */
    public static final String DAMAGE = "Damage";

    private AspectLookupRules() {
    }

    /**
     * Returns a copy of {@code tag} with every compound's keys sorted.
     *
     * @param tag source tag, may be null
     * @return a canonical copy, never null
     */
    public static CompoundTag canonical(@Nullable CompoundTag tag) {
        if (tag == null) {
            return new CompoundTag();
        }
        CompoundTag result = new CompoundTag();
        List<String> keys = new ArrayList<>(tag.getAllKeys());
        Collections.sort(keys);
        for (String key : keys) {
            result.put(key, canonicalValue(tag.get(key)));
        }
        return result;
    }

    /** Recursively canonicalizes a value, leaving list order unchanged. */
    private static Tag canonicalValue(Tag value) {
        if (value instanceof CompoundTag compound) {
            return canonical(compound);
        }
        if (value instanceof ListTag list) {
            ListTag result = new ListTag();
            for (Tag element : list) {
                result.add(canonicalValue(element));
            }
            return result;
        }
        return value.copy();
    }

    /**
     * Builds the key used to store or look up one exact item/tag combination.
     *
     * @param itemId registry id of the item
     * @param tag NBT of the stack, may be null
     * @return {@code id} or {@code id#canonicalTag}
     */
    public static String key(ResourceLocation itemId, @Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return itemId.toString();
        }
        return itemId + "#" + canonical(tag);
    }

    /**
     * Returns a copy of the tag without the damage component.
     *
     * @param tag source tag, may be null
     * @return canonical copy without {@link #DAMAGE}
     */
    public static CompoundTag withoutDamage(@Nullable CompoundTag tag) {
        CompoundTag copy = canonical(tag);
        copy.remove(DAMAGE);
        return copy;
    }

    /**
     * Ordered lookup candidates: exact tag, damage-agnostic tag, then the bare
     * item id (the fully stripped stack). Never returns null or an empty list.
     *
     * @param itemId registry id of the item
     * @param tag NBT of the stack, may be null
     * @return keys in priority order
     */
    public static List<String> candidates(ResourceLocation itemId, @Nullable CompoundTag tag) {
        List<String> result = new ArrayList<>(3);
        if (tag != null && !tag.isEmpty()) {
            result.add(key(itemId, tag));
            if (tag.contains(DAMAGE)) {
                CompoundTag stripped = withoutDamage(tag);
                if (!stripped.isEmpty()) {
                    result.add(key(itemId, stripped));
                }
            }
        }
        result.add(itemId.toString());
        return result;
    }

    /**
     * True when the registration key is for an item without any NBT. Used by
     * the entity/variant lookup to avoid treating a bare id as a tag match.
     *
     * @param key registration key
     * @return true when the key contains no {@code #} separator
     */
    public static boolean isBare(String key) {
        return key.indexOf('#') < 0;
    }

    /**
     * Returns the map entries whose key starts with {@code id + "#"}.
     *
     * @param source registration map
     * @param itemId item id to filter by
     * @return only the NBT-specific entries for that item
     */
    public static List<Map.Entry<String, AspectList>> taggedEntries(
            Map<String, AspectList> source, ResourceLocation itemId) {
        String prefix = itemId + "#";
        List<Map.Entry<String, AspectList>> result = new ArrayList<>();
        for (Map.Entry<String, AspectList> entry : source.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                result.add(entry);
            }
        }
        return result;
    }
}
