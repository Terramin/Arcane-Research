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
        ctx.get().enqueueWork(() -> {
            if (!ctx.get().getDirection().getReceptionSide().isServer()) {
                return;
            }
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            push(player);
        });
        ctx.get().setPacketHandled(true);
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
                        for (Aspect aspect : list.getAspects()) {
                            if (aspect != null) {
                                int amount = list.getAmount(aspect);
                                if (amount > 0) {
                                    bonus.put(aspect.getTag(), amount);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        ScanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new MsgWalletSync(wallet, bonus));
    }
}
