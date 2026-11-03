package arcaneresearch.discovered.scan;

import arcaneresearch.invscan.InvScanServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

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
                player.displayClientMessage(Component.literal("Unknown object"), true);
            }
            return false;
        }
        if (!ScanTargets.validScan(player, aspects)) {
            if (!silent) {
                player.displayClientMessage(
                        Component.literal("Unknown object: undiscovered components"), true);
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

        ScanWallet.markScanned(player, key);
        if (!granted.aspects.isEmpty()) {
            try {
                ScanNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new MsgStudyReward(granted));
            } catch (Exception ignored) {
            }
        }
        try {
            InvScanServer.pushScanned(player, key);
        } catch (Exception ignored) {
        }
        MsgWalletRequest.push(player);
        return true;
    }
}
