package arcaneresearch.common.lib.network.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;
import arcaneresearch.common.tiles.crafting.TilePedestal;
import java.util.function.Supplier;
public class PacketFXInfusionSource {
    private final long p1;
    private final long p2;
    private final int color;
    public PacketFXInfusionSource(BlockPos matrixPos, BlockPos sourcePos, int color) {
        this.p1 = matrixPos.asLong();
        this.p2 = sourcePos.asLong();
        this.color = color;
    }
    private PacketFXInfusionSource(long p1, long p2, int color) {
        this.p1 = p1;
        this.p2 = p2;
        this.color = color;
    }
    public static void encode(PacketFXInfusionSource packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.p1);
        buffer.writeLong(packet.p2);
        buffer.writeInt(packet.color);
    }
    public static PacketFXInfusionSource decode(FriendlyByteBuf buffer) {
        long p1 = buffer.readLong();
        long p2 = buffer.readLong();
        int color = buffer.readInt();
        return new PacketFXInfusionSource(p1, p2, color);
    }
    public static void handle(PacketFXInfusionSource packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handleClient(packet);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketFXInfusionSource packet) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockPos matrixPos = BlockPos.of(packet.p1);
        BlockPos sourcePos = BlockPos.of(packet.p2);
        String key = sourcePos.getX() + ":" + sourcePos.getY() + ":" + sourcePos.getZ() + ":" + packet.color;
        BlockEntity tile = level.getBlockEntity(matrixPos);
        if (tile instanceof TileInfusionMatrix matrix) {
            int count = 15;
            BlockEntity sourceTile = level.getBlockEntity(sourcePos);
            if (sourceTile instanceof TilePedestal) {
                count = 120;
            }
            if (matrix.sourceFX.containsKey(key)) {
                TileInfusionMatrix.SourceFX sf = matrix.sourceFX.get(key);
                sf.ticks = count;
                matrix.sourceFX.put(key, sf);
            } else {
                matrix.sourceFX.put(key, new TileInfusionMatrix.SourceFX(sourcePos, count, packet.color));
            }
        }
    }
}
