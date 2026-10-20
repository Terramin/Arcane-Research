package arcaneresearch.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketMiscEvent.java (PLAY_TO_CLIENT).
// Fields/ctors/constants preserved verbatim; transport -> CustomPacketPayload.
// Wire order identical to the old encode/decode: byte type, then int value ONLY if != 0
// (decode reads the int only when readable bytes remain).
// Payload id "arcaneresearch:misc_event".
public class PacketMiscEvent implements CustomPacketPayload {

    public static final byte WARP_EVENT = 0;
    public static final byte MIST_EVENT = 1;
    public static final byte MIST_EVENT_SHORT = 2;

    public static final CustomPacketPayload.Type<PacketMiscEvent> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "misc_event"));

    public static final StreamCodec<ByteBuf, PacketMiscEvent> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketMiscEvent packet) {
            ByteBufCodecs.BYTE.encode(buf, packet.type);
            if (packet.value != 0) {
                ByteBufCodecs.INT.encode(buf, packet.value);
            }
        }

        @Override
        public PacketMiscEvent decode(ByteBuf buf) {
            byte type = ByteBufCodecs.BYTE.decode(buf);
            int value = 0;
            if (buf.readableBytes() >= 4) {
                value = ByteBufCodecs.INT.decode(buf);
            }
            return new PacketMiscEvent(type, value);
        }
    };

    private final byte type;
    private final int value;

    public PacketMiscEvent(byte type) {
        this.type = type;
        this.value = 0;
    }

    public PacketMiscEvent(byte type, int value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketMiscEvent msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    // Player/Level from the packet context (server-safe types only).
    // FogHandler/ModSounds calls use safe signatures — PLAY_TO_CLIENT, client flow only.
    private static void handleClient(PacketMiscEvent packet, Player player) {
        if (player == null) return;
        Level level = player.level();
        if (level == null) return;

        switch (packet.type) {
            case WARP_EVENT -> {

                if (!arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.nostress && arcaneresearch.init.ModSounds.HEARTBEAT.get() != null) {
                    level.playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            arcaneresearch.init.ModSounds.HEARTBEAT.get(), net.minecraft.sounds.SoundSource.AMBIENT,
                            1.0f, 1.0f, false
                    );
                }
            }
            case MIST_EVENT -> {

                arcaneresearch.client.lib.events.FogHandler.triggerLongFog();
            }
            case MIST_EVENT_SHORT -> {

                arcaneresearch.client.lib.events.FogHandler.triggerShortFog();
            }
        }
    }
}
