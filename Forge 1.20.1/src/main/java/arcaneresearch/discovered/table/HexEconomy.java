package arcaneresearch.discovered.table;
import net.minecraft.server.level.ServerPlayer;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.discovered.scan.MsgWalletRequest;
public final class HexEconomy {
    private HexEconomy() {
    }
    public static HexNoteTableTile openTile(ServerPlayer player) {
        try {
            if (player != null && player.containerMenu instanceof HexResearchTableMenu menu
                    && menu.tableInventory() instanceof HexNoteTableTile tile) {
                return tile;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
    public static boolean takeAspect(ServerPlayer player, Aspect aspect) {
        return takeAspect(player, openTile(player), aspect);
    }
    public static boolean takeAspect(ServerPlayer player, HexResearchTableMenu menu, Aspect aspect) {
        HexNoteTableTile tile = null;
        try {
            if (menu != null && menu.tableInventory() instanceof HexNoteTableTile t) {
                tile = t;
            }
        } catch (Exception ignored) {
        }
        return takeAspect(player, tile, aspect);
    }
    private static boolean takeAspect(ServerPlayer player, HexNoteTableTile tile, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        if (ScanWallet.getAmount(player, aspect) > 0) {
            ScanWallet.remove(player, aspect, 1);
            return true;
        }
        try {
            if (tile != null && tile.takeBonus(aspect) > 0) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    public static void push(ServerPlayer player) {
        try {
            MsgWalletRequest.push(player);
        } catch (Exception ignored) {
        }
    }
}
