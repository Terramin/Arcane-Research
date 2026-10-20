package arcaneresearch.common.lib.network.playerdata;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/playerdata/PacketWarpMessage.java (PLAY_TO_CLIENT).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:warp_message".
public class PacketWarpMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketWarpMessage> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "warp_message"));

    public static final StreamCodec<ByteBuf, PacketWarpMessage> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketWarpMessage packet) {
            buf.writeInt(packet.change);
            buf.writeByte(packet.type);
        }

        @Override
        public PacketWarpMessage decode(ByteBuf buf) {
            PacketWarpMessage msg = new PacketWarpMessage();
            msg.change = buf.readInt();
            msg.type = buf.readByte();
            return msg;
        }
    };

    private int change;
    private byte type;

    public PacketWarpMessage() {
        this.change = 0;
        this.type = 0;
    }

    public PacketWarpMessage(Player player, byte type, int change) {
        this.change = change;
        this.type = type;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketWarpMessage msg, IPayloadContext ctx) {
        if (msg.change != 0) {
            ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
        }
    }

    private static void handleOnClient(PacketWarpMessage msg, Player player) {
        if (player == null) return;

        String textKey;
        boolean playSound = msg.change > 0;

        switch (msg.type) {
            case 0 -> {
                if (msg.change > 0) {
                    textKey = "tc.addwarp";

                    player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.WHISPERS.get(), SoundSource.PLAYERS,
                    0.5f, 1.0f, false);
                } else {
                    textKey = "tc.removewarp";
                    playSound = false;
                }
            }
            case 1 -> {
                if (msg.change > 0) {
                    textKey = "tc.addwarpsticky";
                    player.level().playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.WHISPERS.get(), SoundSource.PLAYERS,
                    0.5f, 1.0f, false);
                } else {
                    textKey = "tc.removewarpsticky";
                    playSound = false;
                }
                player.sendOverlayMessage(Component.translatable(textKey));
            }
            case 2 -> {
                if (msg.change > 0) {
                    textKey = "tc.addwarptemp";
                } else {
                    textKey = "tc.removewarptemp";
                }
                player.sendOverlayMessage(Component.translatable(textKey));
            }
            default -> {
                return;
            }
        }
    }
}