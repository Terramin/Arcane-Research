package arcaneresearch.common.lib.network;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.lib.network.fx.*;
import arcaneresearch.common.lib.network.misc.*;
import arcaneresearch.common.lib.network.playerdata.*;
import arcaneresearch.common.lib.network.tiles.PacketTileToClient;
import arcaneresearch.common.lib.network.tiles.PacketTileToServer;
public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Arcaneresearch.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;
    private static int nextId() {
        return packetId++;
    }
    public static void init() {
        Arcaneresearch.LOGGER.info("Registering Arcaneresearch network packets");
        INSTANCE.messageBuilder(PacketSealToClient.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketSealToClient::encode)
                .decoder(PacketSealToClient::decode)
                .consumerMainThread(PacketSealToClient::handle)
                .add();
        INSTANCE.messageBuilder(PacketSyncKnowledge.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketSyncKnowledge::encode)
                .decoder(PacketSyncKnowledge::decode)
                .consumerMainThread(PacketSyncKnowledge::handle)
                .add();
        INSTANCE.messageBuilder(PacketSyncWarp.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketSyncWarp::encode)
                .decoder(PacketSyncWarp::decode)
                .consumerMainThread(PacketSyncWarp::handle)
                .add();
        INSTANCE.messageBuilder(PacketWarpMessage.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketWarpMessage::encode)
                .decoder(PacketWarpMessage::decode)
                .consumerMainThread(PacketWarpMessage::handle)
                .add();
        INSTANCE.messageBuilder(PacketAuraToClient.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketAuraToClient::encode)
                .decoder(PacketAuraToClient::decode)
                .consumerMainThread(PacketAuraToClient::handle)
                .add();
        INSTANCE.messageBuilder(PacketTileToClient.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketTileToClient::encode)
                .decoder(PacketTileToClient::decode)
                .consumerMainThread(PacketTileToClient::handle)
                .add();
        INSTANCE.messageBuilder(PacketTileToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketTileToServer::encode)
                .decoder(PacketTileToServer::decode)
                .consumerMainThread(PacketTileToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXSlash.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXSlash::encode)
                .decoder(PacketFXSlash::decode)
                .consumerMainThread(PacketFXSlash::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXBlockArc.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXBlockArc::encode)
                .decoder(PacketFXBlockArc::decode)
                .consumerMainThread(PacketFXBlockArc::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXBlockBamf.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXBlockBamf::encode)
                .decoder(PacketFXBlockBamf::decode)
                .consumerMainThread(PacketFXBlockBamf::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXZap.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXZap::encode)
                .decoder(PacketFXZap::decode)
                .consumerMainThread(PacketFXZap::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXNodeBolt.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXNodeBolt::encode)
                .decoder(PacketFXNodeBolt::decode)
                .consumerMainThread(PacketFXNodeBolt::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXEssentiaSource.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXEssentiaSource::encode)
                .decoder(PacketFXEssentiaSource::decode)
                .consumerMainThread(PacketFXEssentiaSource::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXShield.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXShield::encode)
                .decoder(PacketFXShield::decode)
                .consumerMainThread(PacketFXShield::handle)
                .add();
        INSTANCE.messageBuilder(arcaneresearch.common.lib.network.playerdata.PacketRunicCharge.class,
                        nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(arcaneresearch.common.lib.network.playerdata.PacketRunicCharge::encode)
                .decoder(arcaneresearch.common.lib.network.playerdata.PacketRunicCharge::decode)
                .consumerMainThread(arcaneresearch.common.lib.network.playerdata.PacketRunicCharge::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXWispZap.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXWispZap::encode)
                .decoder(PacketFXWispZap::decode)
                .consumerMainThread(PacketFXWispZap::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXFocusEffect.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXFocusEffect::encode)
                .decoder(PacketFXFocusEffect::decode)
                .consumerMainThread(PacketFXFocusEffect::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXFocusPartImpact.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXFocusPartImpact::encode)
                .decoder(PacketFXFocusPartImpact::decode)
                .consumerMainThread(PacketFXFocusPartImpact::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXFocusPartImpactBurst.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXFocusPartImpactBurst::encode)
                .decoder(PacketFXFocusPartImpactBurst::decode)
                .consumerMainThread(PacketFXFocusPartImpactBurst::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXInfusionSource.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXInfusionSource::encode)
                .decoder(PacketFXInfusionSource::decode)
                .consumerMainThread(PacketFXInfusionSource::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXInfusionStream.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXInfusionStream::encode)
                .decoder(PacketFXInfusionStream::decode)
                .consumerMainThread(PacketFXInfusionStream::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXPollute.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXPollute::encode)
                .decoder(PacketFXPollute::decode)
                .consumerMainThread(PacketFXPollute::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXBoreDig.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXBoreDig::encode)
                .decoder(PacketFXBoreDig::decode)
                .consumerMainThread(PacketFXBoreDig::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXScanSource.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXScanSource::encode)
                .decoder(PacketFXScanSource::decode)
                .consumerMainThread(PacketFXScanSource::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXSonic.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXSonic::encode)
                .decoder(PacketFXSonic::decode)
                .consumerMainThread(PacketFXSonic::handle)
                .add();
        INSTANCE.messageBuilder(PacketFXBlockMist.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketFXBlockMist::encode)
                .decoder(PacketFXBlockMist::decode)
                .consumerMainThread(PacketFXBlockMist::handle)
                .add();
        INSTANCE.messageBuilder(PacketSyncProgressToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketSyncProgressToServer::encode)
                .decoder(PacketSyncProgressToServer::decode)
                .consumerMainThread(PacketSyncProgressToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketSyncResearchFlagsToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketSyncResearchFlagsToServer::encode)
                .decoder(PacketSyncResearchFlagsToServer::decode)
                .consumerMainThread(PacketSyncResearchFlagsToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketFocusChangeToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketFocusChangeToServer::encode)
                .decoder(PacketFocusChangeToServer::decode)
                .consumerMainThread(PacketFocusChangeToServer::handle)
                .add();
        INSTANCE.messageBuilder(arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer::encode)
                .decoder(arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer::decode)
                .consumerMainThread(arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketItemKeyToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketItemKeyToServer::encode)
                .decoder(PacketItemKeyToServer::decode)
                .consumerMainThread(PacketItemKeyToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketFocusNodesToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketFocusNodesToServer::encode)
                .decoder(PacketFocusNodesToServer::decode)
                .consumerMainThread(PacketFocusNodesToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketPlayerFlagToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketPlayerFlagToServer::encode)
                .decoder(PacketPlayerFlagToServer::decode)
                .consumerMainThread(PacketPlayerFlagToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketMiscEvent.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketMiscEvent::encode)
                .decoder(PacketMiscEvent::decode)
                .consumerMainThread(PacketMiscEvent::handle)
                .add();
        INSTANCE.messageBuilder(PacketKnowledgeGain.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketKnowledgeGain::encode)
                .decoder(PacketKnowledgeGain::decode)
                .consumerMainThread(PacketKnowledgeGain::handle)
                .add();
        INSTANCE.messageBuilder(PacketSealFilterToClient.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketSealFilterToClient::encode)
                .decoder(PacketSealFilterToClient::decode)
                .consumerMainThread(PacketSealFilterToClient::handle)
                .add();
        INSTANCE.messageBuilder(PacketBiomeChange.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PacketBiomeChange::encode)
                .decoder(PacketBiomeChange::decode)
                .consumerMainThread(PacketBiomeChange::handle)
                .add();
        INSTANCE.messageBuilder(PacketLogisticsRequestToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketLogisticsRequestToServer::encode)
                .decoder(PacketLogisticsRequestToServer::decode)
                .consumerMainThread(PacketLogisticsRequestToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketMiscStringToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketMiscStringToServer::encode)
                .decoder(PacketMiscStringToServer::decode)
                .consumerMainThread(PacketMiscStringToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketStartTheoryToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketStartTheoryToServer::encode)
                .decoder(PacketStartTheoryToServer::decode)
                .consumerMainThread(PacketStartTheoryToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketSelectThaumotoriumRecipeToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketSelectThaumotoriumRecipeToServer::encode)
                .decoder(PacketSelectThaumotoriumRecipeToServer::decode)
                .consumerMainThread(PacketSelectThaumotoriumRecipeToServer::handle)
                .add();
        INSTANCE.messageBuilder(PacketFocusNameToServer.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(PacketFocusNameToServer::encode)
                .decoder(PacketFocusNameToServer::decode)
                .consumerMainThread(PacketFocusNameToServer::handle)
                .add();
        INSTANCE.messageBuilder(arcaneresearch.common.lib.network.misc.PacketItemToClientContainer.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(arcaneresearch.common.lib.network.misc.PacketItemToClientContainer::encode)
                .decoder(arcaneresearch.common.lib.network.misc.PacketItemToClientContainer::decode)
                .consumerMainThread(arcaneresearch.common.lib.network.misc.PacketItemToClientContainer::handle)
                .add();
        INSTANCE.messageBuilder(arcaneresearch.common.lib.network.misc.PacketNote.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(arcaneresearch.common.lib.network.misc.PacketNote::encode)
                .decoder(arcaneresearch.common.lib.network.misc.PacketNote::decode)
                .consumerMainThread(arcaneresearch.common.lib.network.misc.PacketNote::handle)
                .add();
        INSTANCE.messageBuilder(arcaneresearch.common.lib.network.misc.PacketNote.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(arcaneresearch.common.lib.network.misc.PacketNote::encode)
                .decoder(arcaneresearch.common.lib.network.misc.PacketNote::decode)
                .consumerMainThread(arcaneresearch.common.lib.network.misc.PacketNote::handle)
                .add();
        Arcaneresearch.LOGGER.info("Arcaneresearch network packets registered");
    }
    public static <MSG> void sendToPlayer(MSG packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
    public static <MSG> void sendToAll(MSG packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
    public static <MSG> void sendToAllTracking(MSG packet, net.minecraft.world.entity.Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
    }
    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.sendToServer(packet);
    }
    public static <MSG> void sendToNear(MSG packet, PacketDistributor.TargetPoint targetPoint) {
        INSTANCE.send(PacketDistributor.NEAR.with(() -> targetPoint), packet);
    }
    public static <MSG> void sendToDimension(MSG packet, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> dimension), packet);
    }
    public static <MSG> void sendToAllTracking(MSG packet, BlockEntity blockEntity) {
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = blockEntity.getBlockPos();
            serverLevel.getChunkSource().chunkMap.getPlayers(
                    new net.minecraft.world.level.ChunkPos(pos), false
            ).forEach(player -> sendToPlayer(packet, player));
        }
    }
    public static <MSG> void sendToAllTrackingChunk(MSG packet, ServerLevel level, BlockPos pos) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }
}
