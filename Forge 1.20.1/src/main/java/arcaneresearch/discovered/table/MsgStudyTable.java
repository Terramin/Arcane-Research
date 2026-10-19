package arcaneresearch.discovered.table;
import java.util.function.Supplier;
import arcaneresearch.discovered.scan.ScanTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.init.ModItems;
public class MsgStudyTable {
    private BlockPos pos;
    public MsgStudyTable() {
    }
    public MsgStudyTable(BlockPos pos) {
        this.pos = pos;
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }
    public MsgStudyTable(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }
    public static void handle(MsgStudyTable msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ctx.get().getDirection().getReceptionSide().isServer()) {
                return;
            }
            ServerPlayer player = ctx.get().getSender();
            if (player == null || msg.pos == null) {
                return;
            }
            try {
                if (!holdingThaumometer(player)) {
                    return;
                }
                if (player.getEyePosition().distanceTo(
                        net.minecraft.world.phys.Vec3.atCenterOf(msg.pos))
                        > ScanTargets.RANGE + 1.0D) {
                    return;
                }
                Direction side = HexTableFormer.findPairSide(player.serverLevel(), msg.pos);
                if (side == null) {
                    return;
                }
                HexTableFormer.form(player.serverLevel(), msg.pos, side);
            } catch (Exception ignored) {
            }
        });
        ctx.get().setPacketHandled(true);
    }
    private static boolean holdingThaumometer(ServerPlayer player) {
        return arcaneresearch.discovered.scan.ScanTargets.holdingThaumometer(player);
    }
}
