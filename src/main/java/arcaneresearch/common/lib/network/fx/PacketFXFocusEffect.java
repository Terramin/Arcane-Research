package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.FocusEffect;
import arcaneresearch.api.casters.FocusEngine;
import arcaneresearch.api.casters.IFocusElement;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketFXFocusEffect implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXFocusEffect> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_focus_effect"));

    public static final StreamCodec<ByteBuf, PacketFXFocusEffect> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXFocusEffect packet) {
            buf.writeFloat(packet.x);
            buf.writeFloat(packet.y);
            buf.writeFloat(packet.z);
            buf.writeFloat(packet.motionX);
            buf.writeFloat(packet.motionY);
            buf.writeFloat(packet.motionZ);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.parts);
        }

        @Override
        public PacketFXFocusEffect decode(ByteBuf buf) {
            return new PacketFXFocusEffect(
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            ByteBufCodecs.STRING_UTF8.decode(buf)
            );
        }
    };


    private final float x, y, z;
    private final float motionX, motionY, motionZ;
    private final String parts;

    public PacketFXFocusEffect(float x, float y, float z, float motionX, float motionY, float motionZ, String[] parts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("%");
            sb.append(parts[i]);
        }
        this.parts = sb.toString();
    }

    private PacketFXFocusEffect(float x, float y, float z, float motionX, float motionY, float motionZ, String parts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.parts = parts;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXFocusEffect msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXFocusEffect packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            String[] partKeys = packet.parts.split("%");
            int amount = Math.max(1, 10 / partKeys.length);
    
            for (String key : partKeys) {
                IFocusElement element = FocusEngine.getElement(key);
                if (element instanceof FocusEffect effect) {
                    for (int i = 0; i < amount; i++) {
    
                        double mx = packet.motionX + level.getRandom().nextGaussian() / 20.0;
                        double my = packet.motionY + level.getRandom().nextGaussian() / 20.0;
                        double mz = packet.motionZ + level.getRandom().nextGaussian() / 20.0;
    
                        effect.renderParticleFX(level, packet.x, packet.y, packet.z, mx, my, mz);
                    }
                }
            }
        }
}
