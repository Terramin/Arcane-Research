package arcaneresearch.api.aspects;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Pure matching rule for NBT-specific entity aspect registrations.
 *
 * <p>The 1.12.2 original serializes a living entity to NBT and keeps only
 * the entries whose every {@code EntityTagsNBT} condition is present and equal.
 * The last matching registration wins, which lets a generic entry be followed
 * by a more specific one.
 */
public final class EntityTagRules {

    /**
     * One required NBT field and the value it must have.
     *
     * @param name NBT field name
     * @param value expected value (Byte, Short, Integer, Long, Float, Double,
     *              String or Boolean)
     */
    public record Condition(String name, Object value) {
        public Condition {
            Objects.requireNonNull(name, "condition name");
            Objects.requireNonNull(value, "condition value");
        }
    }

    private EntityTagRules() {
    }

    /**
     * True when the tag satisfies every condition; an empty list matches all.
     *
     * @param tag serialized entity data, may be null when conditions exist
     * @param conditions required NBT fields, may be null or empty
     * @return true when all conditions are present and equal
     */
    public static boolean matches(@Nullable CompoundTag tag, @Nullable List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        if (tag == null) {
            return false;
        }
        for (Condition condition : conditions) {
            if (!tag.contains(condition.name())) {
                return false;
            }
            if (!equals(tag.get(condition.name()), condition.value())) {
                return false;
            }
        }
        return true;
    }

    /** Compares a stored NBT tag with the expected Java value by tag type. */
    private static boolean equals(Tag actual, Object expected) {
        if (expected instanceof Boolean bool) {
            return actual instanceof ByteTag byteTag
                    && byteTag.getAsByte() == (bool ? 1 : 0);
        }
        if (expected instanceof Byte value) {
            return actual instanceof ByteTag byteTag && byteTag.getAsByte() == value;
        }
        if (expected instanceof Short value) {
            return actual instanceof ShortTag shortTag && shortTag.getAsShort() == value;
        }
        if (expected instanceof Integer value) {
            return actual instanceof IntTag intTag && intTag.getAsInt() == value;
        }
        if (expected instanceof Long value) {
            return actual instanceof LongTag longTag && longTag.getAsLong() == value;
        }
        if (expected instanceof Float value) {
            return actual instanceof FloatTag floatTag && floatTag.getAsFloat() == value;
        }
        if (expected instanceof Double value) {
            return actual instanceof DoubleTag doubleTag && doubleTag.getAsDouble() == value;
        }
        if (expected instanceof String value) {
            return actual instanceof StringTag stringTag && stringTag.getAsString().equals(value);
        }
        return String.valueOf(expected).equals(actual.toString());
    }
}
