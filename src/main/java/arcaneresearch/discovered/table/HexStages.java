package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.discovered.table.DiscoveredTableResearch;

 









 
public class HexStages {

      
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
                if (entry == null || entry.getStages() == null) {
                    continue;
                }
                ResearchStage[] stages = entry.getStages();
                boolean hex = false;
                for (int i = 0; i < stages.length; i++) {
                    if (stages[i] != null && stages[i].isHex()) {
                        hex = true;
                        break;
                    }
                }
                if (!hex || knowledge.isResearchComplete(entry.getKey())) {
                    continue;
                }
                if (!ResearchManager.doesPlayerHaveRequisites(player, entry.getKey())) {
                    continue;
                }
                if (!out.contains(entry.getKey())) {
                    out.add(entry.getKey());
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

      
    public static List<Aspect> targetsFor(String researchKey) {
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
        if (!targets.isEmpty() && targets.get(0) != null) {
            try {
                return targets.get(0).getColor();
            } catch (Exception ignored) {
            }
        }
        return 0x888888;
    }
}
