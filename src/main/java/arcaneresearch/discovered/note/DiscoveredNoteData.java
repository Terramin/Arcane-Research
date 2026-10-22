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
        if (stack.isEmpty()) {
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
                continue; 
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
        
        data.stage2 = root.getBooleanOr("stage2", false) || root.getBooleanOr("hex", false);
        return data;
    }

      
    public static void write(ItemStack stack, DiscoveredNoteData data) {
        
        
        
        
        
        
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
        
        
        stack.set(net.minecraft.core.component.DataComponents.RARITY,
                data.isComplete() ? net.minecraft.world.item.Rarity.RARE : net.minecraft.world.item.Rarity.UNCOMMON);
    }

      
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

      
    public byte getFailed(String aspectTag) {
        Byte b = failed.get(aspectTag);
        return b == null ? 0 : b.byteValue();
    }

    public void addFailed(String aspectTag, byte amount) {
        int cur = Byte.toUnsignedInt(getFailed(aspectTag)) + Byte.toUnsignedInt(amount);
        failed.put(aspectTag, (byte) Math.min(100, cur));
    }
}
