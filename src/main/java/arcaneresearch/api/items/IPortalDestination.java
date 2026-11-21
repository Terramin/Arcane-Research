package arcaneresearch.api.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import arcaneresearch.api.portals.PortalDestination;

public interface IPortalDestination {

    boolean hasSavedPosition(ItemStack stack);

    @Nullable
    PortalDestination getSavedDestination(ItemStack stack);

    boolean bindPosition(ItemStack stack, Level level, BlockPos pos);
}
