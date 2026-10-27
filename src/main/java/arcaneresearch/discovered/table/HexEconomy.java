package arcaneresearch.discovered.table;

import net.minecraft.server.level.ServerPlayer;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.discovered.scan.MsgWalletRequest;

public final class HexEconomy {
    private HexEconomy() {
    }

    public static HexNoteTableTile openTile(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        try {
            if (player.containerMenu instanceof HexResearchTableMenu menu
                    && menu.tableInventory() instanceof HexNoteTableTile tile) {
                return tile;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static boolean takeAspect(ServerPlayer player, Aspect aspect) {
        if (player == null) {
            return false;
        }
        return takeAspect(player, openTile(player), aspect);
    }

    public static boolean takeAspect(ServerPlayer player, HexResearchTableMenu menu, Aspect aspect) {
        if (player == null) {
            return false;
        }
        HexNoteTableTile tile = null;
        if (menu != null) {
            try {
                if (menu.tableInventory() instanceof HexNoteTableTile t) {
                    tile = t;
                }
            } catch (Exception ignored) {
            }
        }
        return takeAspect(player, tile, aspect);
    }

    private static boolean takeAspect(ServerPlayer player, HexNoteTableTile tile, Aspect aspect) {
        if (player == null || aspect == null) {
            return false;
        }
        boolean hasWallet = false;
        try {
            hasWallet = ScanWallet.getAmount(player, aspect) > 0;
        } catch (Exception ignored) {
        }
        if (hasWallet) {
            try {
                ScanWallet.remove(player, aspect, 1);
            } catch (Exception ignored) {
            }
            return true;
        }
        if (tile == null) {
            return false;
        }
        try {
            return tile.takeBonus(aspect) > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void push(ServerPlayer player) {
        try {
            MsgWalletRequest.push(player);
        } catch (Exception ignored) {
        }
    }
}