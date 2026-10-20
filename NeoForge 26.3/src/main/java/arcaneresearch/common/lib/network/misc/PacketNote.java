package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.tiles.devices.TileArcaneEar;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketNote.java (BIDIRECTIONAL).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:note".
public class PacketNote implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketNote> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "note"));

    public static final StreamCodec<ByteBuf, PacketNote> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketNote packet) {
            buf.writeInt(packet.x);
            buf.writeInt(packet.y);
            buf.writeInt(packet.z);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.dim == null ? "" : packet.dim);
            buf.writeByte(packet.note);
        }

        @Override
        public PacketNote decode(ByteBuf buf) {
            PacketNote msg = new PacketNote();
            msg.x = buf.readInt();
            msg.y = buf.readInt();
            msg.z = buf.readInt();
            msg.dim = ByteBufCodecs.STRING_UTF8.decode(buf);
            msg.note = buf.readByte();
            return msg;
        }
    };

    private int x;
    private int y;
    private int z;
    private String dim;
    private byte note;

    public PacketNote() {
    }

    public PacketNote(int x, int y, int z, String dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        note = -1;
    }

    public PacketNote(int x, int y, int z, String dim, byte note) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.note = note;
    }

    public PacketNote(int x, int y, int z, int dimId) {
        this(x, y, z, String.valueOf(dimId));
    }

    public PacketNote(int x, int y, int z, int dimId, byte note) {
        this(x, y, z, String.valueOf(dimId), note);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketNote msg, IPayloadContext ctx) {
        // NEOFORGE_PORT: Forge branched on context.getDirection().getReceptionSide().isClient();
        // NeoForge equivalent is ctx.flow() (CLIENTBOUND on the client, SERVERBOUND on the server).
        if (ctx.flow() == PacketFlow.CLIENTBOUND) {
            ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
        } else {
            ctx.enqueueWork(() -> handleOnServer(msg, ctx));
        }
    }

    // Level comes from the packet context player (server-safe types only):
    // no Minecraft/ClientLevel refs, so dedicated-server verification stays clean.
    private static void handleOnClient(PacketNote message, net.minecraft.world.entity.player.Player player) {
        if (message.note < 0 || player == null) {
            return;
        }
        Level level = player.level();
        if (level == null) {
            return;
        }
        BlockEntity tile = level.getBlockEntity(new BlockPos(message.x, message.y, message.z));
        if (tile instanceof TileArcaneEar ear) {
            ear.note = message.note;
        }
    }

    private static void handleOnServer(PacketNote message, IPayloadContext ctx) {
        if (message.note != -1 || !(ctx.player() instanceof ServerPlayer sender)) {
            return;
        }
        ServerLevel world = sender.level();
        if (world == null) {
            return;
        }
        BlockEntity tile = world.getBlockEntity(new BlockPos(message.x, message.y, message.z));
        byte note = -1;
        if (tile instanceof TileArcaneEar ear) {
            note = ear.note;
        } else if (tile != null) {
            try {
                if (tile.getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE)) {
                    note = (byte) (int) tile.getBlockState().getValue(
                            net.minecraft.world.level.block.state.properties.BlockStateProperties.NOTE);
                } else if (tile.getBlockState().getBlock() == net.minecraft.world.level.block.Blocks.NOTE_BLOCK) {
                    note = 0;
                }
            } catch (Exception ignored) {
            }
        }
        if (note >= 0) {
            BlockPos pos = new BlockPos(message.x, message.y, message.z);
            PacketHandler.sendToNear(
                    new PacketNote(message.x, message.y, message.z, message.dim, note),
                    world, pos.getX(), pos.getY(), pos.getZ(), 8.0);
        }
    }

    @SuppressWarnings("unused")
    private static int instrumentId(NoteBlockInstrument instrument) {
        return instrument.ordinal();
    }
}