package arcaneresearch.common.world.maze;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (client-dimension-visual stub): vanilla
// net.minecraft.client.renderer.DimensionSpecialEffects (incl. SkyType) and NeoForge's
// RegisterDimensionSpecialEffectsEvent were BOTH removed in 26.x, so there is currently
// no dimension-visual hook to register against. The TC4 fog/sky values are preserved
// below (WorldProviderOuter.getFogColor) and the dimension_type "effects" ids are kept,
// so the client/dimension phase can rewire this 1:1 once a hook exists.
// Original Forge source: Forge 1.20.1 .../world/maze/OuterLandsEffects.java (53 lines).
public class OuterLandsEffects {
    private OuterLandsEffects() {
    }

    /// Fog color multiplier from TC4 (func_76562_b at f2=1: FOG_COLOR * 0.15).
    public static float[] getFogColor() {
        return WorldProviderOuter.getFogColor();
    }

    /// dimension_type "effects" id wiring (kept for the rewire phase).
    public static Identifier primaryEffectsId() {
        return WorldProviderOuter.EFFECTS;
    }

    public static Identifier legacyEffectsId() {
        return Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "outer_lands");
    }

    // TODO_PORT (client/dimension phase): re-register the sky/fog behavior
    // (dark fog-colored sky, fog 10518688, no weather, bright clouds) against
    // whatever 26.x dimension-visual hook exists, then delete this stub.
}