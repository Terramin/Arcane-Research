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

/**
 * Клиент -&gt; сервер: «дай снимок хранилища» (при открытии GUI).
 * Ответ — {@link MsgWalletSync}: кошелёк игрока + бонус тайла открытого
 * меню (у прототипа без тайла бонус пуст).
 *
 * NEOFORGE_PORT: SimpleChannel(ScanNetwork) -&gt; CustomPacketPayload
 * (пустое тело; порядок регистрации — в ScanNetwork).
 * Payload id "arcaneresearch:scan_wallet_request".
 */
public class MsgWalletRequest implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgWalletRequest> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_wallet_request"));

    public static final StreamCodec<ByteBuf, MsgWalletRequest> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgWalletRequest packet) {
        }

        @Override
        public MsgWalletRequest decode(ByteBuf buf) {
            return new MsgWalletRequest();
        }
    };

    public MsgWalletRequest() {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgWalletRequest msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                push(player);
            }
        });
    }

    /** Отправить игроку снимок (после каждой мутации пула). */
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
        PacketHandler.sendToPlayer(new MsgWalletSync(wallet, bonus), player);
    }
}
