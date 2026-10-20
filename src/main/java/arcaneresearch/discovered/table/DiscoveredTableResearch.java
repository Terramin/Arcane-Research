package arcaneresearch.discovered.table;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.note.DiscoveredNotes;
public final class DiscoveredTableResearch {
    private DiscoveredTableResearch() {
    }
    private static List<String> orderedTags;
    public static synchronized List<String> orderedTags() {
        if (orderedTags == null) {
            orderedTags = new ArrayList<>(new TreeSet<>(Aspect.aspects.keySet()));
        }
        return orderedTags;
    }
    public static int aspectIndex(Aspect aspect) {
        if (aspect == null) {
            return 0;
        }
        int i = orderedTags().indexOf(aspect.getTag());
        return i < 0 ? 0 : i + 1;
    }
    public static Aspect aspectByIndex(int index) {
        List<String> order = orderedTags();
        if (index <= 0 || index > order.size()) {
            return null;
        }
        return Aspect.getAspect(order.get(index - 1));
    }
    public static AspectList requiredAspects(String key) {
        AspectList exact = DiscoveredResearchAspects.get(key);
        if (exact != null && exact.size() > 0) {
            return exact;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry == null) {
            entry = requiringResearch(key);
        }
        if (entry == null || entry.getCategory() == null) {
            return null;
        }
        ResearchCategory category = ResearchCategories.getResearchCategory(entry.getCategory());
        if (category == null || category.formula == null || category.formula.size() <= 0) {
            return null;
        }
        return category.formula.copy();
    }
    public static ResearchEntry requiringResearch(String discoveryKey) {
        if (discoveryKey == null) {
            return null;
        }
        for (ResearchEntry entry : allEntries()) {
            if (entry.getStages() == null) {
                continue;
            }
            for (ResearchStage stage : entry.getStages()) {
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
        return null;
    }
    public static boolean hasDiscoveryRequirement(ResearchEntry entry) {
        if (entry == null || entry.getStages() == null) {
            return false;
        }
        for (ResearchStage stage : entry.getStages()) {
            if (stage != null && stage.getDiscovery() != null && stage.getDiscovery().length > 0) {
                return true;
            }
        }
        return false;
    }
    public static boolean isDiscoverySpent(Player player, String discoveryKey) {
        if (player == null || discoveryKey == null) {
            return false;
        }
        if (ArcaneresearchCapabilities.getKnowledge(player)
                .map(k -> k.hasPresentedDiscovery(discoveryKey)).orElse(false)) {
            return true;
        }
        boolean needed = false;
        for (ResearchEntry entry : allEntries()) {
            if (entry.getStages() == null) {
                continue;
            }
            int current = ArcaneresearchCapabilities.getKnowledge(player)
                    .map(k -> k.getResearchStage(entry.getKey()) - 1).orElse(-2);
            ResearchStage[] stages = entry.getStages();
            for (int i = 0; i < stages.length; i++) {
                if (stages[i] == null || stages[i].getDiscovery() == null) {
                    continue;
                }
                for (String d : stages[i].getDiscovery()) {
                    if (discoveryKey.equals(d)) {
                        if (current <= i) {
                            return false;
                        }
                        needed = true;
                    }
                }
            }
        }
        return needed;
    }
    public static List<String> discoveryKeys() {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (ResearchEntry entry : allEntries()) {
            if (entry.getStages() == null) {
                continue;
            }
            for (ResearchStage stage : entry.getStages()) {
                if (stage == null || stage.getDiscovery() == null) {
                    continue;
                }
                for (String d : stage.getDiscovery()) {
                    if (d != null && !d.isEmpty()) {
                        out.add(d);
                    }
                }
            }
        }
        return new ArrayList<>(out);
    }
    public static List<ResearchEntry> allEntries() {
        List<ResearchEntry> out = new ArrayList<>();
        for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
            if (category.research != null) {
                out.addAll(category.research.values());
            }
        }
        return out;
    }
    public static String findMatchingResearch(Player player, Aspect[] tags, int[] amounts) {
        int bestMatch = 0;
        String best = null;
        for (ResearchEntry entry : allEntries()) {
            if (entry.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN)) {
                continue;
            }
            if (ArcaneresearchCapabilities.isResearchComplete(player, entry.getKey())) {
                continue;
            }
            if (!hasDiscoveryRequirement(entry)) {
                continue;
            }
            if (isDiscoverySpent(player, entry.getKey())) {
                continue;
            }
            if (!ResearchManager.doesPlayerHaveRequisites(player, entry.getKey())) {
                continue;
            }
            AspectList required = requiredAspects(entry.getKey());
            if (required == null) {
                continue;
            }
            int match = 0;
            for (int i = 0; i < 5 && i < tags.length && i < amounts.length; i++) {
                if (tags[i] != null && amounts[i] > 0 && required.getAmount(tags[i]) > 0) {
                    match++;
                }
            }
            if (match > 0 && match > bestMatch) {
                bestMatch = match;
                best = entry.getKey();
            }
        }
        return best;
    }
    public static String findLostResearch(Player player) {
        List<String> choices = new ArrayList<>();
        for (ResearchEntry entry : allEntries()) {
            if (!entry.hasMeta(ResearchEntry.EnumResearchMeta.HIDDEN)) {
                continue;
            }
            if (ArcaneresearchCapabilities.isResearchComplete(player, entry.getKey())) {
                continue;
            }
            if (!hasDiscoveryRequirement(entry)) {
                continue;
            }
            if (isDiscoverySpent(player, entry.getKey())) {
                continue;
            }
            if (!ResearchManager.doesPlayerHaveRequisites(player, entry.getKey())) {
                continue;
            }
            choices.add(entry.getKey());
        }
        if (choices.isEmpty()) {
            return null;
        }
        return choices.get(player.getRandom().nextInt(choices.size()));
    }
    public static int researchIterations(int amount) {
        return 1 + amount / 2;
    }
    public static boolean progressTableResearch(Level level, Player researcher, ItemStack note,
            int baseChance, int baseLoss, Aspect[] inTags, int[] inAmounts) {
        if (baseLoss <= 0) {
            baseLoss = 1;
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(note);
        if (data.key == null) {
            String key = findLostResearch(researcher);
            AspectList required = key == null ? null : requiredAspects(key);
            if (key == null || required == null) {
                return false;
            }
            DiscoveredNoteData.write(note, DiscoveredNoteData.create(key, required));
            data = DiscoveredNoteData.read(note);
        } else if (ArcaneresearchCapabilities.isResearchComplete(researcher, data.key)
                || isDiscoverySpent(researcher, data.key)) {
            return false;
        }
        for (Aspect tag : inTags) {
            if (tag != null && !contains(data.tags, tag) && data.getFailed(tag.getTag()) < 100) {
                data.addFailed(tag.getTag(),
                        (byte) Math.round((2 + level.getRandom().nextInt(baseLoss)) / 10.0F));
            }
        }
        boolean progressed = false;
        for (int t = 0; t < data.tags.length; t++) {
            Aspect tag = data.tags[t];
            if (tag == null) {
                continue;
            }
            int entry = -1;
            for (int i = 0; i < inTags.length; i++) {
                if (inTags[i] == tag) {
                    entry = i;
                    break;
                }
            }
            if (entry < 0) {
                continue;
            }
            float chance = baseChance / 100.0F;
            int tries = researchIterations(entry < inAmounts.length ? inAmounts[entry] : 0);
            for (int a = 0; a < tries; a++) {
                if (level.getRandom().nextFloat() <= chance) {
                    int want = t < data.need.length ? data.need[t] : 0;
                    int got = t < data.have.length ? data.have[t] : 0;
                    if (got < want) {
                        data.have[t] = got + 1;
                        chance *= 0.9F;
                        progressed = true;
                        break;
                    }
                }
            }
        }
        if (data.getTotalProgress() >= 1.0F) {
            data.stage1 = true;
        }
        DiscoveredNoteData.write(note, data);
        return progressed;
    }
    private static boolean contains(Aspect[] tags, Aspect tag) {
        for (Aspect t : tags) {
            if (t == tag) {
                return true;
            }
        }
        return false;
    }
}
