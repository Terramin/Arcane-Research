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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXFocusPartImpact.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_focus_part_impact".
public class PacketFXFocusPartImpact implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXFocusPartImpact> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_focus_part_impact"));

    public static final StreamCodec<ByteBuf, PacketFXFocusPartImpact> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXFocusPartImpact packet) {
            buf.writeFloat(packet.x);
            buf.writeFloat(packet.y);
            buf.writeFloat(packet.z);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.parts);
        }

        @Override
        public PacketFXFocusPartImpact decode(ByteBuf buf) {
            return new PacketFXFocusPartImpact(
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            ByteBufCodecs.STRING_UTF8.decode(buf)
            );
        }
    };


    private final float x, y, z;
    private final String parts;

    public PacketFXFocusPartImpact(double x, double y, double z, String[] parts) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("%");
            sb.append(parts[i]);
        }
        this.parts = sb.toString();
    }

    private PacketFXFocusPartImpact(float x, float y, float z, String parts) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.parts = parts;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXFocusPartImpact msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXFocusPartImpact packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            String[] partKeys = packet.parts.split("%");
            int amount = Math.max(1, 15 / partKeys.length);
            RandomSource rand = level.getRandom();
    
            for (String key : partKeys) {
                IFocusElement element = FocusEngine.getElement(key);
                if (element instanceof FocusEffect effect) {
                    for (int i = 0; i < amount; i++) {
    
                        double mx = rand.nextGaussian() * 0.15;
                        double my = rand.nextGaussian() * 0.15;
                        double mz = rand.nextGaussian() * 0.15;
    
                        effect.renderParticleFX(level, packet.x, packet.y, packet.z, mx, my, mz);
                    }
                }
            }
        }
}
