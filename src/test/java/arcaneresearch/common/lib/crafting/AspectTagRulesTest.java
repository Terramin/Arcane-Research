package arcaneresearch.common.lib.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Аспекты предметов: чистые правила")
class AspectTagRulesTest {

    private static final Aspect[] ALL = {Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH,
            Aspect.ORDER, Aspect.ENTROPY, Aspect.BEAST, Aspect.PROTECT, Aspect.TOOL,
            Aspect.CRAFT, Aspect.MAGIC, Aspect.DESIRE, Aspect.FLIGHT, Aspect.EXCHANGE,
            Aspect.UNDEAD, Aspect.COLD};

    private static ListTag enchantments(String id, int level) {
        CompoundTag entry = new CompoundTag();
        entry.putString("id", id);
        entry.putInt("lvl", level);
        ListTag list = new ListTag();
        list.add(entry);
        return list;
    }

    @Test
    @DisplayName("enchanted books read StoredEnchantments, normal items keep their list")
    void enchantmentTags() {
        CompoundTag bookTag = new CompoundTag();
        ListTag stored = enchantments("minecraft:sharpness", 3);
        bookTag.put(AspectTagRules.STORED_ENCHANTMENTS, stored);

        assertEquals(stored, AspectTagRules.enchantmentTag(true, bookTag, new ListTag()));
        assertEquals(0, AspectTagRules.enchantmentTag(true, null, stored).size());
        assertEquals(stored, AspectTagRules.enchantmentTag(false, bookTag, stored));
        assertEquals(0, AspectTagRules.enchantmentTag(true, new CompoundTag(), stored).size());
    }

    @Test
    @DisplayName("container essentia replaces base aspects instead of merging")
    void containerReplacement() {
        AspectList base = new AspectList().add(Aspect.AIR, 5).add(Aspect.FIRE, 7);
        AspectList contained = new AspectList().add(Aspect.WATER, 2);

        AspectList resolved = AspectTagRules.containerAspects(base, contained, false);
        assertEquals(1, resolved.size());
        assertEquals(2, resolved.getAmount(Aspect.WATER));
        assertEquals(5, base.getAmount(Aspect.AIR), "source list must not be mutated");

        assertEquals(2, AspectTagRules.containerAspects(base, contained, true).size());
        assertEquals(2, AspectTagRules.containerAspects(base, null, false).size());
        assertEquals(0, AspectTagRules.containerAspects(null, new AspectList(), false).size());
    }

    @Test
    @DisplayName("capping clamps every aspect to the limit")
    void cap() {
        AspectList source = new AspectList().add(Aspect.AIR, 900).add(Aspect.FIRE, 3);
        AspectList capped = AspectTagRules.cap(source, 500);

        assertEquals(500, capped.getAmount(Aspect.AIR));
        assertEquals(3, capped.getAmount(Aspect.FIRE));
        assertEquals(900, source.getAmount(Aspect.AIR));
        assertEquals(0, AspectTagRules.cap(null, 500).size());
    }

    @Test
    @DisplayName("fuzz: capped aspects stay in range and never gain null entries")
    void capFuzz() {
        Random random = new Random(777L);
        for (int i = 0; i < 20_000; i++) {
            AspectList source = new AspectList();
            int n = random.nextInt(12);
            for (int j = 0; j < n; j++) {
                source.add(ALL[random.nextInt(ALL.length)], random.nextInt(2000) - 200);
            }
            AspectList capped = AspectTagRules.cap(source, 500);
            assertTrue(capped.size() <= source.size());
            for (Aspect aspect : capped.getAspects()) {
                assertTrue(aspect != null);
                assertTrue(capped.getAmount(aspect) <= 500);
            }
        }
    }

    @Test
    @DisplayName("cull keeps the ten highest-weighted aspects, favouring composites")
    void cullPriority() {
        AspectList many = new AspectList();
        for (Aspect aspect : new Aspect[]{Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH,
                Aspect.ORDER, Aspect.ENTROPY, Aspect.BEAST, Aspect.PROTECT, Aspect.TOOL,
                Aspect.CRAFT, Aspect.MAGIC, Aspect.DESIRE}) {
            many.add(aspect, 10);
        }

        AspectList culled = AspectHelper.cullTags(many, 10);
        assertEquals(10, culled.size());
        assertFalse(culled.getAmount(Aspect.AIR) > 0, "primal aspects lose the weight tie");
        assertTrue(culled.getAmount(Aspect.DESIRE) > 0, "composite aspects survive");
    }
}
