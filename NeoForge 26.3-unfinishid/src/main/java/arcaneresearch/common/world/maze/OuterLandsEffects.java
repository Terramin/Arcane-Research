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

    // NEOFORGE_PORT: the sky/fog behavior is data-driven in 26.x: environment attributes
    // (minecraft:visual/sky_color, fog_color, ...) in data/arcaneresearch/dimension_type/
    // outer_lands.json and worldgen/biome/outer_lands.json replace DimensionSpecialEffects.
}