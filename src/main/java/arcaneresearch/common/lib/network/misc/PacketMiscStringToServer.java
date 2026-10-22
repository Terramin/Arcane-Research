package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.menu.LogisticsMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketMiscStringToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketMiscStringToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "misc_string_to_server"));

    public static final StreamCodec<ByteBuf, PacketMiscStringToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketMiscStringToServer packet) {
            buf.writeInt(packet.messageId);
            
            ByteBufCodecs.stringUtf8(256).encode(buf, packet.text);
        }

        @Override
        public PacketMiscStringToServer decode(ByteBuf buf) {
            int messageId = buf.readInt();
            String text = ByteBufCodecs.stringUtf8(256).decode(buf);
            return new PacketMiscStringToServer(messageId, text);
        }
    };

    private final int messageId;
    private final String text;

    public PacketMiscStringToServer(int messageId, String text) {
        this.messageId = messageId;
        this.text = text != null ? text : "";
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMiscStringToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            switch (packet.messageId) {
                case 0 -> {

                    if (player.containerMenu instanceof LogisticsMenu logisticsMenu) {
                        logisticsMenu.setSearchText(packet.text);
                    }
                }

            }
        });
    }
}
