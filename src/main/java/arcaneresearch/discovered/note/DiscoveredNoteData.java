package arcaneresearch.discovered.note;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.ArcaneresearchInvHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

public class DiscoveredNoteData {

    public static final String NBT_ROOT = "DiscoveredNote";

    public String key;
    public Aspect[] tags = new Aspect[0];
    public int[] need = new int[0];
    public int[] have = new int[0];
    public Map<String, Byte> failed = new HashMap<>();
    public boolean stage1;
    public boolean stage2;

    public static boolean hasNote(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
        return custom != null && custom.contains(NBT_ROOT);
    }

    public static DiscoveredNoteData read(ItemStack stack) {
        DiscoveredNoteData data = new DiscoveredNoteData();
        if (!hasNote(stack)) {
            return data;
        }
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
        if (custom == null) {
            return data;
        }
        CompoundTag root = custom.getCompoundOrEmpty(NBT_ROOT);
        data.key = root.getStringOr("key", "");
        if (data.key.isEmpty()) {
            data.key = null;
        }
        ListTag tagList = root.getListOrEmpty("tags");
        ListTag needList = root.getListOrEmpty("need");
        ListTag haveList = root.getListOrEmpty("have");
        int n = tagList.size();
        Aspect[] rawTags = new Aspect[n];
        int[] rawNeeds = new int[n];
        int[] rawHaves = new int[n];
        int valid = 0;
        for (int i = 0; i < n; i++) {
            String s = tagList.getStringOr(i, "");
            Aspect aspect = Aspect.getAspect(s);
            if (aspect == null) {
                continue;
            }
            rawTags[valid] = aspect;
            rawNeeds[valid] = i < needList.size() ? Math.max(0, needList.getIntOr(i, 0)) : 0;
            rawHaves[valid] = i < haveList.size() ? Math.max(0, haveList.getIntOr(i, 0)) : 0;
            valid++;
        }
        if (valid == n) {
            data.tags = rawTags;
            data.need = rawNeeds;
            data.have = rawHaves;
        } else {
            data.tags = java.util.Arrays.copyOf(rawTags, valid);
            data.need = java.util.Arrays.copyOf(rawNeeds, valid);
            data.have = java.util.Arrays.copyOf(rawHaves, valid);
        }
        CompoundTag fail = root.getCompoundOrEmpty("fail");
        for (String k : fail.keySet()) {
            data.failed.put(k, fail.getByteOr(k, (byte) 0));
        }
        data.stage1 = root.getBooleanOr("stage1", false);
        data.stage2 = root.getBooleanOr("stage2", false) || root.getBooleanOr("hex", false);
        return data;
    }

    public static void write(ItemStack stack, DiscoveredNoteData data) {
        if (stack == null || data == null) {
            return;
        }
        CompoundTag custom = ArcaneresearchInvHelper.getCustomTag(stack);
        if (custom == null) {
            custom = new CompoundTag();
        }
        CompoundTag root = new CompoundTag();
        root.putString("key", data.key == null ? "" : data.key);
        ListTag tagList = new ListTag();
        ListTag needList = new ListTag();
        ListTag haveList = new ListTag();
        Aspect[] tags = data.tags;
        int[] needs = data.need;
        int[] haves = data.have;
        if (tags != null) {
            int needLen = needs == null ? 0 : needs.length;
            int haveLen = haves == null ? 0 : haves.length;
            for (int i = 0; i < tags.length; i++) {
                Aspect a = tags[i];
                if (a == null) {
                    continue;
                }
                String t = a.getTag();
                if (t == null) {
                    continue;
                }
                tagList.add(StringTag.valueOf(t));
                needList.add(net.minecraft.nbt.IntTag.valueOf(i < needLen ? needs[i] : 0));
                haveList.add(net.minecraft.nbt.IntTag.valueOf(i < haveLen ? haves[i] : 0));
            }
        }
        root.put("tags", tagList);
        root.put("need", needList);
        root.put("have", haveList);
        root.putBoolean("stage1", data.stage1);
        root.putBoolean("stage2", data.stage2);
        CompoundTag fail = new CompoundTag();
        if (data.failed != null) {
            for (Map.Entry<String, Byte> e : data.failed.entrySet()) {
                String k = e.getKey();
                Byte v = e.getValue();
                if (k != null && v != null) {
                    fail.putByte(k, v);
                }
            }
        }
        root.put("fail", fail);
        custom.put(NBT_ROOT, root);
        ArcaneresearchInvHelper.setCustomTag(stack, custom);
        stack.set(net.minecraft.core.component.DataComponents.RARITY,
                data.isComplete() ? net.minecraft.world.item.Rarity.RARE : net.minecraft.world.item.Rarity.UNCOMMON);
    }

