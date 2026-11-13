package arcaneresearch.api.portals;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IPortal {

    float OPEN_SLIDE_PX = 27.5F;

    Level level();

    BlockPos getTeleportTarget();

    ResourceKey<Level> getTeleportDimension();

    void setTeleportTarget(BlockPos pos);

    void setTeleportDimension(ResourceKey<Level> dimension);

    void setSize(float width, float height);

    boolean isClosing();

    void startClosing();

    int getDisplayedLifetime();

    float getOpenFraction(float partialTicks);

    float getFadeAlpha(float partialTicks);
}
