package arcaneresearch.common.lib.wands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;



public class WandResearchHelper {
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

    public static boolean isResearchComplete(Level level, String playerName, String research) {
        if (level == null || playerName == null) {
            return false;
        }
        try {
            if (level instanceof ServerLevel server) {
                Player p = server.getServer().getPlayerList().getPlayerByName(playerName);
                if (p != null) {
                    return isResearchComplete(p, research);
                }
                return false;
            }
            if (level.isClientSide()) {
                return false;
            }
            for (Player p : level.players()) {
                
                if (playerName.equals(p.getName().getString())) {
                    return isResearchComplete(p, research);
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
