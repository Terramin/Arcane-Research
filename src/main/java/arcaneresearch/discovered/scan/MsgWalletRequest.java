package arcaneresearch.discovered.scan;

import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.table.HexNoteTableTile;
import arcaneresearch.discovered.table.HexResearchTableMenu;
import org.jspecify.annotations.NonNull;

public class MsgWalletRequest implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgWalletRequest> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_wallet_request"));

    public static final StreamCodec<ByteBuf, MsgWalletRequest> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull MsgWalletRequest packet) {
        }

        @Override
        public @NonNull MsgWalletRequest decode(@NonNull ByteBuf buf) {
            return new MsgWalletRequest();
        }
    };

    public MsgWalletRequest() {
    }

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgWalletRequest msg, IPayloadContext ctx) {
        if (ctx == null) {
            return;
        }
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                push(player);
            }
        });
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
            PacketHandler.sendToPlayer(new MsgWalletSync(wallet, bonus), player);
        } catch (Exception ignored) {
        }
    }
}