package arcaneresearch.common.lib.aura;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
public class AuraResearch {
    private AuraResearch() {
    }
    public static boolean isResearchComplete(Player player, String research) {
        if (player == null || research == null) {
            return false;
        }
        try {
            return ArcaneresearchCapabilities.isResearchComplete(player, research);
        } catch (Exception e) {
            return false;
        }
    }
}
