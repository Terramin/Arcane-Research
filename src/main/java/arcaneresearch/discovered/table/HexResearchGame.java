package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;

import net.minecraft.world.entity.player.Player;

public class HexResearchGame {

    public enum PlaceResult {
        OK, NO_HEX, OCCUPIED, NO_POOL
    }

      
    public interface HasAspect {
        boolean has(Aspect aspect);
    }

    public static boolean isKnown(Player player, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        try {
            return ArcaneresearchCapabilities.isResearchKnown(player, "!" + aspect.getTag().toLowerCase());
        } catch (Exception e) {
            return false;
        }
    }

    

    public static HexResearchNoteData createNote(String key, List<Aspect> targets, int color, Random random) {
        HexResearchNoteData data = new HexResearchNoteData();
        data.key = key;
        data.color = color;
        data.complete = false;
        data.copies = 0;
        if (random == null) {
            random = new Random();
        }

        List<Aspect> clean = new ArrayList<>();
        if (targets != null) {
            for (Aspect aspect : targets) {
                if (aspect != null && !clean.contains(aspect)) {
                    clean.add(aspect);
                }
            }
        }

        int complexity = Math.max(1, (clean.size() + 2) / 3);
        int radius = 1 + Math.min(3, complexity);

        HashMap<String, HexUtils.Hex> field = HexUtils.generateHexes(radius);
        for (HexUtils.Hex hex : field.values()) {
            String hk = hex.toString();
            data.hexes.put(hk, hex);
            data.hexEntries.put(hk, new HexEntry(null, 0));
        }

        if (!clean.isEmpty()) {
            ArrayList<HexUtils.Hex> ring = HexUtils.distributeRingRandomly(radius, clean.size(), random);
            final int n = Math.min(clean.size(), ring.size());
            for (int i = 0; i < n; i++) {
                String k = ring.get(i).toString();
                if (data.hexEntries.containsKey(k)) {
                    data.hexEntries.put(k, new HexEntry(clean.get(i), 1));
                }
            }
        }

        
        int blanks = complexity * 2;
        int tries = 0;
        ArrayList<String> keys = new ArrayList<>(data.hexes.keySet());
        while (blanks > 0 && tries < 200 && !keys.isEmpty()) {
            tries++;
            int idx = random.nextInt(keys.size());
            String k = keys.get(idx);
            HexEntry entry = data.hexEntries.get(k);
            if (entry == null || entry.type != 0) {
                continue;
            }
            HexUtils.Hex cell = data.hexes.get(k);
            if (cell == null || !canRemove(data, cell)) {
                continue;
            }
            data.hexes.remove(k);
            data.hexEntries.remove(k);
            int last = keys.size() - 1;
            keys.set(idx, keys.get(last));
            keys.remove(last);
            blanks--;
        }

        
        
        
        return data;
    }

     



 
    private static boolean canRemove(HexResearchNoteData data, HexUtils.Hex cell) {
        final int[][] nb = HexUtils.NEIGHBOURS;
        for (int d = 0; d < 6; d++) {
            int nq = cell.q + nb[d][0];
            int nr = cell.r + nb[d][1];
            HexEntry entry = data.hexEntries.get(nq + ":" + nr);
            if (entry == null || entry.type != 1) {
                continue;
            }
            int count = 0;
            for (int d2 = 0; d2 < 6; d2++) {
                int nnq = nq + nb[d2][0];
                int nnr = nr + nb[d2][1];
                if (data.hexes.containsKey(nnq + ":" + nnr)) {
                    count++;
                    if (count >= 2) {
                        break;
                    }
                }
            }
            if (count < 2) {
                return false;
            }
        }
        return true;
    }

    

     


 
    public static PlaceResult placeAspect(HexResearchNoteData data, int q, int r, Aspect aspect) {
        if (data == null || data.complete || aspect == null) {
            return PlaceResult.NO_HEX;
        }
        String k = q + ":" + r;
        if (!data.hexes.containsKey(k)) {
            return PlaceResult.NO_HEX;
        }
        HexEntry entry = data.hexEntries.get(k);
        if (entry == null || entry.type != 0) {
            return PlaceResult.OCCUPIED;
        }
        data.hexEntries.put(k, new HexEntry(aspect, 2));
        return PlaceResult.OK;
    }

      
    public static PlaceResult placeAspect(HexResearchNoteData data, AspectList pool,
            int q, int r, Aspect aspect) {
        if (aspect == null) {
            return PlaceResult.NO_HEX;
        }
        if (pool == null || pool.getAmount(aspect) <= 0) {
            return PlaceResult.NO_POOL;
        }
        PlaceResult result = placeAspect(data, q, r, aspect);
        if (result == PlaceResult.OK) {
            pool.remove(aspect, 1);
        }
        return result;
    }

     



 
    public static Aspect eraseAspect(HexResearchNoteData data, int q, int r) {
        if (data == null || data.complete) {
            return null;
        }
        String k = new HexUtils.Hex(q, r).toString();
        HexEntry entry = data.hexEntries.get(k);
        if (entry == null || entry.type != 2) {
            return null;
        }
        Aspect removed = entry.aspect;
        data.hexEntries.put(k, new HexEntry(null, 0));
        return removed;
    }

    
    

    

     



 
    public static boolean checkCompletion(HexResearchNoteData data, HasAspect has) {
        if (data == null || data.complete) {
            return data != null;
        }
        Set<String> main = new HashSet<>();
        for (Map.Entry<String, HexEntry> e : data.hexEntries.entrySet()) {
            HexEntry entry = e.getValue();
            if (entry != null && entry.type == 1) {
                main.add(e.getKey());
            }
        }
        Set<String> checked = new HashSet<>();
        Set<String> remains = new HashSet<>();
        if (!main.isEmpty()) {
            HexUtils.Hex start = data.hexes.get(main.iterator().next());
            if (start != null) {
                checkConnections(data, start, checked, main, remains, has);
            }
        }
        if (!main.isEmpty()) {
            return false;
        }
        Iterator<Map.Entry<String, HexEntry>> it = data.hexEntries.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, HexEntry> e = it.next();
            HexEntry entry = e.getValue();
            if (entry != null && entry.type != 1 && !remains.contains(e.getKey())) {
                it.remove();
                data.hexes.remove(e.getKey());
            }
        }
        data.complete = true;
        return true;
    }

    private static void checkConnections(HexResearchNoteData data, HexUtils.Hex hex,
                                         Set<String> checked, Set<String> main,
                                         Set<String> remains, HasAspect has) {
        String me = hex.toString();
        if (!checked.add(me)) {
            return;
        }
        remains.add(me);
        HexEntry entry = data.hexEntries.get(me);
        if (entry == null || entry.aspect == null) {
            return;
        }
        if (entry.type == 1) {
            main.remove(me);
        }
        boolean mineKnown = has == null || has.has(entry.aspect);
        for (int d = 0; d < 6; d++) {
            HexUtils.Hex neighbour = hex.getNeighbour(d);
            String nk = neighbour.toString();
            HexEntry other = data.hexEntries.get(nk);
            if (other == null || other.aspect == null || other.type < 1) {
                continue;
            }
            if (checked.contains(nk)) {
                continue;
            }
            boolean otherKnown = has == null || has.has(other.aspect);
            if (!mineKnown || !otherKnown) {
                continue;
            }
            if (linked(entry.aspect, other.aspect)) {
                checkConnections(data, neighbour, checked, main, remains, has);
            }
        }
    }

      
    public static boolean linked(Aspect a1, Aspect a2) {
        if (a1 == null || a2 == null) {
            return false;
        }
        if (!a1.isPrimal()) {
            Aspect[] c1 = a1.getComponents();
            if (c1 != null && c1.length >= 2 && (c1[0] == a2 || c1[1] == a2)) {
                return true;
            }
        }
        if (!a2.isPrimal()) {
            Aspect[] c2 = a2.getComponents();
            return c2 != null && c2.length >= 2 && (c2[0] == a1 || c2[1] == a1);
        }
        return false;
    }

     


 
    public static void visualize(HexResearchNoteData data, HasAspect has,
            HashMap<String, HexUtils.Hex[]> lines, ArrayList<String> highlight) {
        if (lines == null || highlight == null) {
            return;
        }
        lines.clear();
        highlight.clear();
        if (data == null) {
            return;
        }
        for (Map.Entry<String, HexEntry> e : data.hexEntries.entrySet()) {
            HexEntry entry = e.getValue();
            HexUtils.Hex hex = data.hexes.get(e.getKey());
            if (entry == null || hex == null || entry.type != 1 || entry.aspect == null) {
                continue;
            }
            if (has != null && !has.has(entry.aspect)) {
                continue;
            }
            Set<String> checked = new HashSet<>();
            visualizeFrom(data, hex, checked, lines, highlight, has);
        }
    }

    private static void visualizeFrom(HexResearchNoteData data, HexUtils.Hex hex,
                                      Set<String> checked, HashMap<String, HexUtils.Hex[]> lines,
                                      ArrayList<String> highlight, HasAspect has) {
        String me = hex.toString();
        if (!checked.add(me)) {
            return;
        }
        HexEntry entry = data.hexEntries.get(me);
        if (entry == null || entry.aspect == null) {
            return;
        }
        for (int d = 0; d < 6; d++) {
            HexUtils.Hex neighbour = hex.getNeighbour(d);
            String nk = neighbour.toString();
            HexEntry other = data.hexEntries.get(nk);
            if (other == null || other.aspect == null || other.type < 1) {
                continue;
            }
            if (checked.contains(nk)) {
                continue;
            }
            if (has != null && (!has.has(entry.aspect) || !has.has(other.aspect))) {
                continue;
            }
            if (linked(entry.aspect, other.aspect)) {
                lines.put(me + ":" + nk, new HexUtils.Hex[]{hex, neighbour});
                if (!highlight.contains(nk)) {
                    highlight.add(nk);
                }
                visualizeFrom(data, neighbour, checked, lines, highlight, has);
            }
        }
    }
}
