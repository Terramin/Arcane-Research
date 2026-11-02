package arcaneresearch.common.lib.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;
import org.jetbrains.annotations.NotNull;

public class ScanNode implements IScanThing {
    private static final String RESEARCH_KEY = "!arcaneresearch:auranode";

    public static void register() {
        try {
            ScanningManager.addScannableThing(new ScanNode());
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean checkThing(@NotNull Player player, @NotNull Object obj) {
        if (!(obj instanceof BlockPos pos)) {
            return false;
        }
        Level level = player.level();
        BlockEntity tile = level.getBlockEntity(pos);
        return tile instanceof INode;
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return RESEARCH_KEY;
    }
}