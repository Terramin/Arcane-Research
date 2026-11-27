package arcaneresearch.api.aspects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Аспекты сущностей: проверка NBT-условий")
class EntityTagRulesTest {

    @Test
    @DisplayName("пустой список условий совпадает всегда")
    void emptyMatchesEverything() {
        assertTrue(EntityTagRules.matches(new CompoundTag(), List.of()));
        assertTrue(EntityTagRules.matches(null, null));
    }

    @Test
    @DisplayName("байтовое условие (powered creeper)")
    void byteCondition() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("powered", (byte) 1);

        assertTrue(EntityTagRules.matches(tag,
                List.of(new EntityTagRules.Condition("powered", (byte) 1))));
        assertFalse(EntityTagRules.matches(tag,
                List.of(new EntityTagRules.Condition("powered", (byte) 0))));
    }

    @Test
    @DisplayName("числовые условия разных типов")
    void numericConditions() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("PechType", 2);
        tag.putShort("Short", (short) 7);
        tag.putLong("Long", 9L);
        tag.putFloat("Float", 1.5f);
        tag.putDouble("Double", 2.5d);

        assertTrue(EntityTagRules.matches(tag, List.of(
                new EntityTagRules.Condition("PechType", 2),
                new EntityTagRules.Condition("Short", (short) 7),
                new EntityTagRules.Condition("Long", 9L),
                new EntityTagRules.Condition("Float", 1.5f),
                new EntityTagRules.Condition("Double", 2.5d))));
        assertFalse(EntityTagRules.matches(tag,
                List.of(new EntityTagRules.Condition("PechType", 1))));
    }

    @Test
    @DisplayName("строковое условие и boolean")
    void stringAndBooleanConditions() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Variant", "blue");
        tag.putBoolean("Enabled", true);

        assertTrue(EntityTagRules.matches(tag, List.of(
                new EntityTagRules.Condition("Variant", "blue"),
                new EntityTagRules.Condition("Enabled", true))));
        assertFalse(EntityTagRules.matches(tag,
                List.of(new EntityTagRules.Condition("Variant", "red"))));
    }

    @Test
    @DisplayName("отсутствующее поле не совпадает")
    void missingFieldDoesNotMatch() {
        CompoundTag tag = new CompoundTag();
        assertFalse(EntityTagRules.matches(tag,
                List.of(new EntityTagRules.Condition("powered", (byte) 1))));
        assertFalse(EntityTagRules.matches(null,
                List.of(new EntityTagRules.Condition("powered", (byte) 1))));
    }

    @Test
    @DisplayName("все условия должны совпасть одновременно")
    void allConditionsRequired() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("powered", (byte) 1);
        tag.putInt("PechType", 2);

        assertTrue(EntityTagRules.matches(tag, List.of(
                new EntityTagRules.Condition("powered", (byte) 1),
                new EntityTagRules.Condition("PechType", 2))));
        assertFalse(EntityTagRules.matches(tag, List.of(
                new EntityTagRules.Condition("powered", (byte) 1),
                new EntityTagRules.Condition("PechType", 3))));
    }
}
