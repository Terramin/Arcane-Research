package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.List;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.lib.research.ResearchManager;
import net.minecraft.world.entity.player.Player;

public class HexStages {
    private static final int DEFAULT_COLOR = 0x888888;

    public static String findOpenable(Player player) {
        List<String> keys = openableKeys(player);
        return keys.isEmpty() ? null : keys.get(0);
    }

    public static List<String> openableKeys(Player player) {
        ArrayList<String> out = new ArrayList<>();
        if (player == null) {
            return out;
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return out;
        }
        try {
            for (ResearchEntry entry : DiscoveredTableResearch.allEntries()) {
                if (entry == null) {
                    continue;
                }
                ResearchStage[] stages = entry.getStages();
                if (stages == null) {
                    continue;
                }
                String key = entry.getKey();
                if (key == null || key.isEmpty()) {
                    continue;
                }
                boolean hex = false;
                for (ResearchStage stage : stages) {
                    if (stage != null && stage.isHex()) {
                        hex = true;
                        break;
                    }
                }
                if (!hex || knowledge.isResearchComplete(key)) {
                    continue;
                }
                if (!ResearchManager.doesPlayerHaveRequisites(player, key)) {
                    continue;
                }
                if (!out.contains(key)) {
                    out.add(key);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static List<Aspect> targetsFor(String researchKey) {
        if (researchKey == null || researchKey.isEmpty()) {
            return new ArrayList<>(0);
        }
        ArrayList<Aspect> out = new ArrayList<>();
        try {
            AspectList required = DiscoveredTableResearch.requiredAspects(researchKey);
            if (required != null) {
                for (Aspect aspect : required.getAspects()) {
                    if (aspect != null && !out.contains(aspect)) {
                        out.add(aspect);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static int colorFor(String researchKey) {
        List<Aspect> targets = targetsFor(researchKey);
        if (!targets.isEmpty()) {
            try {
                Aspect first = targets.get(0);
                if (first != null) {
                    return first.getColor();
                }
            } catch (Exception ignored) {
            }
        }
        return DEFAULT_COLOR;
    }
}