package arcaneresearch.common.lib.aura;



import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.api.research.ScanningManager;

public class ScanNode implements IScanThing {
    public static void register() {
        try {
            ScanningManager.addScannableThing(new ScanNode());
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        try {
            if (obj instanceof BlockPos pos && player != null && player.level() != null) {
                BlockEntity tile = player.level().getBlockEntity(pos);
                return tile instanceof INode;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return "!arcaneresearch:auranode";
    }
}
