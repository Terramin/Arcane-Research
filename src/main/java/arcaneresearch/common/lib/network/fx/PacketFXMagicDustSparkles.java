package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Broadcasts the Salis Mundus dust sparkles to all nearby clients so that
 * remote players see the same FX as the caster.
 */
public class PacketFXMagicDustSparkles implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXMagicDustSparkles> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_magic_dust_sparkles"));

    public static final StreamCodec<ByteBuf, PacketFXMagicDustSparkles> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXMagicDustSparkles packet) {
            buf.writeInt(packet.playerId);
            buf.writeByte(packet.hand);
            buf.writeLong(packet.pos.asLong());
            buf.writeDouble(packet.hitX);
            buf.writeDouble(packet.hitY);
            buf.writeDouble(packet.hitZ);
            ByteBufCodecs.VAR_INT.encode(buf, packet.sparkles.size());
            for (BlockPos p : packet.sparkles) {
                buf.writeLong(p.asLong());
            }
        }

        @Override
        public PacketFXMagicDustSparkles decode(ByteBuf buf) {
            int playerId = buf.readInt();
            byte hand = buf.readByte();
            BlockPos pos = BlockPos.of(buf.readLong());
            double hitX = buf.readDouble();
            double hitY = buf.readDouble();
            double hitZ = buf.readDouble();
            int count = ByteBufCodecs.VAR_INT.decode(buf);
            List<BlockPos> sparkles = new ArrayList<>(count);
            for (int i = 0; i < count; ++i) {
                sparkles.add(BlockPos.of(buf.readLong()));
            }
            return new PacketFXMagicDustSparkles(playerId,
                    hand == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
                    pos, new Vec3(hitX, hitY, hitZ), sparkles);
        }
    };

    private final int playerId;
    private final byte hand;
    private final BlockPos pos;
    private final double hitX, hitY, hitZ;
    private final List<BlockPos> sparkles;

    public PacketFXMagicDustSparkles(Player player, BlockPos pos, Vec3 hitVec, InteractionHand hand, List<BlockPos> sparkles) {
        this(player == null ? -1 : player.getId(), hand, pos, hitVec, sparkles);
    }

    private PacketFXMagicDustSparkles(int playerId, InteractionHand hand, BlockPos pos, Vec3 hitVec, List<BlockPos> sparkles) {
        this.playerId = playerId;
        this.hand = (byte) (hand == InteractionHand.OFF_HAND ? 1 : 0);
        this.pos = pos;
        this.hitX = hitVec == null ? pos.getX() + 0.5 : hitVec.x;
        this.hitY = hitVec == null ? pos.getY() + 0.5 : hitVec.y;
        this.hitZ = hitVec == null ? pos.getZ() + 0.5 : hitVec.z;
        this.sparkles = sparkles != null ? sparkles : new ArrayList<>();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXMagicDustSparkles packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(packet));
    }

    private static void handleClient(PacketFXMagicDustSparkles packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Player player = null;
        if (packet.playerId >= 0) {
            Entity entity = level.getEntity(packet.playerId);
            if (entity instanceof Player p) {
                player = p;
            }
        }
        if (player == null) {
            return;
        }
        float handOffset = packet.hand == 0 ? 0.0f : 180.0f;
        double px = player.getX();
        double py = player.getBoundingBox().minY + player.getBbHeight() / 2.0f + 0.25;
        double pz = player.getZ();
        px += -net.minecraft.util.Mth.cos((player.getYRot() + handOffset) / 180.0f * (float) Math.PI) * 0.3f;
        pz += -net.minecraft.util.Mth.sin((player.getYRot() + handOffset) / 180.0f * (float) Math.PI) * 0.3f;
        Vec3 look = player.getLookAngle();
        px += look.x * 0.3;
        py += look.y * 0.3;
        pz += look.z * 0.3;
        Vec3 v1 = new Vec3(px, py, pz);
        Vec3 v2 = new Vec3(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5).subtract(v1);
        java.util.Random rand = new java.util.Random();
        for (int cnt = 50, a = 0; a < cnt; ++a) {
            boolean floaty = a < cnt / 3;
            float r = 1.0f;
            float g = (189 + level.getRandom().nextInt(67)) / 255.0f;
            float b = (64 + level.getRandom().nextInt(192)) / 255.0f;
            FXDispatcher.INSTANCE.drawSimpleSparkle(rand, v1.x, v1.y, v1.z,
                    v2.x / 6.0 + level.getRandom().nextGaussian() * 0.05,
                    v2.y / 6.0 + level.getRandom().nextGaussian() * 0.05 + (floaty ? 0.05 : 0.15),
                    v2.z / 6.0 + level.getRandom().nextGaussian() * 0.05,
                    0.5f, r, g, b, level.getRandom().nextInt(5),
                    floaty ? (0.3f + level.getRandom().nextFloat() * 0.5f) : 0.85f,
                    floaty ? 0.2f : 0.5f, 16);
        }
        Vec3 hit = new Vec3(packet.hitX, packet.hitY, packet.hitZ);
        for (BlockPos p : packet.sparkles) {
            FXDispatcher.INSTANCE.drawBlockSparkles(p, hit);
        }
    }
}
