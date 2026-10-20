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

/**
 * Реестр hex-стадий (2-я стадия двухстадийной схемы: 1-я — theorycraft-стол,
 * 2-я — hex-минигира). Ключ — research key, значение — 0-based индекс стадии.
 *
 * Без правок проекта метка стадии задаётся только явной регистрацией здесь
 * (для проектных исследований — отдельный вопрос: JSON-ключ стадии).
 * Цели берутся из {@code DiscoveredTableResearch.requiredAspects(key)}.
 *
 * NEOFORGE_PORT: 1:1 из Forge 1.20.1 (getKnowledge уже в null-форме,
 * Optional-цепочек в файле нет).
 */
public class HexStages {

    /** Первый hex-этап, текущий у игрока (порядок JSON). */
    public static String findOpenable(Player player) {
        List<String> keys = openableKeys(player);
        return keys.isEmpty() ? null : keys.get(0);
    }

    /**
     * Все исследования с доступным hex (порядок JSON). Выбором UI — первый.
     */
    public static List<String> openableKeys(Player player) {
        ArrayList<String> out = new ArrayList<>();
        if (player == null) {
            return out;
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) {
            return out;
        }
        // JSON-метки: hex-стадия открыта, пока исследование не завершено.
        // Привязки к индексу текущей стадии нет: завершение — только сдачей
        // (tryCompleteFromNote, по одной стадии за сдачу), циклы повторяются,
        // иначе поздние сдачи оставались бы без доступного hex.
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

    /** Целевые аспекты hex-записки исследования (без фолбэков). */
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

    /** Цвет записки — цвет первого целевого аспекта. */
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
