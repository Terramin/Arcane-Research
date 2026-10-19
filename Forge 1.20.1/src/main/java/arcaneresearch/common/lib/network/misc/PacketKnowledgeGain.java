package arcaneresearch.common.lib.network.misc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.init.ModSounds;
import java.util.function.Supplier;
public class PacketKnowledgeGain {
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
    public static void encode(PacketKnowledgeGain packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.type);
        buf.writeUtf(packet.category);
    }
    public static PacketKnowledgeGain decode(FriendlyByteBuf buf) {
        return new PacketKnowledgeGain(
                buf.readByte(),
                buf.readUtf(32767)
        );
    }
    public static void handle(PacketKnowledgeGain packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketKnowledgeGain packet) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        IPlayerKnowledge.EnumKnowledgeType type = IPlayerKnowledge.EnumKnowledgeType.values()[packet.type];
        ResearchCategory cat = (packet.category.length() > 0)
                ? ResearchCategories.getResearchCategory(packet.category)
                : null;
        arcaneresearch.client.lib.events.HudHandler.knowledgeGainTrackers.add(
                new arcaneresearch.client.lib.events.HudHandler.KnowledgeGainTracker(
                        type, cat, 40 + player.getRandom().nextInt(20), player.getRandom().nextLong()));
        if (ModSounds.LEARN.get() != null) {
            mc.level.playLocalSound(
                    player.getX(), player.getY(), player.getZ(),
                    ModSounds.LEARN.get(), SoundSource.AMBIENT,
                    1.0f, 1.0f, false
            );
        }
    }
}
