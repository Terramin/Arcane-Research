package arcaneresearch.common.lib.network.misc;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.client.lib.events.HudHandler;





public class PacketKnowledgeGain implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketKnowledgeGain> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "knowledge_gain"));

    public static final StreamCodec<ByteBuf, PacketKnowledgeGain> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, p -> p.type,
            ByteBufCodecs.STRING_UTF8, p -> p.category,
            PacketKnowledgeGain::new);

    private final byte type;
    private final String category;

    public PacketKnowledgeGain(IPlayerKnowledge.EnumKnowledgeType type, String category) {
        this.type = (byte) type.ordinal();
        this.category = (category == null) ? "" : category;
    }

    private PacketKnowledgeGain(byte type, String category) {
        this.type = type;
        this.category = category;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketKnowledgeGain msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    
    
    private static void handleClient(PacketKnowledgeGain packet, Player player) {
        if (player == null) return;
        Level level = player.level();

        IPlayerKnowledge.EnumKnowledgeType type = IPlayerKnowledge.EnumKnowledgeType.values()[packet.type];
        arcaneresearch.api.research.ResearchCategory cat = (packet.category.length() > 0)
                ? arcaneresearch.api.research.ResearchCategories.getResearchCategory(packet.category)
                : null;

        HudHandler.trackKnowledgeGain(player, type, cat);

        if (arcaneresearch.init.ModSounds.LEARN.get() != null) {
            level.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    arcaneresearch.init.ModSounds.LEARN.get(), SoundSource.AMBIENT,
                    1.0f, 1.0f, false
            );
        }
    }
}
