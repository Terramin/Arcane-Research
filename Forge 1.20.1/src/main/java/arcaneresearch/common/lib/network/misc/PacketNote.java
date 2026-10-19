package arcaneresearch.common.lib.network.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.tiles.devices.TileArcaneEar;
import java.util.function.Supplier;
public class PacketNote {
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
    public static void encode(PacketNote msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
        buf.writeInt(msg.z);
        buf.writeUtf(msg.dim == null ? "" : msg.dim);
        buf.writeByte(msg.note);
    }
    public static PacketNote decode(FriendlyByteBuf buf) {
        PacketNote msg = new PacketNote();
        msg.x = buf.readInt();
        msg.y = buf.readInt();
        msg.z = buf.readInt();
        msg.dim = buf.readUtf();
        msg.note = buf.readByte();
        return msg;
    }
    public static void handle(PacketNote msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> handleOnClient(msg));
        } else {
            context.enqueueWork(() -> handleOnServer(msg, context));
        }
        context.setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketNote message) {
        if (message.note < 0) {
            return;
        }
        Level level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        BlockEntity tile = level.getBlockEntity(new BlockPos(message.x, message.y, message.z));
        if (tile instanceof TileArcaneEar ear) {
            ear.note = message.note;
        }
    }
    private static void handleOnServer(PacketNote message, NetworkEvent.Context ctx) {
        if (message.note != -1 || ctx.getSender() == null) {
            return;
        }
        ServerLevel world = ctx.getSender().serverLevel();
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
            PacketHandler.INSTANCE.send(
                    PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                            pos.getX(), pos.getY(), pos.getZ(), 8.0, world.dimension())),
                    new PacketNote(message.x, message.y, message.z, message.dim, note));
        }
    }
    @SuppressWarnings("unused")
    private static int instrumentId(NoteBlockInstrument instrument) {
        return instrument.ordinal();
    }
}
