package arcaneresearch.invscan;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.research.ScanningManager;
import arcaneresearch.common.items.tools.ItemThaumometer;
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

        arcaneresearch.api.aspects.AspectList aspects =
                arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager
                        .getObjectTags(stack);
        if (aspects != null) {
            aspects = aspects.copy();
        }

        if (ScanRewards.grant(player, key, aspects, true)) {
            ScanningManager.scanTheThing(player, stack, true);
        }
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

        AspectList aspects = ScanTargets.entityAspects(player);

        if (ScanRewards.grant(player, SELF_KEY, aspects, true)) {
            ScanningManager.scanTheThing(player, player, true);
        }
    }

    public static void pushScanned(ServerPlayer player, String key) {
        if (player == null || key == null || key.isEmpty()) {
            return;
        }
        try {
            InvScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgScannedSync(java.util.List.of(key), false));
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
            ListTag list = ScanWallet.scannedKeys(player);
            for (Tag tag : list) {
                if (tag instanceof StringTag s) {
                    keys.add(s.getAsString());
                    if (keys.size() >= MAX_SYNC_KEYS) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("InvScan: failed to collect scanned keys", e);
        }
        try {
            InvScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgScannedSync(keys, true));
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("InvScan: failed to send scanned sync", e);
        }
    }
}