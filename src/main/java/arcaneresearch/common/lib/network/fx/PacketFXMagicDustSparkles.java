package arcaneresearch.common.lib.network.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.client.fx.FXDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Broadcasts the Salis Mundus dust sparkles to all nearby clients so that
 * remote players see the same FX as the caster.
 */
public class PacketFXMagicDustSparkles {
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

    public static void encode(PacketFXMagicDustSparkles packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.playerId);
        buffer.writeByte(packet.hand);
        buffer.writeBlockPos(packet.pos);
        buffer.writeDouble(packet.hitX);
        buffer.writeDouble(packet.hitY);
        buffer.writeDouble(packet.hitZ);
        buffer.writeVarInt(packet.sparkles.size());
        for (BlockPos p : packet.sparkles) {
            buffer.writeBlockPos(p);
        }
    }

    public static PacketFXMagicDustSparkles decode(FriendlyByteBuf buffer) {
        int playerId = buffer.readVarInt();
        byte hand = buffer.readByte();
        BlockPos pos = buffer.readBlockPos();
        double hitX = buffer.readDouble();
        double hitY = buffer.readDouble();
        double hitZ = buffer.readDouble();
        int count = buffer.readVarInt();
        List<BlockPos> sparkles = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            sparkles.add(buffer.readBlockPos());
        }
        return new PacketFXMagicDustSparkles(playerId, hand == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,
                pos, new Vec3(hitX, hitY, hitZ), sparkles);
    }

    public static void handle(PacketFXMagicDustSparkles packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
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
            float g = (189 + level.random.nextInt(67)) / 255.0f;
            float b = (64 + level.random.nextInt(192)) / 255.0f;
            FXDispatcher.INSTANCE.drawSimpleSparkle(rand, v1.x, v1.y, v1.z,
                    v2.x / 6.0 + level.random.nextGaussian() * 0.05,
                    v2.y / 6.0 + level.random.nextGaussian() * 0.05 + (floaty ? 0.05 : 0.15),
                    v2.z / 6.0 + level.random.nextGaussian() * 0.05,
                    0.5f, r, g, b, level.random.nextInt(5),
                    floaty ? (0.3f + level.random.nextFloat() * 0.5f) : 0.85f,
                    floaty ? 0.2f : 0.5f, 16);
        }
        Vec3 hit = new Vec3(packet.hitX, packet.hitY, packet.hitZ);
        for (BlockPos p : packet.sparkles) {
            FXDispatcher.INSTANCE.drawBlockSparkles(p, hit);
        }
    }
}
