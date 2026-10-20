package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.world.entity.player.Player;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.discovered.table.HexResearchNoteData.HexEntry;

/**
 * Порт правил мини-игры исследовательского стола TC 4.2.3.5
 * ({@code ResearchManager.createNote/checkResearchCompletion/checkConnections/
 * getCombinationResult} + {@code TileResearchTable.placeAspect}).
 *
 * Адаптации под проект 1.20.1 (всё задокументировано):
 * <ul>
 *   <li>Сложность исследования ({@code complexity}) заменена выводом из числа
 *   целевых аспектов: {@code complexity = max(1, (targets+2)/3)},
 *   {@code radius = 1+min(3, complexity)} — та же формула поля и дырок.</li>
 *   <li>Очки аспектов — в хранилище игрока (кошелёк + бонус тайла,
 *   см. {@link HexEconomy}), а не в записке.</li>
 *   <li>Чернила тратятся на постановку/стирание; стирание без возврата
 *   (паритет: рефанд только с перками, которых нет).</li>
 *   <li>Комбинация списывает попытку всегда (паритет); знание аспекта
 *   проверяется через {@code isResearchKnown(player, "!" + tag)} — та же
 *   проверка, что в {@code ResearchPageScreen} для известных игроку
 *   аспектов.</li>
 * </ul>
 */
public class HexResearchGame {

    public enum PlaceResult {
        OK, NO_HEX, OCCUPIED, NO_POOL
    }

    /** Проверка «аспект доступен» (игрок уже подставлен вызывающим). */
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

    // ---- генерация записки: порт ResearchManager.createNote ----

    public static HexResearchNoteData createNote(String key, List<Aspect> targets, int color, Random random) {
        HexResearchNoteData data = new HexResearchNoteData();
        data.key = key;
        data.color = color;
        data.complete = false;
        data.copies = 0;

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
            data.hexes.put(hex.toString(), hex);
            data.hexEntries.put(hex.toString(), new HexEntry(null, 0));
        }

        if (!clean.isEmpty()) {
            ArrayList<HexUtils.Hex> ring = HexUtils.distributeRingRandomly(radius, clean.size(), random);
            for (int i = 0; i < clean.size() && i < ring.size(); i++) {
                String k = ring.get(i).toString();
                if (data.hexEntries.containsKey(k)) {
                    data.hexEntries.put(k, new HexEntry(clean.get(i), 1));
                }
            }
        }

        // Дырки: порт blanks=complexity*2 с защитой от изоляции целей.
        int blanks = complexity * 2;
        int tries = 0;
        ArrayList<String> keys = new ArrayList<>(data.hexes.keySet());
        while (blanks > 0 && tries < 200 && !keys.isEmpty()) {
            tries++;
            String k = keys.get(random.nextInt(keys.size()));
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
            keys.remove(k);
            blanks--;
        }

