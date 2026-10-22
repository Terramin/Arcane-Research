package arcaneresearch.common.lib.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;

import javax.annotation.Nonnull;
import java.util.*;








public class PlayerKnowledge {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "knowledge");

    public static class DefaultImpl implements IPlayerKnowledge {

        private final HashSet<String> research = new HashSet<>();
        private final Map<String, Integer> stages = new HashMap<>();
        private final Map<String, HashSet<EnumResearchFlag>> flags = new HashMap<>();
        private final Map<String, Integer> knowledge = new HashMap<>();
        
        private final HashSet<String> presentedDiscoveries = new HashSet<>();

        @Override
        public boolean addPresentedDiscovery(@Nonnull String key) {
            if (key == null || key.isEmpty()) {
                return false;
            }
            return presentedDiscoveries.add(key);
        }

        @Override
        public boolean hasPresentedDiscovery(@Nonnull String key) {
            return key != null && presentedDiscoveries.contains(key);
        }

        @Override
        public boolean consumePresentedDiscovery(@Nonnull String key) {
            return key != null && presentedDiscoveries.remove(key);
        }

        @Override
        public void clearPresentedDiscoveries() {
            presentedDiscoveries.clear();
        }

        @Override
        public void clear() {
            research.clear();
            flags.clear();
            stages.clear();
            knowledge.clear();
            presentedDiscoveries.clear();
        }

        @Override
        public EnumResearchStatus getResearchStatus(@Nonnull String res) {
            if (!isResearchKnown(res)) {
                return EnumResearchStatus.UNKNOWN;
            }
            ResearchEntry entry = ResearchCategories.getResearch(res);
            if (entry == null || entry.getStages() == null || getResearchStage(res) > entry.getStages().length) {
                return EnumResearchStatus.COMPLETE;
            }
            return EnumResearchStatus.IN_PROGRESS;
        }

        @Override
        public boolean isResearchKnown(String res) {
            if (res == null) {
                return false;
            }
            if (res.isEmpty()) {
                return true;
            }
            String[] ss = res.split("@");
            return (ss.length <= 1 || getResearchStage(ss[0]) >= parseInt(ss[1], 0)) && research.contains(ss[0]);
        }

        private int parseInt(String s, int defaultValue) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        @Override
        public boolean isResearchComplete(String res) {
            return getResearchStatus(res) == EnumResearchStatus.COMPLETE;
        }

        @Override
        public int getResearchStage(String res) {
            if (res == null || !research.contains(res)) {
                return -1;
            }
            Integer stage = stages.get(res);
            return (stage == null) ? 0 : stage;
        }

        @Override
        public boolean setResearchStage(String res, int stage) {
            if (res == null || !research.contains(res) || stage <= 0) {
                return false;
            }
            stages.put(res, stage);
            return true;
        }

        @Override
        public boolean addResearch(@Nonnull String res) {
            if (!isResearchKnown(res)) {
                research.add(res);
                return true;
            }
            return false;
        }

        @Override
        public boolean removeResearch(@Nonnull String res) {

            if (isResearchKnown(res)) {
                research.remove(res);
                
                stages.remove(res);
                flags.remove(res);
                return true;
            }
            return false;
        }

        @Nonnull
        @Override
        public Set<String> getResearchList() {
            return Collections.unmodifiableSet(research);
        }

        @Override
        public boolean setResearchFlag(@Nonnull String res, @Nonnull EnumResearchFlag flag) {
            HashSet<EnumResearchFlag> list = flags.computeIfAbsent(res, k -> new HashSet<>());
            if (list.contains(flag)) {
                return false;
            }
            list.add(flag);
            return true;
        }

        @Override
        public boolean clearResearchFlag(@Nonnull String res, @Nonnull EnumResearchFlag flag) {
            HashSet<EnumResearchFlag> list = flags.get(res);
            if (list != null) {
                boolean b = list.remove(flag);
                if (list.isEmpty()) {
                    flags.remove(res);
                }
                return b;
            }
            return false;
        }

        @Override
        public boolean hasResearchFlag(@Nonnull String res, @Nonnull EnumResearchFlag flag) {
            return flags.get(res) != null && flags.get(res).contains(flag);
        }

        private String getKey(EnumKnowledgeType type, String category) {
            return type.getAbbreviation() + "_" + (category == null ? "" : category);
        }

        @Override
        public boolean addKnowledge(EnumKnowledgeType type, String category, int amount) {
            String key = getKey(type, category);
            int c = getKnowledgeRaw(type, category);
            if (c + amount < 0) {
                return false;
            }
            c += amount;
            knowledge.put(key, c);
            return true;
        }

        @Override
        public int getKnowledge(EnumKnowledgeType type, String category) {
            String key = getKey(type, category);
            int c = knowledge.getOrDefault(key, 0);
            return (int) Math.floor(c / (double) type.getProgression());
        }

        @Override
        public int getKnowledgeRaw(EnumKnowledgeType type, String category) {
            String key = getKey(type, category);
            return knowledge.getOrDefault(key, 0);
        }

        @Override
        public void sync(@Nonnull ServerPlayer player) {
            arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                    new arcaneresearch.common.lib.network.playerdata.PacketSyncKnowledge(player),
                    player
            );
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag rootTag = new CompoundTag();

            ListTag researchList = new ListTag();
            for (String resKey : research) {
                CompoundTag tag = new CompoundTag();
                tag.putString("key", resKey);
                if (stages.containsKey(resKey)) {
                    tag.putInt("stage", stages.get(resKey));
                }
                if (flags.containsKey(resKey)) {
                    HashSet<EnumResearchFlag> list = flags.get(resKey);
                    if (list != null && !list.isEmpty()) {
                        StringBuilder fs = new StringBuilder();
                        for (EnumResearchFlag flag : list) {
                            if (fs.length() > 0) {
                                fs.append(",");
                            }
                            fs.append(flag.name());
                        }
                        tag.putString("flags", fs.toString());
                    }
                }
                researchList.add(tag);
            }
            rootTag.put("research", researchList);

            ListTag knowledgeList = new ListTag();
            for (Map.Entry<String, Integer> entry : knowledge.entrySet()) {
                String key = entry.getKey();
                Integer c = entry.getValue();
                if (c != null && c > 0 && key != null && !key.isEmpty()) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("key", key);
                    tag.putInt("amount", c);
                    knowledgeList.add(tag);
                }
            }
            rootTag.put("knowledge", knowledgeList);

            
            ListTag presentedList = new ListTag();
            for (String key : presentedDiscoveries) {
                if (key != null && !key.isEmpty()) {
                    CompoundTag tag = new CompoundTag();
                    tag.putString("key", key);
                    presentedList.add(tag);
                }
            }
            rootTag.put("presented", presentedList);

            return rootTag;
        }

        @Override
        public void deserializeNBT(CompoundTag rootTag) {
            if (rootTag == null) {
                return;
            }
            clear();

            ListTag researchList = rootTag.getListOrEmpty("research");
            for (int i = 0; i < researchList.size(); i++) {
                CompoundTag tag = researchList.getCompoundOrEmpty(i);
                String know = tag.getStringOr("key", "");
                if (know != null && !know.isEmpty() && !isResearchKnown(know)) {

                    if (know.equals("!gotcrystals")) {
                        know = "!gotcrystal";
                        if (isResearchKnown(know)) {
                            continue;
                        }
                    }
                    research.add(know);
                    int stage = tag.getIntOr("stage", 0);
                    if (stage > 0) {
                        stages.put(know, stage);
                    }
                    String fs = tag.getStringOr("flags", "");
                    if (!fs.isEmpty()) {
                        String[] ss = fs.split(",");
                        for (String s : ss) {
                            try {
                                EnumResearchFlag flag = EnumResearchFlag.valueOf(s);
                                setResearchFlag(know, flag);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }

            ListTag knowledgeList = rootTag.getListOrEmpty("knowledge");
            for (int j = 0; j < knowledgeList.size(); j++) {
                CompoundTag tag = knowledgeList.getCompoundOrEmpty(j);
                String key = tag.getStringOr("key", "");
                int amount = tag.getIntOr("amount", 0);
                if (key != null && !key.isEmpty()) {
                    knowledge.put(key, amount);
                }
            }

            
            ListTag presentedList = rootTag.getListOrEmpty("presented");
            for (int j = 0; j < presentedList.size(); j++) {
                String key = presentedList.getCompoundOrEmpty(j).getStringOr("key", "");
                if (key != null && !key.isEmpty()) {
                    presentedDiscoveries.add(key);
                }
            }

            addAutoUnlockResearch();
        }

        
        
        
        
        @Override
        public void serialize(ValueOutput output) {
            var researchList = output.childrenList("research");
            for (String resKey : research) {
                var tag = researchList.addChild();
                tag.putString("key", resKey);
                if (stages.containsKey(resKey)) {
                    tag.putInt("stage", stages.get(resKey));
                }
                if (flags.containsKey(resKey)) {
                    HashSet<EnumResearchFlag> list = flags.get(resKey);
                    if (list != null && !list.isEmpty()) {
                        StringBuilder fs = new StringBuilder();
                        for (EnumResearchFlag flag : list) {
                            if (fs.length() > 0) {
                                fs.append(",");
                            }
                            fs.append(flag.name());
                        }
                        tag.putString("flags", fs.toString());
                    }
                }
            }

            var knowledgeList = output.childrenList("knowledge");
            for (Map.Entry<String, Integer> entry : knowledge.entrySet()) {
                String key = entry.getKey();
                Integer c = entry.getValue();
                if (c != null && c > 0 && key != null && !key.isEmpty()) {
                    var tag = knowledgeList.addChild();
                    tag.putString("key", key);
                    tag.putInt("amount", c);
                }
            }

            
            var presentedList = output.childrenList("presented");
            for (String key : presentedDiscoveries) {
                if (key != null && !key.isEmpty()) {
                    var tag = presentedList.addChild();
                    tag.putString("key", key);
                }
            }
        }

        @Override
        public void deserialize(ValueInput input) {
            clear();

            for (var tag : input.childrenListOrEmpty("research")) {
                String know = tag.getStringOr("key", "");
                if (know != null && !know.isEmpty() && !isResearchKnown(know)) {

                    if (know.equals("!gotcrystals")) {
                        know = "!gotcrystal";
                        if (isResearchKnown(know)) {
                            continue;
                        }
                    }
                    research.add(know);
                    int stage = tag.getIntOr("stage", 0);
                    if (stage > 0) {
                        stages.put(know, stage);
                    }
                    String fs = tag.getStringOr("flags", "");
                    if (!fs.isEmpty()) {
                        String[] ss = fs.split(",");
                        for (String s : ss) {
                            try {
                                EnumResearchFlag flag = EnumResearchFlag.valueOf(s);
                                setResearchFlag(know, flag);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                }
            }

            for (var tag : input.childrenListOrEmpty("knowledge")) {
                String key = tag.getStringOr("key", "");
                int amount = tag.getIntOr("amount", 0);
                if (key != null && !key.isEmpty()) {
                    knowledge.put(key, amount);
                }
            }

            
            for (var tag : input.childrenListOrEmpty("presented")) {
                String key = tag.getStringOr("key", "");
                if (key != null && !key.isEmpty()) {
                    presentedDiscoveries.add(key);
                }
            }

            addAutoUnlockResearch();
        }

        private void addAutoUnlockResearch() {
            for (ResearchCategory cat : ResearchCategories.researchCategories.values()) {
                for (ResearchEntry ri : cat.research.values()) {
                    if (ri.hasMeta(ResearchEntry.EnumResearchMeta.AUTOUNLOCK)) {
                        addResearch(ri.getKey());
                    }
                }
            }
        }
    }
}
