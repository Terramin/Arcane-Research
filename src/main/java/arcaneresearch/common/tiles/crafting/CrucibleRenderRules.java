package arcaneresearch.common.tiles.crafting;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

/** Pure fluid colouring rules for the crucible renderer. */
public final class CrucibleRenderRules {

    /**
     * RGBA of the liquid surface.
     *
     * @param r red component
     * @param g green component
     * @param b blue component
     * @param alpha opacity
     */
    public record FluidColor(float r, float g, float b, float alpha) {
    }

    /** Fallback water colour used when the biome cannot be queried. */
    public static final int DEFAULT_WATER_COLOR = 0x3F76E4;

    private CrucibleRenderRules() {
    }

    /**
     * Builds the liquid colour from the dissolved aspects. With no aspects the
     * biome's water colour is used. Opacity fades as more vis is dissolved.
     *
     * @param aspects aspects dissolved in the crucible
     * @param waterColor RGB colour of the water in the current biome
     */
    public static FluidColor fluidColor(AspectList aspects, int waterColor) {
        int total = aspects == null ? 0 : aspects.visSize();

        double ar = 0.0;
        double ag = 0.0;
        double ab = 0.0;
        if (total > 0) {
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null) {
                    continue;
                }
                int color = aspect.getColor() & 0xFFFFFF;
                int amount = aspects.getAmount(aspect);
                ar += ((color >> 16) & 0xFF) * (double) amount;
                ag += ((color >> 8) & 0xFF) * (double) amount;
                ab += (color & 0xFF) * (double) amount;
            }
        }

        float r;
        float g;
        float b;
        if (total > 0) {
            r = (float) (ar / total / 255.0);
            g = (float) (ag / total / 255.0);
            b = (float) (ab / total / 255.0);
        } else {
            r = ((waterColor >> 16) & 0xFF) / 255.0f;
            g = ((waterColor >> 8) & 0xFF) / 255.0f;
            b = (waterColor & 0xFF) / 255.0f;
        }

        float recolor = (float) total / CrucibleRules.ASPECT_LIMIT;
        if (recolor > 0.0f) {
            recolor = Math.min(1.0f, 0.5f + recolor / 2.0f);
        }
        float alpha = 1.0f - recolor / 2.0f;
        return new FluidColor(r, g, b, alpha);
    }
}
