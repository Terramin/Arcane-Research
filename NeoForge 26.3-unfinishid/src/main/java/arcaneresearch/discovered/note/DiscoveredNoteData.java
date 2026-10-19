package arcaneresearch.discovered.note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

/**
 * Данные исследовательской записки (свиток), порт в паритете с:
 * - TC3 {@code thaumcraft.common.research.ResearchNoteData} (tags/progress/failedTags, getTotalProgress, getTagProgress)
 * - reforged {@code thaumcraft.research.ResearchNoteData} (key/tags/progress/failed, read/create/toNote)
 *
 * Адаптация под Forge 1.20.1: вместо DataComponent (reforged, неофордж 26.x)
 * используется обычный NBT-тег {@code DiscoveredNote} на стаке. Требуемые количества
 * аспектов ({@code need}) хранятся прямо в NBT, т.к. у ResearchEntry проекта
 * нет аспект-стоимости (в отличие от reforged ResearchItem.tags).
 *
 * NEOFORGE_PORT: ItemStack NBT (hasTag/getTag/getOrCreateTag) удалён в 26.x ->
 * DataComponents.CUSTOM_DATA через ArcaneresearchInvHelper
 * (getCustomTag/getOrCreateCustomTag/setCustomTag); NBT-формат 1:1.
 */
public class DiscoveredNoteData {

    public static final String NBT_ROOT = "DiscoveredNote";

    /** Ключ исследования (research key), может быть null у пустой записки. */
    public String key;
    /** Аспекты записки по порядку. */
    public Aspect[] tags = new Aspect[0];
    /** Сколько нужно по каждому аспекту (параллельно tags). */
    public int[] need = new int[0];
    /** Сколько уже изучено (параллельно tags). */
    public int[] have = new int[0];
    /**
     * Неудачные попытки по тегам аспектов (0..100), паритет TC3 {@code failedTags}.
     * В TC3 индексировалось числовым id аспекта; здесь ключ — строковый тег,
     * т.к. у Aspect проекта нет числового id. Используется столом исследования.
     */
    public Map<String, Byte> failed = new HashMap<>();

    /**
     * Стадии мини-игр записки: {@code stage1} ставит стол записок при
     * прогрессе 100%, {@code stage2} — hex-стол при победе. Порядок не важен.
     * Завершена записка ({@link #isComplete}) только при обеих стадиях
     * (для исследований с hex-стадией).
     */
    public boolean stage1;
    public boolean stage2;

    // ------------------------------------------------------------------ IO

    public static boolean hasNote(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
        return custom != null && custom.contains(NBT_ROOT);
    }

