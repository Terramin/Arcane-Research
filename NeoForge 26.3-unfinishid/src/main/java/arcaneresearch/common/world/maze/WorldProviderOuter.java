package arcaneresearch.common.world.maze;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import arcaneresearch.Arcaneresearch;

/**
 * Порт измерения Внешних земель (Outer Lands) из TC4/TC5.
 * Оригинал TC4: arcaneresearch.common.lib.world.dim.WorldProviderOuter
 * (название, сообщения входа/выхода, туман 10518688, облака y=50,
 * без погоды/молний/снега, карта крутится, время застывшее).
 * Оригинал TC5: arcaneresearch.common.lib.world.dim.WorldProviderOuter
 * (те же константы + EFFECTS="arcaneresearch:outer").
 * Здесь — константы и ключ измерения для 1.20.1 (сам мир задаётся
 * datapack-JSON arcaneresearch:outer_lands + генератором пустоты,
 * визуал — OuterLandsEffects).
 * Всё лежит в arcaneresearch.arcaneresearch.common.world.maze по просьбе портирования.
 */
public final class WorldProviderOuter {
    public static final Identifier OUTER_LANDS_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "outer_lands");
    public static final ResourceKey<Level> OUTER_LANDS_KEY =
            ResourceKey.create(Registries.DIMENSION, OUTER_LANDS_ID);
    /** Id эффектов неба/тумана, совпадает с dimension_type "effects". */
    public static final Identifier EFFECTS =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "outer");

    public static final String DIMENSION_NAME = "The Outer Lands";
    public static final String WELCOME_MESSAGE = "Entering The Outer Lands";
    public static final String DEPART_MESSAGE = "Leaving The Outer Lands";

    /** Цвет тумана/неба из TC4 (func_76562_b, i=10518688). */
    public static final int FOG_COLOR = 10518688;
    /** Высота облаков/комнат лабиринта. */
    public static final int CLOUD_LEVEL = 50;
    public static final int MAZE_LEVEL = 50;
    public static final float CLOUD_BRIGHTNESS = 1.0F;
    /** Застывшее время (полночь), как fixed_time=18000 в dimension_type. */
    public static final long FIXED_TIME = 18000L;

    private WorldProviderOuter() {
    }

    public static String getDimensionName() {
        return DIMENSION_NAME;
    }

    public static String getWelcomeMessage() {
        return WELCOME_MESSAGE;
    }

    public static String getDepartMessage() {
        return DEPART_MESSAGE;
    }

    /** TC4 shouldMapSpin — карта во Внешних землях крутится. */
    public static boolean shouldMapSpin() {
        return true;
    }

    /** TC4: нет заморозки/ снега/молний/дождя. */
    public static boolean canBlockFreeze() {
        return false;
    }

    public static boolean canSnowAt() {
        return false;
    }

    public static boolean canDoLightning() {
        return false;
    }

    public static boolean canDoRainSnowIce() {
        return false;
    }

    /** TC4 getSunBrightness — всегда 0 (темно, свет только от блоков). */
    public static float getSunBrightness() {
        return 0.0F;
    }

    /** TC4 getCloudColour — яркость облаков 1.0. */
    public static float getCloudBrightness() {
        return CLOUD_BRIGHTNESS;
    }

    /** Распаковка FOG_COLOR в RGB 0..1 (формула TC4 с f2=1). */
    public static float[] getFogColor() {
        float r = ((FOG_COLOR >> 16) & 0xFF) / 255.0F * 0.15F;
        float g = ((FOG_COLOR >> 8) & 0xFF) / 255.0F * 0.15F;
        float b = (FOG_COLOR & 0xFF) / 255.0F * 0.15F;
        return new float[]{r, g, b};
    }

    public static boolean isOuterLands(ResourceKey<Level> dimension) {
        return OUTER_LANDS_KEY.equals(dimension)
                || MazeFeature.DIMENSION.equals(dimension);
    }
}
