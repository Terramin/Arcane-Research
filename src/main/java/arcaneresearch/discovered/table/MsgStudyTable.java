package arcaneresearch.discovered.table;

import java.util.function.Supplier;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.scan.ScanTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class MsgStudyTable {

    private final BlockPos pos;

    public MsgStudyTable() {
        this.pos = null;
    }

    public MsgStudyTable(BlockPos pos) {
        this.pos = pos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    public MsgStudyTable(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public static void handle(MsgStudyTable msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || msg.pos == null) {
                return;
            }
            try {
                if (!ScanTargets.holdingThaumometer(player)) {
                    return;
                }
                if (player.getEyePosition().distanceTo(Vec3.atCenterOf(msg.pos))
                        > ScanTargets.RANGE + 1.0D) {
                    return;
                }
                Direction side = HexTableFormer.findPairSide(player.serverLevel(), msg.pos);
                if (side == null) {
                    return;
                }
                HexTableFormer.form(player.serverLevel(), msg.pos, side);
            } catch (Exception e) {
                Arcaneresearch.LOGGER.warn("MsgStudyTable: failed to form table at {}", msg.pos, e);
            }
        });
        context.setPacketHandled(true);
    }
}