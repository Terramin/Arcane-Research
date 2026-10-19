package arcaneresearch.common.lib.network.misc;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PacketBiomeChange {
    private final int x;
    private final int y;
    private final int z;
    private final String biomeId;
    public PacketBiomeChange(BlockPos pos, ResourceKey<Biome> biome) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.biomeId = biome.location().toString();
    }
    public PacketBiomeChange(int x, int y, int z, String biomeId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.biomeId = biomeId;
    }
    public static void encode(PacketBiomeChange packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.x);
        buf.writeInt(packet.y);
        buf.writeInt(packet.z);
        buf.writeUtf(packet.biomeId);
    }
    public static PacketBiomeChange decode(FriendlyByteBuf buf) {
        return new PacketBiomeChange(
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readUtf(32767)
        );
    }
    public static void handle(PacketBiomeChange packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleClient(packet));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleClient(PacketBiomeChange packet) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        if (level == null) return;
        BlockPos pos = new BlockPos(packet.x, packet.y, packet.z);
        ResourceLocation biomeRL = new ResourceLocation(packet.biomeId);
        var biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
        Holder<Biome> biomeHolder = biomeRegistry.getHolder(ResourceKey.create(Registries.BIOME, biomeRL)).orElse(null);
        if (biomeHolder == null) return;
        LevelChunk chunk = level.getChunkAt(pos);
        int sectionIndex = level.getSectionIndex(pos.getY());
        if (sectionIndex >= 0 && sectionIndex < chunk.getSections().length) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            int bx = (pos.getX() & 15) >> 2;
            int by = (pos.getY() & 15) >> 2;
            int bz = (pos.getZ() & 15) >> 2;
            if (section.getBiomes() instanceof PalettedContainer<Holder<Biome>> biomes) {
                biomes.set(bx, by, bz, biomeHolder);
            }
            mc.levelRenderer.setBlocksDirty(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
        }
    }
}