    public static DiscoveredNoteData create(String key, AspectList required) {
        DiscoveredNoteData data = new DiscoveredNoteData();
        data.key = key;
        if (required == null || required.aspects.isEmpty()) {
            return data;
        }
        Aspect[] raw = required.getAspects();
        if (raw == null || raw.length == 0) {
            return data;
        }
        int count = 0;
        for (Aspect a : raw) {
            if (a != null) {
                count++;
            }
        }
        if (count == 0) {
            return data;
        }
        Aspect[] filtered = new Aspect[count];
        int[] needs = new int[count];
        int[] haves = new int[count];
        int idx = 0;
        for (Aspect a : raw) {
            if (a == null) {
                continue;
            }
            filtered[idx] = a;
            int amt = required.getAmount(a);
            needs[idx] = Math.max(1, amt);
            haves[idx] = 0;
            idx++;
        }
        data.tags = filtered;
        data.need = needs;
        data.have = haves;
        return data;
    }

    public float getTotalProgress() {
        Aspect[] tags = this.tags;
        if (tags == null) {
            return 0.0F;
        }
        int[] need = this.need;
        int[] have = this.have;
        int needLen = need == null ? 0 : need.length;
        int haveLen = have == null ? 0 : have.length;
        float done = 0.0F;
        float total = 0.0F;
        for (int i = 0; i < tags.length; i++) {
            int want = i < needLen ? need[i] : 0;
            int got = i < haveLen ? Math.min(have[i], want) : 0;
            done += got;
            total += want;
        }
        return total <= 0.0F ? 0.0F : done / total;
    }

    public boolean isComplete() {
        Aspect[] tags = this.tags;
        if (tags == null || tags.length <= 0 || getTotalProgress() < 1.0F) {
            return false;
        }
        return !requiresHex(key) || (stage1 && stage2);
    }

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

    private static arcaneresearch.api.research.ResearchEntry requiringResearch(String discoveryKey) {
        if (discoveryKey == null) {
            return null;
        }
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

    public float getTagProgress(Aspect tag) {
        if (tag == null || tags == null) {
            return 0.0F;
        }
        String tagName = tag.getTag();
        int[] need = this.need;
        int[] have = this.have;
        int needLen = need == null ? 0 : need.length;
        int haveLen = have == null ? 0 : have.length;
        for (int i = 0; i < tags.length; i++) {
            Aspect t = tags[i];
            if (t == null) {
                continue;
            }
            String name = t.getTag();
            boolean match = t == tag || (name != null && name.equals(tagName));
            if (match) {
                int want = i < needLen ? need[i] : 0;
                int got = i < haveLen ? have[i] : 0;
                return want <= 0 ? 0.0F : (float) got / (float) want;
            }
        }
        return 0.0F;
    }

    public byte getFailed(String aspectTag) {
        if (aspectTag == null || failed == null) {
            return 0;
        }
        Byte b = failed.get(aspectTag);
        return b == null ? 0 : b;
    }

    public void addFailed(String aspectTag, byte amount) {
        if (aspectTag == null) {
            return;
        }
        if (failed == null) {
            failed = new HashMap<>();
        }
        int cur = Byte.toUnsignedInt(getFailed(aspectTag)) + Byte.toUnsignedInt(amount);
        failed.put(aspectTag, (byte) Math.min(100, cur));
    }
}