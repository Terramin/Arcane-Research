package arcaneresearch.common.lib.network.playerdata;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.tiles.crafting.FocusElementNode;
import arcaneresearch.common.tiles.crafting.TileFocalManipulator;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/playerdata/PacketFocusNodesToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:focus_nodes_to_server".
public class PacketFocusNodesToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFocusNodesToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "focus_nodes_to_server"));

    public static final StreamCodec<ByteBuf, PacketFocusNodesToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFocusNodesToServer packet) {
            buf.writeLong(packet.pos);
            buf.writeByte(packet.data.size());

            for (FocusElementNode node : packet.data.values()) {
                CompoundTag nodeTag = node.serialize();
                ByteBufCodecs.COMPOUND_TAG.encode(buf, nodeTag);
            }

            ByteBufCodecs.STRING_UTF8.encode(buf, packet.name);
        }

        @Override
        public PacketFocusNodesToServer decode(ByteBuf buf) {
            long pos = buf.readLong();
            int count = buf.readByte();
            HashMap<Integer, FocusElementNode> data = new HashMap<>();

            for (int i = 0; i < count; i++) {
                CompoundTag nodeTag = ByteBufCodecs.COMPOUND_TAG.decode(buf);
                if (nodeTag != null) {
                    FocusElementNode node = new FocusElementNode();
                    node.deserialize(nodeTag);
                    data.put(node.id, node);
                }
            }

            String name = ByteBufCodecs.STRING_UTF8.decode(buf);

            return new PacketFocusNodesToServer(pos, data, name);
        }
    };

    private final long pos;
    private final HashMap<Integer, FocusElementNode> data;
    private final String name;

    public PacketFocusNodesToServer(BlockPos pos, HashMap<Integer, FocusElementNode> data, String name) {
        this.pos = pos.asLong();
        this.data = data;
        this.name = name;
    }

    private PacketFocusNodesToServer(long pos, HashMap<Integer, FocusElementNode> data, String name) {
        this.pos = pos;
        this.data = data;
        this.name = name;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFocusNodesToServer packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            Level level = player.level();
            BlockPos blockPos = BlockPos.of(packet.pos);

            if (!level.isLoaded(blockPos)) return;
            if (player.distanceToSqr(blockPos.getX(), blockPos.getY(), blockPos.getZ()) > 64.0) return;

            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof TileFocalManipulator focalManipulator) {
                focalManipulator.data.clear();
                focalManipulator.data.putAll(packet.data);
                focalManipulator.focusName = packet.name;
                focalManipulator.setChanged();
            }
        });
    }
}
