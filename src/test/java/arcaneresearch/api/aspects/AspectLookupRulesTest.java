package arcaneresearch.api.aspects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Поиск аспектов: каскад NBT и damage")
class AspectLookupRulesTest {

    private static final ResourceLocation ID = ResourceLocation.parse("minecraft:diamond_sword");

    @Test
    @DisplayName("canonical сортирует ключи составного тега")
    void canonicalSortsCompoundKeys() {
        CompoundTag first = new CompoundTag();
        first.putInt("Damage", 5);
        first.putString("CustomModelData", "x");

        CompoundTag second = new CompoundTag();
        second.putString("CustomModelData", "x");
        second.putInt("Damage", 5);

        assertEquals(AspectLookupRules.canonical(first).toString(),
                AspectLookupRules.canonical(second).toString());
    }

    @Test
    @DisplayName("canonical сохраняет вложенные теги и списки")
    void canonicalHandlesNestedTags() {
        CompoundTag innerA = new CompoundTag();
        innerA.putInt("b", 2);
        innerA.putInt("a", 1);
        CompoundTag innerB = new CompoundTag();
        innerB.putInt("a", 1);
        innerB.putInt("b", 2);
        assertEquals(AspectLookupRules.canonical(innerA), AspectLookupRules.canonical(innerB));
    }

    @Test
    @DisplayName("ключ без NBT — просто id предмета")
    void bareKey() {
        assertEquals("minecraft:diamond_sword", AspectLookupRules.key(ID, null));
        assertTrue(AspectLookupRules.isBare(AspectLookupRules.key(ID, null)));
    }

    @Test
    @DisplayName("ключ с NBT содержит канонический тег")
    void taggedKey() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Damage", 7);
        String key = AspectLookupRules.key(ID, tag);
        assertTrue(key.startsWith("minecraft:diamond_sword#"));
        assertFalse(AspectLookupRules.isBare(key));
    }

    @Test
    @DisplayName("без Damage остаётся только канонический тег")
    void withoutDamage() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Damage", 32767);
        tag.putString("Mode", "test");
        CompoundTag stripped = AspectLookupRules.withoutDamage(tag);
        assertFalse(stripped.contains("Damage"));
        assertTrue(stripped.contains("Mode"));
    }

    @Test
    @DisplayName("кандидаты: точный тег → без damage → голый id")
    void candidatesWithDamage() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Damage", 3);
        List<String> candidates = AspectLookupRules.candidates(ID, tag);

        assertEquals(2, candidates.size());
        assertEquals("minecraft:diamond_sword#{Damage:3}", candidates.get(0));
        assertEquals("minecraft:diamond_sword", candidates.get(1));
    }

    @Test
    @DisplayName("без damage кандидатов два: тег и id")
    void candidatesWithoutDamage() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Mode", "test");
        List<String> candidates = AspectLookupRules.candidates(ID, tag);

        assertEquals(2, candidates.size());
        assertEquals("minecraft:diamond_sword", candidates.get(1));
    }

    @Test
    @DisplayName("для предмета без тега кандидат один")
    void candidatesForBareStack() {
        assertEquals(List.of("minecraft:diamond_sword"), AspectLookupRules.candidates(ID, null));
    }

    @Test
    @DisplayName("NBT-аспект метки даёт отдельный ключ для каждого аспекта")
    void labelledNbtKeys() {
        CompoundTag aer = new CompoundTag();
        aer.putString("aspect", "aer");
        CompoundTag ignis = new CompoundTag();
        ignis.putString("aspect", "ignis");
        ResourceLocation id = ResourceLocation.parse("arcaneresearch:label_filled");

        assertEquals("arcaneresearch:label_filled#{aspect:\"aer\"}", AspectLookupRules.key(id, aer));
        assertEquals("arcaneresearch:label_filled#{aspect:\"ignis\"}", AspectLookupRules.key(id, ignis));
        assertEquals(2, AspectLookupRules.candidates(id, aer).size());
        assertTrue(AspectLookupRules.candidates(id, aer).get(0).contains("aspect:\"aer\""));
    }

    @Test
    @DisplayName("taggedEntries возвращает только записи с тегом")
    void taggedEntries() {
        Map<String, AspectList> source = new java.util.HashMap<>();
        source.put("minecraft:diamond_sword", new AspectList());
        source.put("minecraft:diamond_sword#{\"Damage\":1}", new AspectList());
        source.put("minecraft:iron_sword#{}", new AspectList());

        List<Map.Entry<String, AspectList>> entries = AspectLookupRules.taggedEntries(source, ID);
        assertEquals(1, entries.size());
        assertTrue(entries.get(0).getKey().contains("Damage"));
    }
}
