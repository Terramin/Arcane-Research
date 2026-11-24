package arcaneresearch.client.renderers.item;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

/** Splits the baked jar model into liquid quads and glass quads. */
public final class JarItemQuads {
    public static final int LIQUID_TINT = 1;

    private JarItemQuads() {
    }

    public static boolean isLiquid(BakedQuad quad) {
        return quad.getTintIndex() == LIQUID_TINT;
    }

    public static void partition(List<BakedQuad> quads, List<BakedQuad> liquid, List<BakedQuad> glass) {
        for (BakedQuad quad : quads) {
            (isLiquid(quad) ? liquid : glass).add(quad);
        }
    }
}
