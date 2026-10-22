package arcaneresearch.discovered.table;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.scan.ScanTargets;

 








 
public class MsgStudyTable implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgStudyTable> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hex_study_table"));

    public static final StreamCodec<ByteBuf, MsgStudyTable> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgStudyTable packet) {
            buf.writeLong(packet.pos);
        }

        @Override
        public MsgStudyTable decode(ByteBuf buf) {
            return new MsgStudyTable(BlockPos.of(buf.readLong()));
        }
    };

    private long pos;

    public MsgStudyTable() {
    }

    public MsgStudyTable(BlockPos pos) {
        this.pos = pos.asLong();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgStudyTable msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            BlockPos target = BlockPos.of(msg.pos);
            try {
                if (!holdingThaumometer(player)) {
                    return;
                }
                if (player.getEyePosition().distanceTo(
                        net.minecraft.world.phys.Vec3.atCenterOf(target))
                        > ScanTargets.RANGE + 1.0D) {
                    return;
                }
                Direction side = HexTableFormer.findPairSide(player.level(), target);
                if (side == null) {
                    return;
                }
                HexTableFormer.form(player.level(), target, side);
            } catch (Exception ignored) {
            }
        });
    }

    private static boolean holdingThaumometer(ServerPlayer player) {
        return arcaneresearch.discovered.scan.ScanTargets.holdingThaumometer(player);
    }
}
