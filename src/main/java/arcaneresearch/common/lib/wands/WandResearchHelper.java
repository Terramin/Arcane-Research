package arcaneresearch.common.lib.wands;

import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WandResearchHelper {

    private WandResearchHelper() {
    }

    public static boolean isResearchComplete(Player player, String research) {
        if (player == null || research == null) return false;
        try {
            return ArcaneresearchCapabilities.isResearchComplete(player, research);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean isResearchComplete(Level level, String playerName, String research) {
        if (level == null || playerName == null || research == null) return false;

        try {
            if (level instanceof ServerLevel server) {
                MinecraftServer srv = server.getServer();

                PlayerList list = srv.getPlayerList();
                Player p = list.getPlayerByName(playerName);
                return isResearchComplete(p, research);
            }

            if (level.isClientSide) return false;

            for (Player p : level.players()) {
                String name = p.getGameProfile().getName();
                if (playerName.equals(name)) {
                    return isResearchComplete(p, research);
                }
            }
        } catch (RuntimeException ignored) {
        }
        return false;
    }
}