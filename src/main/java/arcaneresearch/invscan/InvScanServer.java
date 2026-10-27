package arcaneresearch.invscan;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.discovered.scan.MsgStudyReward;
import arcaneresearch.discovered.scan.MsgWalletRequest;
import arcaneresearch.discovered.scan.ScanNetwork;
import arcaneresearch.discovered.scan.ScanTargets;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.helper.RegsHelper;

public final class InvScanServer {

    public static final String KEY_PREFIX = "T:";
    public static final String SELF_KEY = KEY_PREFIX + "self";

    private InvScanServer() {
    }

    private static boolean holdingThaumometerOnCursor(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        return menu.getCarried().getItem() instanceof ItemThaumometer;
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
            Arcaneresearch.LOGGER.debug("InvScan: scan for {} produced no aspects", key);
            return;
        }
        if (!ScanTargets.validScan(player, aspects)) {
            Arcaneresearch.LOGGER.debug("InvScan: validScan rejected {}", key);
            return;
        }

        AspectList granted = new AspectList();
        for (Aspect aspect : aspects.getAspects()) {
            if (aspect == null) {
                continue;
            }
            int amount = aspects.getAmount(aspect);

            if (!ScanTargets.known(player, aspect)) {
                String tag = aspect.getTag();
                if (tag != null) {
                    try {
                        ArcaneresearchApi.internalMethods.progressResearch(player, "!" + tag);
                    } catch (Exception e) {
                        Arcaneresearch.LOGGER.warn(
                                "InvScan: progressResearch failed for aspect '{}'", tag, e);
                    }
                }
                amount += 2;
            }

            if (amount > 0) {
                ScanWallet.add(player, aspect, amount);
                granted.add(aspect, amount);
            }
        }

        ScanWallet.markScanned(player, key);
        if (granted.size() > 0) {
            ScanNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new MsgStudyReward(granted));
        }
        MsgWalletRequest.push(player);
    }
}