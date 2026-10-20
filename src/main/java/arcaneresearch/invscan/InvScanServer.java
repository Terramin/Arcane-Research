package arcaneresearch.invscan;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.scan.MsgWalletRequest;
import arcaneresearch.discovered.scan.ScanTargets;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.helper.RegsHelper;

/**
 * Server-side validation and completion of inventory hovers scans.
 * Mirrors the grant logic of MsgStudyComplete for item stacks and the player itself.
 *
 * NEOFORGE_PORT: Forge PacketDistributor.PLAYER -&gt; PacketHandler.sendToPlayer;
 * displayClientMessage -&gt; sendSystemMessage (как в MsgStudyComplete).
 */
public final class InvScanServer {

    public static final String KEY_PREFIX = "T:";
    public static final String SELF_KEY = KEY_PREFIX + "self";

    private InvScanServer() {
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
        String key = KEY_PREFIX + RegsHelper.itemKey(stack.getItem());
        if (ScanWallet.hasScanned(player, key)) {
            return;
        }
        AspectList aspects = AspectHelper.getObjectAspects(stack);
        completeScan(player, key, aspects);
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
        completeScan(player, SELF_KEY, aspects);
    }

    private static void completeScan(ServerPlayer player, String key, AspectList aspects) {
        if (aspects == null || aspects.size() <= 0) {
            player.sendSystemMessage(Component.literal("Unknown object"), true);
            return;
        }
        if (!ScanTargets.validScan(player, aspects)) {
            player.sendSystemMessage(
                    Component.literal("Unknown object: undiscovered components"), true);
            return;
        }
        AspectList granted = new AspectList();
        try {
            for (Aspect aspect : aspects.getAspects()) {
                if (aspect == null) {
                    continue;
                }
                int amount = aspects.getAmount(aspect);
                if (!ScanTargets.known(player, aspect)) {
                    try {
                        ArcaneresearchApi.internalMethods.progressResearch(
                                player, "!" + aspect.getTag());
                    } catch (Exception ignored) {
                    }
                    amount += 2;
                }
                if (amount > 0) {
                    ScanWallet.add(player, aspect, amount);
                    granted.add(aspect, amount);
                }
            }
        } catch (Exception ignored) {
        }
        ScanWallet.markScanned(player, key);
        if (granted.size() > 0) {
            PacketHandler.sendToPlayer(
                    new arcaneresearch.discovered.scan.MsgStudyReward(granted), player);
        }
        MsgWalletRequest.push(player);
    }
}
