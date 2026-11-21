package arcaneresearch.api.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record PortalDestination(BlockPos pos, ResourceKey<Level> dimension, float width, float height) {

    public static final float DEFAULT_WIDTH = 2.4F;

    public static final float DEFAULT_HEIGHT = 4.2F;

    public PortalDestination {
        if (dimension == null) {
            dimension = Level.OVERWORLD;
        }
    }

    public boolean isValid() {
        return pos != null;
    }
}
