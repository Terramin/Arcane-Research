package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXShield.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_shield".
public class PacketFXShield implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXShield> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_shield"));

    public static final StreamCodec<ByteBuf, PacketFXShield> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXShield packet) {
            buf.writeInt(packet.sourceEntityId);
            buf.writeInt(packet.targetEntityId);
        }

        @Override
        public PacketFXShield decode(ByteBuf buf) {
            return new PacketFXShield(buf.readInt(), buf.readInt());
        }
    };


    private final int sourceEntityId;
    private final int targetEntityId;

    public PacketFXShield(int sourceEntityId, int targetEntityId) {
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXShield msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    // Level from the packet context player; FX creation in client FXDispatcher
    // (server-safe signature, client flow only).
    private static void handleClient(PacketFXShield packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            Entity source = level.getEntity(packet.sourceEntityId);
            if (source == null) return;
    
            float pitch = 0.0f;
            float yaw = 0.0f;
    
            if (packet.targetEntityId >= 0) {
    
                Entity target = level.getEntity(packet.targetEntityId);
                if (target != null) {
    
                    double dx = source.getX() - target.getX();
                    AABB sourceBB = source.getBoundingBox();
                    AABB targetBB = target.getBoundingBox();
                    double dy = (sourceBB.minY + sourceBB.maxY) / 2.0 - (targetBB.minY + targetBB.maxY) / 2.0;
                    double dz = source.getZ() - target.getZ();
                    double horizontal = Math.sqrt(dx * dx + dz * dz);
    
                    yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                    pitch = (float) (-(Math.atan2(dy, horizontal) * 180.0 / Math.PI));
                } else {
                    pitch = 90.0f;
                    yaw = 0.0f;
                }
    
                spawnShieldRunes(level, source, yaw, pitch);
            } else if (packet.targetEntityId == -1) {
    
                spawnShieldRunes(level, source, 0.0f, 90.0f);
                spawnShieldRunes(level, source, 0.0f, 270.0f);
            } else if (packet.targetEntityId == -2) {
    
                spawnShieldRunes(level, source, 0.0f, 270.0f);
            } else if (packet.targetEntityId == -3) {
    
                spawnShieldRunes(level, source, 0.0f, 90.0f);
            }
        }
    
        private static void spawnShieldRunes(Level level, Entity entity, float yaw, float pitch) {
            FXDispatcher.INSTANCE.drawShieldRunes(level, entity, yaw, pitch);
        }
}