        // Пул записки не сеем (паритет: createNote пула не выдаёт).
        // Поле NBT pool оставлено для чтения старых записок.
        // Пул записки удалён: очки — в хранилище игрока (кошелёк/бонус).
        return data;
    }

    /**
     * Клетку можно убрать, если ни одна соседняя цель не останется с &lt;2
     * соседями. Паритет со строками 370-385: кандидат считается (не
     * исключается), требуется cc &gt;= 2.
     */
    private static boolean canRemove(HexResearchNoteData data, HexUtils.Hex cell) {
        for (int d = 0; d < 6; d++) {
            HexUtils.Hex neighbour = cell.getNeighbour(d);
            HexEntry entry = data.hexEntries.get(neighbour.toString());
            if (entry == null || entry.type != 1) {
                continue;
            }
            int count = 0;
            for (int d2 = 0; d2 < 6; d2++) {
                HexUtils.Hex nn = neighbour.getNeighbour(d2);
                if (data.hexes.containsKey(nn.toString())) {
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

    // ---- постановка / стирание: порт TileResearchTable.placeAspect ----

    /**
     * Постановка на свободный гекс (только поле; списание — кошелёк/бонус
     * у вызывателя, как {@code placeAspect} после выбора источника).
     */
    public static PlaceResult placeAspect(HexResearchNoteData data, int q, int r, Aspect aspect) {
        if (data == null || data.complete || aspect == null) {
            return PlaceResult.NO_HEX;
        }
        String k = new HexUtils.Hex(q, r).toString();
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

    /** Совместимость локального демо (кошелёк — пул записки). */
    public static PlaceResult placeAspect(HexResearchNoteData data, AspectList pool,
            int q, int r, Aspect aspect) {
        if (pool == null || pool.getAmount(aspect) <= 0) {
            return PlaceResult.NO_POOL;
        }
        PlaceResult result = placeAspect(data, q, r, aspect);
        if (result == PlaceResult.OK) {
            pool.remove(aspect, 1);
        }
        return result;
    }

    /**
     * Стирание руны игрока (type 2 -&gt; 0). Возвращает снятый аспект.
     * Возврата в пул НЕТ (паритет: рефанд только с перками RESEARCHER,
     * которых нет); чернила тратятся всегда — у вызывателя.
     */
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

    // ---- комбинирование соединением занимается MsgCombine (кошелёк/бонус),
    // чистый результат — AspectHelper.getCombinationResult напрямую ----

    // ---- победа: порт ResearchManager.checkResearchCompletion ----

    /**
     * DFS связности целей. При победе чистит мусорные гексы, ставит
     * {@code complete=true}. Не трогает предметы/знания (это серверная
     * привязка — вопрос отдельно).
     */
    public static boolean checkCompletion(HexResearchNoteData data, HasAspect has) {
        if (data == null || data.complete) {
            return data != null && data.complete;
        }
        ArrayList<String> main = new ArrayList<>();
        for (String k : data.hexEntries.keySet()) {
            HexEntry entry = data.hexEntries.get(k);
            if (entry != null && entry.type == 1) {
                main.add(k);
            }
        }
        ArrayList<String> checked = new ArrayList<>();
        ArrayList<String> remains = new ArrayList<>();
        if (!main.isEmpty()) {
            HexUtils.Hex start = data.hexes.get(main.get(0));
            if (start != null) {
                checkConnections(data, start, checked, main, remains, has);
            }
        }
        if (main.isEmpty()) {
            for (String k : new ArrayList<>(data.hexEntries.keySet())) {
                HexEntry entry = data.hexEntries.get(k);
                if (entry != null && entry.type != 1 && !remains.contains(k)) {
                    data.hexEntries.remove(k);
                    data.hexes.remove(k);
                }
            }
            data.complete = true;
            return true;
        }
        return false;
    }

    private static void checkConnections(HexResearchNoteData data, HexUtils.Hex hex,
            ArrayList<String> checked, ArrayList<String> main,
            ArrayList<String> remains, HasAspect has) {
        String me = hex.toString();
        if (checked.contains(me)) {
            return;
        }
        checked.add(me);
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

    /** Правило связи: один — не-примал и второй — его компонента (в любую сторону). */
    public static boolean linked(Aspect a1, Aspect a2) {
        if (a1 == null || a2 == null) {
            return false;
        }
        if (!a1.isPrimal() && a1.getComponents() != null && a1.getComponents().length >= 2
                && (a1.getComponents()[0] == a2 || a1.getComponents()[1] == a2)) {
            return true;
        }
        return !a2.isPrimal() && a2.getComponents() != null && a2.getComponents().length >= 2
                && (a2.getComponents()[0] == a1 || a2.getComponents()[1] == a1);
    }

    /**
     * Клиентская визуализация связей (линии/подсветка) без мутаций.
     * Обходит все якоря type==1 с известным аспектом, как drawSheet оригинала.
     */
    public static void visualize(HexResearchNoteData data, HasAspect has,
            HashMap<String, HexUtils.Hex[]> lines, ArrayList<String> highlight) {
        lines.clear();
        highlight.clear();
        if (data == null) {
            return;
        }
        for (String k : data.hexEntries.keySet()) {
            HexEntry entry = data.hexEntries.get(k);
            HexUtils.Hex hex = data.hexes.get(k);
            if (entry == null || hex == null || entry.type != 1 || entry.aspect == null) {
                continue;
            }
            if (has != null && !has.has(entry.aspect)) {
                continue;
            }
            ArrayList<String> checked = new ArrayList<>();
            visualizeFrom(data, hex, checked, lines, highlight, has);
        }
    }

    private static void visualizeFrom(HexResearchNoteData data, HexUtils.Hex hex,
            ArrayList<String> checked, HashMap<String, HexUtils.Hex[]> lines,
            ArrayList<String> highlight, HasAspect has) {
        String me = hex.toString();
        if (checked.contains(me)) {
            return;
        }
        checked.add(me);
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
