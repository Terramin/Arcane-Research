package arcaneresearch.common.world.maze;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

public class OuterLandsEffects {
    private OuterLandsEffects() {
    }

    public static float[] getFogColor() {
        return WorldProviderOuter.getFogColor();
    }

    public static Identifier primaryEffectsId() {
        return WorldProviderOuter.EFFECTS;
    }

    public static Identifier legacyEffectsId() {
        return Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "outer_lands");
    }
}