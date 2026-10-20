package arcaneresearch.api.research;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import arcaneresearch.api.aspects.AspectList;
import java.util.Collection;
import java.util.LinkedHashMap;
public class ResearchCategories {
    public static LinkedHashMap<String, ResearchCategory> researchCategories = new LinkedHashMap<>();
    public static ResearchCategory getResearchCategory(String key) {
        return researchCategories.get(key);
    }
    public static Component getCategoryName(String key) {
        return Component.translatable("tc.research_category." + key);
    }
    public static ResearchEntry getResearch(String key) {
        for (ResearchCategory category : researchCategories.values()) {
            for (ResearchEntry entry : category.research.values()) {
                if (entry.getKey().equals(key)) {
                    return entry;
                }
            }
        }
        return null;
    }
    public static ResearchCategory registerCategory(String key, String researchKey, AspectList formula,
                                                    ResourceLocation icon, ResourceLocation background) {
        if (getResearchCategory(key) == null) {
            ResearchCategory category = new ResearchCategory(key, researchKey, formula, icon, background);
            researchCategories.put(key, category);
            return category;
        }
        return null;
    }
    public static ResearchCategory registerCategory(String key, String researchKey, AspectList formula,
                                                    ResourceLocation icon, ResourceLocation background,
                                                    ResourceLocation background2) {
        if (getResearchCategory(key) == null) {
            ResearchCategory category = new ResearchCategory(key, researchKey, formula, icon, background, background2);
            researchCategories.put(key, category);
            return category;
        }
        return null;
    }
    public static Collection<String> getCategoryKeys() {
        return researchCategories.keySet();
    }
    public static Collection<ResearchCategory> getCategories() {
        return researchCategories.values();
    }
}
