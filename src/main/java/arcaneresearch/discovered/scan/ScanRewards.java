package arcaneresearch.discovered.scan;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.invscan.InvScanServer;

public final class ScanRewards {

    private ScanRewards() {
    }

    public static boolean grant(ServerPlayer player, String key, AspectList aspects, boolean silent) {
        if (player == null || key == null || key.isEmpty()) {
            return false;
        }
        if (ScanWallet.hasScanned(player, key)) {
            return false;
        }
        if (aspects == null || aspects.aspects.isEmpty()) {
            if (!silent) {
                player.sendOverlayMessage(Component.literal("Unknown object"));
            }
            return false;
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

        if (granted.aspects.isEmpty()) {
            if (!silent) {
                player.sendOverlayMessage(
                        Component.literal("Unknown object: no aspects gained"));
            }
            return false;
        }

        ScanWallet.markScanned(player, key);
        try {
            PacketHandler.sendToPlayer(new MsgStudyReward(granted), player);
        } catch (Exception ignored) {
        }
        try {
            InvScanServer.pushScanned(player, key);
        } catch (Exception ignored) {
        }
        MsgWalletRequest.push(player);
        return true;
    }
}