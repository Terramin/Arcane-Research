package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketFXBoreDig implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXBoreDig> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_bore_dig"));

    public static final StreamCodec<ByteBuf, PacketFXBoreDig> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXBoreDig packet) {
            buf.writeInt(packet.x);
            buf.writeInt(packet.y);
            buf.writeInt(packet.z);
            buf.writeInt(packet.boreId);
            buf.writeInt(packet.delay);
        }

        @Override
        public PacketFXBoreDig decode(ByteBuf buf) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            int boreId = buf.readInt();
            int delay = buf.readInt();
            return new PacketFXBoreDig(x, y, z, boreId, delay);
        }
    };


    private final int x;
    private final int y;
    private final int z;
    private final int boreId;
    private final int delay;

    public PacketFXBoreDig(BlockPos pos, Entity bore, int delay) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.boreId = bore.getId();
        this.delay = delay;
    }

    private PacketFXBoreDig(int x, int y, int z, int boreId, int delay) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.boreId = boreId;
        this.delay = delay;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXBoreDig msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXBoreDig packet, Player player) {
            try {
                if (player == null) return;
                Level level = player.level();
                if (level == null) return;
    
                BlockPos pos = new BlockPos(packet.x, packet.y, packet.z);
                Entity bore = level.getEntity(packet.boreId);
    
                if (bore == null) {
                    return;
                }
    
                BlockState state = level.getBlockState(pos);
                if (state.is(Blocks.AIR)) {
                    return;
                }
    
                FXDispatcher.INSTANCE.boreDigFx(
                        pos.getX(), pos.getY(), pos.getZ(),
                        bore, state, 0, packet.delay
                );
            } catch (Exception ignored) {
    
            }
        }
}
