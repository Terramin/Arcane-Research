package arcaneresearch.common.world.maze;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import arcaneresearch.Arcaneresearch;

 










 
public final class WorldProviderOuter {
    public static final Identifier OUTER_LANDS_ID =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "outer_lands");
    public static final ResourceKey<Level> OUTER_LANDS_KEY =
            ResourceKey.create(Registries.DIMENSION, OUTER_LANDS_ID);
      
    public static final Identifier EFFECTS =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "outer");

    public static final String DIMENSION_NAME = "The Outer Lands";
    public static final String WELCOME_MESSAGE = "Entering The Outer Lands";
    public static final String DEPART_MESSAGE = "Leaving The Outer Lands";

      
    public static final int FOG_COLOR = 10518688;
      
    public static final int CLOUD_LEVEL = 50;
    public static final int MAZE_LEVEL = 50;
    public static final float CLOUD_BRIGHTNESS = 1.0F;
      
    public static final long FIXED_TIME = 18000L;

    private static final float[] FOG_RGB = computeFogColor();

    private WorldProviderOuter() {
    }

    private static float[] computeFogColor() {
        float r = ((FOG_COLOR >> 16) & 0xFF) / 255.0F * 0.15F;
        float g = ((FOG_COLOR >> 8) & 0xFF) / 255.0F * 0.15F;
        float b = (FOG_COLOR & 0xFF) / 255.0F * 0.15F;
        return new float[]{r, g, b};
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

      
    public static boolean shouldMapSpin() {
        return true;
    }

      
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

      
    public static float getSunBrightness() {
        return 0.0F;
    }

      
    public static float getCloudBrightness() {
        return CLOUD_BRIGHTNESS;
    }

      
    public static float[] getFogColor() {
        return new float[]{FOG_RGB[0], FOG_RGB[1], FOG_RGB[2]};
    }

    public static boolean isOuterLands(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return false;
        }
        if (OUTER_LANDS_KEY.equals(dimension)) {
            return true;
        }
        ResourceKey<Level> mazeDim = MazeFeature.DIMENSION;
        return mazeDim.equals(dimension);
    }
}
