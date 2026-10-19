package arcaneresearch.api.research;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

import java.util.HashMap;
import java.util.Map;

public class ResearchCategory {

    public int minDisplayColumn;

    public int minDisplayRow;

    public int maxDisplayColumn;

    public int maxDisplayRow;

    public Identifier icon;

    public Identifier background;

    public Identifier background2;

    public String researchKey;

    public String key;

    public AspectList formula;

    public Map<String, ResearchEntry> research = new HashMap<>();

    public ResearchCategory(String key, String researchKey, AspectList formula,
                           Identifier icon, Identifier background) {
        this.key = key;
        this.researchKey = researchKey;
        this.icon = icon;
        this.background = background;
        this.background2 = null;
        this.formula = formula;
    }

    public ResearchCategory(String key, String researchKey, AspectList formula,
                           Identifier icon, Identifier background,
                           Identifier background2) {
        this.key = key;
        this.researchKey = researchKey;
        this.icon = icon;
        this.background = background;
        this.background2 = background2;
        this.formula = formula;
    }

    public int applyFormula(AspectList aspects) {
        return applyFormula(aspects, 1.0D);
    }

    public int applyFormula(AspectList aspects, double mod) {
        if (formula == null) return 0;

        double total = 0.0D;
        for (Aspect aspect : formula.getAspects()) {
            total += mod * mod * aspects.getAmount(aspect) * formula.getAmount(aspect) / 10.0D;
        }

        if (total > 0.0D) {
            total = Math.sqrt(total);
        }

        return Mth.ceil(total);
    }
}
