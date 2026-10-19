package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

/**
 * Точные требуемые аспекты мини-игры записок для всех открытий проекта.
 * Ручные тематические наборы в духе reforged {@code ConfigResearch}
 * (TC6-оригинал точных наборов не имеет — там формула категории,
 * которая здесь осталась запасным вариантом в
 * {@link DiscoveredTableResearch#requiredAspects}).
 */
public final class DiscoveredResearchAspects {

    private static final Map<String, AspectList> MAP = new LinkedHashMap<>();

    static {
        // reforged ConfigResearch: ALUMENTUM = POWER 8, FIRE 8, DESTRUCTION 4
        register("ALUMENTUM", new AspectList()
                .add(Aspect.ENERGY, 8)
                .add(Aspect.FIRE, 8)
                .add(Aspect.ENTROPY, 4));
        // Чародейское спа: вода, жизнь, алхимия.
        register("ARCANESPA", new AspectList()
                .add(Aspect.WATER, 12)
                .add(Aspect.LIFE, 8)
                .add(Aspect.ALCHEMY, 8));
        // Соли для ванн: вода, жизнь, порядок.
        register("BATHSALTS", new AspectList()
                .add(Aspect.WATER, 8)
                .add(Aspect.LIFE, 8)
                .add(Aspect.ORDER, 8));
        // reforged ConfigResearch: BOOTSTRAVELLER =
        // MOTION 24, FLIGHT 24, ARMOR 16, EARTH 16, WATER 16
        register("BOOTSTRAVELLER", new AspectList()
                .add(Aspect.MOTION, 24)
                .add(Aspect.FLIGHT, 24)
                .add(Aspect.PROTECT, 16)
                .add(Aspect.EARTH, 16)
                .add(Aspect.WATER, 16));
        // Центрифуга: механизм, движение, обмен и магия эссенции.
        register("CENTRIFUGE", new AspectList()
                .add(Aspect.MECHANISM, 16)
                .add(Aspect.MOTION, 12)
                .add(Aspect.EXCHANGE, 8)
                .add(Aspect.MAGIC, 8));
        // Улучшенный фокус: магия, разум, механизм.
        register("FOCUSADVANCED", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.MIND, 8)
                .add(Aspect.MECHANISM, 4));
        // Молния: магия, вред, энергия.
        register("FOCUSBOLT", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.AVERSION, 8)
                .add(Aspect.ENERGY, 4));
        // Разрушение: магия, энтропия, земля и инструмент.
        register("FOCUSBREAK", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.ENTROPY, 8)
                .add(Aspect.EARTH, 4)
                .add(Aspect.TOOL, 4));
        // Облако: воздух, магия, вода.
        register("FOCUSCLOUD", new AspectList()
                .add(Aspect.AIR, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.WATER, 4));
        // Проклятие: вред, магия, душа.
        register("FOCUSCURSE", new AspectList()
                .add(Aspect.AVERSION, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.SOUL, 4));
        // Стихийный: магия и четыре стихии.
        register("FOCUSELEMENTAL", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.FIRE, 6)
                .add(Aspect.WATER, 6)
                .add(Aspect.EARTH, 6)
                .add(Aspect.AIR, 6));
        // Обмен: обмен, магия, инструмент.
        register("FOCUSEXCHANGE", new AspectList()
                .add(Aspect.EXCHANGE, 16)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.TOOL, 8));
        // Флюс: порча, магия, энтропия.
        register("FOCUSFLUX", new AspectList()
                .add(Aspect.FLUX, 16)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.ENTROPY, 8));
        // Великий: магия, разум, энергия.
        register("FOCUSGREATER", new AspectList()
                .add(Aspect.MAGIC, 16)
                .add(Aspect.MIND, 8)
                .add(Aspect.ENERGY, 8));
        // Лечение: жизнь, душа, магия.
        register("FOCUSHEAL", new AspectList()
                .add(Aspect.LIFE, 16)
                .add(Aspect.SOUL, 8)
                .add(Aspect.MAGIC, 8));
        // Добыча: земля, инструмент, магия и разрушение породы.
        register("FOCUSMINE", new AspectList()
                .add(Aspect.EARTH, 12)
                .add(Aspect.TOOL, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.ENTROPY, 4));
        // Плоская траектория: движение, воздух, магия.
        register("FOCUSPLAN", new AspectList()
                .add(Aspect.MOTION, 12)
                .add(Aspect.AIR, 8)
                .add(Aspect.MAGIC, 8));
        // Снаряд: движение, вред, магия и воздух полёта.
        register("FOCUSPROJECTILE", new AspectList()
                .add(Aspect.MOTION, 16)
                .add(Aspect.AVERSION, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.AIR, 4));
        // Разлом: пустота, чуждость, магия.
        register("FOCUSRIFT", new AspectList()
                .add(Aspect.VOID, 16)
                .add(Aspect.ELDRITCH, 8)
                .add(Aspect.MAGIC, 8));
        // Рассеивание: движение, воздух, магия.
        register("FOCUSSCATTER", new AspectList()
                .add(Aspect.MOTION, 12)
                .add(Aspect.AIR, 8)
                .add(Aspect.MAGIC, 8));
        // Заклинательная мышь: зверь, полёт, магия.
        register("FOCUSSPELLBAT", new AspectList()
                .add(Aspect.BEAST, 8)
                .add(Aspect.FLIGHT, 8)
                .add(Aspect.MAGIC, 8));
        // Разделение: движение, обмен, магия.
        register("FOCUSSPLIT", new AspectList()
                .add(Aspect.MOTION, 8)
                .add(Aspect.EXCHANGE, 8)
                .add(Aspect.MAGIC, 8));
        // Инфузия: магия, ремесло, инструмент и порядок симметрии.
        register("INFUSION", new AspectList()
                .add(Aspect.MAGIC, 16)
                .add(Aspect.CRAFT, 12)
                .add(Aspect.TOOL, 8)
                .add(Aspect.ORDER, 8));
        // Усиление инфузии: энергия, магия, энтропия.
        register("INFUSIONBOOST", new AspectList()
                .add(Aspect.ENERGY, 12)
                .add(Aspect.MAGIC, 12)
                .add(Aspect.ENTROPY, 8));
        // Стабильность: порядок, защита, магия и механизм стабилизатора.
        register("INFUSIONSTABLE", new AspectList()
                .add(Aspect.ORDER, 12)
                .add(Aspect.PROTECT, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.MECHANISM, 4));
        // Очистка металла: металл, огонь, порядок.
        register("METALPURIFICATION", new AspectList()
                .add(Aspect.METAL, 16)
                .add(Aspect.FIRE, 8)
                .add(Aspect.ORDER, 8));
        // Распылитель: вода, алхимия, механизм и воздушная взвесь.
        register("POTIONSPRAYER", new AspectList()
                .add(Aspect.WATER, 12)
                .add(Aspect.ALCHEMY, 8)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.AIR, 4));
        // Перезарядка: энергия, магия, механизм и кристалл-накопитель.
        register("RECHARGEPEDESTAL", new AspectList()
                .add(Aspect.ENERGY, 16)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.CRYSTAL, 8));
        // Мыло: вода, порядок, жизнь и аромат.
        register("SANESOAP", new AspectList()
                .add(Aspect.WATER, 8)
                .add(Aspect.ORDER, 8)
                .add(Aspect.LIFE, 8)
                .add(Aspect.SENSES, 4));
        // Тауматорий: огонь, металл, механизм и каменный корпус.
        register("THAUMATORIUM", new AspectList()
                .add(Aspect.FIRE, 16)
                .add(Aspect.METAL, 12)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.EARTH, 8));
        // Амулет виса: энергия, магия, желание.
        register("VISAMULET", new AspectList()
                .add(Aspect.ENERGY, 12)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.DESIRE, 8));
        // Вис-батарея: энергия, кристалл, механизм.
        register("VISBATTERY", new AspectList()
                .add(Aspect.ENERGY, 16)
                .add(Aspect.CRYSTAL, 8)
                .add(Aspect.MECHANISM, 8));
        // Зарядник: энергия, механизм, магия и инструменты верстака.
        register("WORKBENCHCHARGER", new AspectList()
                .add(Aspect.ENERGY, 12)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.TOOL, 4));
    }

    private DiscoveredResearchAspects() {
    }

    public static void register(String key, AspectList required) {
        if (key != null && required != null) {
            MAP.put(key, required);
        }
    }

    /** Копия списка или null, если точного набора нет. */
    public static AspectList get(String key) {
        AspectList list = MAP.get(key);
        return list == null ? null : list.copy();
    }

    /**
     * Ключи в порядке регистрации.
     *
     * @deprecated вкладка строится по {@code required_discovery} стадий
     *             ({@link DiscoveredTableResearch#discoveryKeys()} + статический
     *             список в {@code DiscoveredNoteSetup}); метод оставлен для
     *             совместимости.
     */
    @Deprecated
    public static List<String> keys() {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, AspectList> e : MAP.entrySet()) {
            if (e.getValue() != null && e.getValue().size() > 0) {
                out.add(e.getKey());
            }
        }
        return out;
    }
}
