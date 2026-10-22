package arcaneresearch.discovered.table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

 





 
public final class DiscoveredResearchAspects {

    private static final Map<String, AspectList> MAP = new LinkedHashMap<>();

    static {
        
        register("ALUMENTUM", new AspectList()
                .add(Aspect.ENERGY, 8)
                .add(Aspect.FIRE, 8)
                .add(Aspect.ENTROPY, 4));
        
        register("ARCANESPA", new AspectList()
                .add(Aspect.WATER, 12)
                .add(Aspect.LIFE, 8)
                .add(Aspect.ALCHEMY, 8));
        
        register("BATHSALTS", new AspectList()
                .add(Aspect.WATER, 8)
                .add(Aspect.LIFE, 8)
                .add(Aspect.ORDER, 8));
        
        
        register("BOOTSTRAVELLER", new AspectList()
                .add(Aspect.MOTION, 24)
                .add(Aspect.FLIGHT, 24)
                .add(Aspect.PROTECT, 16)
                .add(Aspect.EARTH, 16)
                .add(Aspect.WATER, 16));
        
        register("CENTRIFUGE", new AspectList()
                .add(Aspect.MECHANISM, 16)
                .add(Aspect.MOTION, 12)
                .add(Aspect.EXCHANGE, 8)
                .add(Aspect.MAGIC, 8));
        
        register("FOCUSADVANCED", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.MIND, 8)
                .add(Aspect.MECHANISM, 4));
        
        register("FOCUSBOLT", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.AVERSION, 8)
                .add(Aspect.ENERGY, 4));
        
        register("FOCUSBREAK", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.ENTROPY, 8)
                .add(Aspect.EARTH, 4)
                .add(Aspect.TOOL, 4));
        
        register("FOCUSCLOUD", new AspectList()
                .add(Aspect.AIR, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.WATER, 4));
        
        register("FOCUSCURSE", new AspectList()
                .add(Aspect.AVERSION, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.SOUL, 4));
        
        register("FOCUSELEMENTAL", new AspectList()
                .add(Aspect.MAGIC, 8)
                .add(Aspect.FIRE, 6)
                .add(Aspect.WATER, 6)
                .add(Aspect.EARTH, 6)
                .add(Aspect.AIR, 6));
        
        register("FOCUSEXCHANGE", new AspectList()
                .add(Aspect.EXCHANGE, 16)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.TOOL, 8));
        
        register("FOCUSFLUX", new AspectList()
                .add(Aspect.FLUX, 16)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.ENTROPY, 8));
        
        register("FOCUSGREATER", new AspectList()
                .add(Aspect.MAGIC, 16)
                .add(Aspect.MIND, 8)
                .add(Aspect.ENERGY, 8));
        
        register("FOCUSHEAL", new AspectList()
                .add(Aspect.LIFE, 16)
                .add(Aspect.SOUL, 8)
                .add(Aspect.MAGIC, 8));
        
        register("FOCUSMINE", new AspectList()
                .add(Aspect.EARTH, 12)
                .add(Aspect.TOOL, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.ENTROPY, 4));
        
        register("FOCUSPLAN", new AspectList()
                .add(Aspect.MOTION, 12)
                .add(Aspect.AIR, 8)
                .add(Aspect.MAGIC, 8));
        
        register("FOCUSPROJECTILE", new AspectList()
                .add(Aspect.MOTION, 16)
                .add(Aspect.AVERSION, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.AIR, 4));
        
        register("FOCUSRIFT", new AspectList()
                .add(Aspect.VOID, 16)
                .add(Aspect.ELDRITCH, 8)
                .add(Aspect.MAGIC, 8));
        
        register("FOCUSSCATTER", new AspectList()
                .add(Aspect.MOTION, 12)
                .add(Aspect.AIR, 8)
                .add(Aspect.MAGIC, 8));
        
        register("FOCUSSPELLBAT", new AspectList()
                .add(Aspect.BEAST, 8)
                .add(Aspect.FLIGHT, 8)
                .add(Aspect.MAGIC, 8));
        
        register("FOCUSSPLIT", new AspectList()
                .add(Aspect.MOTION, 8)
                .add(Aspect.EXCHANGE, 8)
                .add(Aspect.MAGIC, 8));
        
        register("INFUSION", new AspectList()
                .add(Aspect.MAGIC, 16)
                .add(Aspect.CRAFT, 12)
                .add(Aspect.TOOL, 8)
                .add(Aspect.ORDER, 8));
        
        register("INFUSIONBOOST", new AspectList()
                .add(Aspect.ENERGY, 12)
                .add(Aspect.MAGIC, 12)
                .add(Aspect.ENTROPY, 8));
        
        register("INFUSIONSTABLE", new AspectList()
                .add(Aspect.ORDER, 12)
                .add(Aspect.PROTECT, 8)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.MECHANISM, 4));
        
        register("METALPURIFICATION", new AspectList()
                .add(Aspect.METAL, 16)
                .add(Aspect.FIRE, 8)
                .add(Aspect.ORDER, 8));
        
        register("POTIONSPRAYER", new AspectList()
                .add(Aspect.WATER, 12)
                .add(Aspect.ALCHEMY, 8)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.AIR, 4));
        
        register("RECHARGEPEDESTAL", new AspectList()
                .add(Aspect.ENERGY, 16)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.CRYSTAL, 8));
        
        register("SANESOAP", new AspectList()
                .add(Aspect.WATER, 8)
                .add(Aspect.ORDER, 8)
                .add(Aspect.LIFE, 8)
                .add(Aspect.SENSES, 4));
        
        register("THAUMATORIUM", new AspectList()
                .add(Aspect.FIRE, 16)
                .add(Aspect.METAL, 12)
                .add(Aspect.MECHANISM, 8)
                .add(Aspect.EARTH, 8));
        
        register("VISAMULET", new AspectList()
                .add(Aspect.ENERGY, 12)
                .add(Aspect.MAGIC, 8)
                .add(Aspect.DESIRE, 8));
        
        register("VISBATTERY", new AspectList()
                .add(Aspect.ENERGY, 16)
                .add(Aspect.CRYSTAL, 8)
                .add(Aspect.MECHANISM, 8));
        
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

      
    public static AspectList get(String key) {
        AspectList list = MAP.get(key);
        return list == null ? null : list.copy();
    }

     






 
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
