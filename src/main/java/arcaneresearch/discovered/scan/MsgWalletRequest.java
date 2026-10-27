package arcaneresearch.discovered.scan;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.table.HexNoteTableTile;
import arcaneresearch.discovered.table.HexResearchTableMenu;

public class MsgWalletRequest {
    public MsgWalletRequest() {
    }

    public MsgWalletRequest(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public static void handle(MsgWalletRequest msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx == null ? null : ctx.get();
        if (context == null) {
            return;
        }
        context.enqueueWork(() -> {
            if (!context.getDirection().getReceptionSide().isServer()) {
                return;
            }
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            push(player);
        });
        context.setPacketHandled(true);
    }

    public static void push(ServerPlayer player) {
        if (player == null) {
            return;
        }
        Map<String, Integer> wallet = ScanWallet.snapshot(player);
        Map<String, Integer> bonus = new LinkedHashMap<>();
        try {
            if (player.containerMenu instanceof HexResearchTableMenu menu) {
                Container inv = menu.tableInventory();
                if (inv instanceof HexNoteTableTile tile) {
                    AspectList list = tile.getBonus();
                    if (list != null) {
                        Aspect[] aspects = list.getAspects();
                        if (aspects != null) {
                            for (Aspect aspect : aspects) {
                                if (aspect == null) {
                                    continue;
                                }
                                String key = aspect.getTag();
                                if (key == null) {
                                    continue;
                                }
                                int amount = list.getAmount(aspect);
                                if (amount > 0) {
                                    bonus.put(key, amount);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        try {
            ScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new MsgWalletSync(wallet, bonus));
        } catch (Exception ignored) {
        }
    }
}