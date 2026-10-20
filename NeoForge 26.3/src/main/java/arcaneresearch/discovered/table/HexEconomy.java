package arcaneresearch.discovered.table;

import net.minecraft.server.level.ServerPlayer;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.discovered.scan.MsgWalletRequest;

/**
 * Хранилище hex-стола в одном месте: списание (кошелёк игрока, затем
 * бонус открытого тайла) и пуш синка клиенту. Паритет ветвей
 * {@code placeAspect} / {@code PacketAspectCombinationToServer} оригинала
 * (без перков и ab-флагов клиента).
 *
 * NEOFORGE_PORT: 1:1 из Forge 1.20.1 (транспорт — через MsgWalletRequest.push,
 * см. ScanNetwork).
 */
public final class HexEconomy {

    private HexEconomy() {
    }

    /** Тайл открытого меню (null у прототипа без блока — бонуса нет). */
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

    /**
     * Списать очко аспекта: кошелёк, затем бонус тайла.
     * Частичная оплата возможна (как в оригинале, без рефанда).
     */
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

    /** Отправить игроку снимок хранилища (после каждой мутации). */
    public static void push(ServerPlayer player) {
        try {
            MsgWalletRequest.push(player);
        } catch (Exception ignored) {
        }
    }
}