    /** Паритет reforged {@code ResearchNoteData.read}. */
    public static DiscoveredNoteData read(ItemStack stack) {
        DiscoveredNoteData data = new DiscoveredNoteData();
        if (!hasNote(stack)) {
            return data;
        }
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack).getCompoundOrEmpty(NBT_ROOT);
        data.key = root.getStringOr("key", "");
        if (data.key != null && data.key.isEmpty()) {
            data.key = null;
        }
        ListTag tagList = root.getListOrEmpty("tags");
        ListTag needList = root.getListOrEmpty("need");
        ListTag haveList = root.getListOrEmpty("have");
        List<Aspect> tags = new ArrayList<>();
        List<Integer> needs = new ArrayList<>();
        List<Integer> haves = new ArrayList<>();
        for (int i = 0; i < tagList.size(); i++) {
            Aspect aspect = Aspect.getAspect(tagList.getStringOr(i, ""));
            if (aspect == null) {
                continue; // неизвестный тег — пропускаем, как UNKNOWN в reforged
            }
            tags.add(aspect);
            needs.add(i < needList.size() ? Math.max(0, needList.getIntOr(i, 0)) : 0);
            haves.add(i < haveList.size() ? Math.max(0, haveList.getIntOr(i, 0)) : 0);
        }
        data.tags = tags.toArray(new Aspect[0]);
        data.need = needs.stream().mapToInt(Integer::intValue).toArray();
        data.have = haves.stream().mapToInt(Integer::intValue).toArray();
        CompoundTag fail = root.getCompoundOrEmpty("fail");
        for (String k : fail.keySet()) {
            data.failed.put(k, fail.getByteOr(k, (byte) 0));
        }
        data.stage1 = root.getBooleanOr("stage1", false);
        // Совместимость со старым флагом: hex ставился победой hex-мини-игры.
        data.stage2 = root.getBooleanOr("stage2", false) || root.getBooleanOr("hex", false);
        return data;
    }

    /** Паритет reforged {@code updateData} + {@code toNote}. */
    public static void write(ItemStack stack, DiscoveredNoteData data) {
        // MERGE, а не замена слоя: в том же слое живёт hex-поле
        // (единый NBT-слой записки), затирать его нельзя.
        // getCustomTag уже возвращает отсоединённую копию — правим и пишем назад.
        // NEOFORGE_PORT: custom бывает null на свежих стаках (CUSTOM_DATA ещё нет);
        // поля записки пишутся в SUB-тег NBT_ROOT (так их читает read()),
        // соседи верхнего уровня (hex-поле) сохраняются.
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
        if (custom == null) {
            custom = new CompoundTag();
        }
        CompoundTag root = new CompoundTag();
        root.putString("key", data.key == null ? "" : data.key);
        ListTag tagList = new ListTag();
        ListTag needList = new ListTag();
        ListTag haveList = new ListTag();
        for (int i = 0; i < data.tags.length; i++) {
            if (data.tags[i] == null) {
                continue;
            }
            tagList.add(StringTag.valueOf(data.tags[i].getTag()));
            needList.add(net.minecraft.nbt.IntTag.valueOf(i < data.need.length ? data.need[i] : 0));
            haveList.add(net.minecraft.nbt.IntTag.valueOf(i < data.have.length ? data.have[i] : 0));
        }
        root.put("tags", tagList);
        root.put("need", needList);
        root.put("have", haveList);
        root.putBoolean("stage1", data.stage1);
        root.putBoolean("stage2", data.stage2);
        CompoundTag fail = new CompoundTag();
        for (Map.Entry<String, Byte> e : data.failed.entrySet()) {
            fail.putByte(e.getKey(), e.getValue());
        }
        root.put("fail", fail);
        custom.put(NBT_ROOT, root);
        ArcaneresearchInvHelper.setCustomTag(stack, custom);
        // NEOFORGE_PORT: Item.getRarity override removed in 26.x (паритет TC3:
        // notes/uncommon, discovery/rare) -> RARITY data component, пересчёт при записи.
        stack.set(net.minecraft.core.component.DataComponents.RARITY,
                data.isComplete() ? net.minecraft.world.item.Rarity.RARE : net.minecraft.world.item.Rarity.UNCOMMON);
    }

    /** Паритет reforged {@code ResearchNoteData.create(key)} + TC3 {@code createNote}. */
    public static DiscoveredNoteData create(String key, AspectList required) {
        DiscoveredNoteData data = new DiscoveredNoteData();
        data.key = key;
        if (required != null && required.size() > 0) {
            Aspect[] aspects = required.getAspects();
            data.tags = aspects;
            data.need = new int[aspects.length];
            data.have = new int[aspects.length];
            for (int i = 0; i < aspects.length; i++) {
                data.need[i] = Math.max(1, required.getAmount(aspects[i]));
                data.have[i] = 0;
            }
        }
        return data;
    }

    // ------------------------------------------------------------------ math

    /** Паритет TC3 {@code getTotalProgress} (суммы have/need вместо rr.tags). */
    public float getTotalProgress() {
        float done = 0.0F;
        float total = 0.0F;
        for (int i = 0; i < tags.length; i++) {
            int want = i < need.length ? need[i] : 0;
            int got = i < have.length ? Math.min(have[i], want) : 0;
            done += got;
            total += want;
        }
        return total <= 0.0F ? 0.0F : done / total;
    }

    public boolean isComplete() {
        if (tags.length <= 0 || getTotalProgress() < 1.0F) {
            return false;
        }
        return !requiresHex(key) || (stage1 && stage2);
    }

    /** Исследование требует hex-стадию (есть стадия с {@code "hex": true}). */
    public static boolean requiresHex(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            arcaneresearch.api.research.ResearchEntry entry =
                    arcaneresearch.api.research.ResearchCategories.getResearch(key);
            if (entry == null) {
                entry = requiringResearch(key);
            }
            if (entry == null || entry.getStages() == null) {
                return false;
            }
            for (arcaneresearch.api.research.ResearchStage stage : entry.getStages()) {
                if (stage != null && stage.isHex()) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Первое исследование, чья стадия требует это открытие. */
    private static arcaneresearch.api.research.ResearchEntry requiringResearch(String discoveryKey) {
        try {
            for (arcaneresearch.api.research.ResearchCategory category :
                    arcaneresearch.api.research.ResearchCategories.researchCategories.values()) {
                if (category == null || category.research == null) {
                    continue;
                }
                for (arcaneresearch.api.research.ResearchEntry entry : category.research.values()) {
                    if (entry == null || entry.getStages() == null) {
                        continue;
                    }
                    for (arcaneresearch.api.research.ResearchStage stage : entry.getStages()) {
                        if (stage == null || stage.getDiscovery() == null) {
                            continue;
                        }
                        for (String d : stage.getDiscovery()) {
                            if (discoveryKey.equals(d)) {
                                return entry;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Паритет TC3 {@code getTagProgress}. */
    public float getTagProgress(Aspect tag) {
        if (tag == null) {
            return 0.0F;
        }
        for (int i = 0; i < tags.length; i++) {
            if (tags[i] == tag || (tags[i] != null && tags[i].getTag().equals(tag.getTag()))) {
                int want = i < need.length ? need[i] : 0;
                int got = i < have.length ? have[i] : 0;
                return want <= 0 ? 0.0F : (float) got / (float) want;
            }
        }
        return 0.0F;
    }

    /** Неудача 0..100 по тегу (для стола исследования, паритет TC3 failedTags). */
    public byte getFailed(String aspectTag) {
        Byte b = failed.get(aspectTag);
        return b == null ? 0 : b.byteValue();
    }

    public void addFailed(String aspectTag, byte amount) {
        int cur = Byte.toUnsignedInt(getFailed(aspectTag)) + Byte.toUnsignedInt(amount);
        failed.put(aspectTag, (byte) Math.min(100, cur));
    }
}
