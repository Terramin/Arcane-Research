package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketBiomeChange implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketBiomeChange> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "biome_change"));

    public static final StreamCodec<ByteBuf, PacketBiomeChange> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketBiomeChange packet) {
            buf.writeInt(packet.x);
            buf.writeInt(packet.y);
            buf.writeInt(packet.z);
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.biomeId);
        }

        @Override
        public PacketBiomeChange decode(ByteBuf buf) {
            return new PacketBiomeChange(
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            ByteBufCodecs.STRING_UTF8.decode(buf)
            );
        }
    };

    private final int x;
    private final int y;
    private final int z;
    private final String biomeId;

    public PacketBiomeChange(BlockPos pos, ResourceKey<Biome> biome) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.biomeId = biome.identifier().toString();
    }

    public PacketBiomeChange(int x, int y, int z, String biomeId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.biomeId = biomeId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketBiomeChange packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(packet, ctx.player()));
    }

    
    
    private static void handleClient(PacketBiomeChange packet, Player player) {
        if (player == null) return;
        Level level = player.level();
        if (level == null) return;

        BlockPos pos = new BlockPos(packet.x, packet.y, packet.z);

        Identifier biomeRL = Identifier.parse(packet.biomeId);
        var biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        
        Holder<Biome> biomeHolder = biomeRegistry.get(ResourceKey.create(Registries.BIOME, biomeRL)).orElse(null);

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

            
            
            
            FXDispatcher.INSTANCE.markChunkSectionDirty(level, pos);
        }
    }
}