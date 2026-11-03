package arcaneresearch.invscan;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.research.ScanningManager;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.scan.ScanRewards;
import arcaneresearch.discovered.scan.ScanTargets;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.helper.RegsHelper;

public final class InvScanServer {

    public static final String ITEM_SCAN_PREFIX = "B:";
    public static final String SELF_KEY = "S:self";

    private static final int MAX_SYNC_KEYS = 8192;

    private InvScanServer() {
    }

    public static String itemScanKey(ItemStack stack) {
        return ITEM_SCAN_PREFIX + RegsHelper.itemKey(stack.getItem());
    }

    private static boolean holdingThaumometerOnCursor(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        return menu != null && menu.getCarried().getItem() instanceof ItemThaumometer;
    }

    public static void handleSlot(ServerPlayer player, int containerId, int index) {
        if (player == null || !holdingThaumometerOnCursor(player)) {
            return;
        }
        AbstractContainerMenu menu = player.containerMenu;
        if (menu.containerId != containerId || index < 0 || index >= menu.slots.size()) {
            return;
        }
        Slot slot = menu.slots.get(index);
        if (!slot.hasItem() || slot instanceof ResultSlot || !slot.mayPickup(player)) {
            return;
        }
        ItemStack stack = slot.getItem();
        String key = itemScanKey(stack);
        if (ScanWallet.hasScanned(player, key)) {
            return;
        }

        ScanningManager.scanTheThing(player, stack, true);

        arcaneresearch.api.aspects.AspectList aspects =
                ArcaneresearchCraftingManager.getObjectTags(stack);
        if (aspects != null) {
            aspects = aspects.copy();
        }
        ScanRewards.grant(player, key, aspects, true);
    }

    public static void handleSelf(ServerPlayer player) {
        if (player == null || !holdingThaumometerOnCursor(player)) {
            return;
        }
        if (player.containerMenu != player.inventoryMenu) {
            return;
        }
        if (ScanWallet.hasScanned(player, SELF_KEY)) {
            return;
        }

        ScanningManager.scanTheThing(player, player, true);

        arcaneresearch.api.aspects.AspectList aspects = ScanTargets.entityAspects(player);
        ScanRewards.grant(player, SELF_KEY, aspects, true);
    }

    public static void pushScanned(ServerPlayer player, String key) {
        if (player == null || key == null || key.isEmpty()) {
            return;
        }
        try {
            PacketHandler.sendToPlayer(
                    new MsgScannedSync(java.util.List.of(key), false), player);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("InvScan: failed to push scanned key", e);
        }
    }

    static void sendScannedSync(ServerPlayer player) {
        if (player == null) {
            return;
        }
        List<String> keys = new ArrayList<>();
        try {
            List<String> stored = ScanWallet.scannedKeys(player);
            for (String key : stored) {
                keys.add(key);
                if (keys.size() >= MAX_SYNC_KEYS) {
                    break;
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("InvScan: failed to collect scanned keys", e);
        }
        try {
            PacketHandler.sendToPlayer(new MsgScannedSync(keys, true), player);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("InvScan: failed to send scanned sync", e);
        }
    }
}
