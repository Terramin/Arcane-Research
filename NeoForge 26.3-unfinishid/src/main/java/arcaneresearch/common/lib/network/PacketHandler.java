package arcaneresearch.common.lib.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (complete: every Forge packet is registered below):
// Forge 1.20.1 source of truth:
//   Forge 1.20.1/src/main/java/arcaneresearch/common/lib/network/PacketHandler.java
//   - SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(Identifier(MODID,"main"), ...)
//   - ~45 INSTANCE.messageBuilder(...).encoder().decoder().consumerMainThread().add() in fixed order
//     (PacketSealToClient, PacketSyncKnowledge, PacketSyncWarp, PacketWarpMessage, PacketAuraToClient,
//      PacketTileToClient/ToServer, PacketFX* (15), PacketSync*ToServer, PacketFocus*ToServer, ...)
//   - helpers sendToPlayer/sendToAll/sendToAllTracking/sendToServer/sendToNear/sendToDimension.
//
// NeoForge 26.3 REQUIRED transport change (only transport layer; payload content stays 1:1):
//   SimpleChannel/NetworkRegistry/PacketDistributor(F) -> CustomPacketPayload + StreamCodec
//     + RegisterPayloadHandlersEvent registrar.playToClient/playToServer/playBidirectional
//     + PacketDistributor.sendToPlayer/sendToAllPlayers/sendToPlayersTrackingChunk/... + ClientPacketDistributor.sendToServer
//   FriendlyByteBuf encode/decode/handle triple -> record implements CustomPacketPayload { TYPE, STREAM_CODEC, type() }
//     + handler(IPayload, IPayloadContext).
//   Identifier(MODID, path) id -> Identifier.fromNamespaceAndPath(MODID, path) (26.x: Identifier->Identifier).
//
// Each packet is ported 1:1 as a CustomPacketPayload, with the same fields/handlers.
//   Packet list (= registration order, must be preserved):
//   SealToClient, SyncKnowledge, SyncWarp, WarpMessage, AuraToClient, TileToClient, TileToServer,
//   FXSlash, FXBlockArc, FXBlockBamf, FXZap, FXEssentiaSource, FXShield, FXWispZap, FXFocusEffect,
//   FXFocusPartImpact, FXFocusPartImpactBurst, FXInfusionSource, FXInfusionStream, FXPollute, FXBoreDig,
//   FXScanSource, FXSonic, FXBlockMist, SyncProgressToServer, SyncResearchFlagsToServer, FocusChangeToServer,
//   WandFocusChangeToServer, ItemKeyToServer, FocusNodesToServer, PlayerFlagToServer, MiscEvent, KnowledgeGain,
//   SealFilterToClient, BiomeChange, LogisticsRequestToServer, MiscStringToServer, StartTheoryToServer,
//   SelectThaumotoriumRecipeToServer, FocusNameToServer, ItemToClientContainer, Note (x2 directions).
public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final var registrar = event.registrar(Arcaneresearch.MODID).versioned(PROTOCOL_VERSION);

        // PORTED (playToClient, MAIN thread = Forge consumerMainThread):
        registrar.playToClient(
                arcaneresearch.common.lib.network.playerdata.PacketSyncKnowledge.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketSyncKnowledge.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketSyncKnowledge::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.playerdata.PacketSyncWarp.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketSyncWarp.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketSyncWarp::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXPollute.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXPollute.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXPollute::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.tiles.PacketTileToClient.TYPE,
                arcaneresearch.common.lib.network.tiles.PacketTileToClient.STREAM_CODEC,
                arcaneresearch.common.lib.network.tiles.PacketTileToClient::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.tiles.PacketTileToServer.TYPE,
                arcaneresearch.common.lib.network.tiles.PacketTileToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.tiles.PacketTileToServer::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXZap.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXZap.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXZap::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketKnowledgeGain.TYPE,
                arcaneresearch.common.lib.network.misc.PacketKnowledgeGain.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketKnowledgeGain::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketMiscEvent.TYPE,
                arcaneresearch.common.lib.network.misc.PacketMiscEvent.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketMiscEvent::handle);
        // PORTED FX packets (playToClient, Forge order; registration order no longer matters - 26.x uses named payload ids):
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXSlash.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXSlash.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXSlash::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXBlockArc.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXBlockArc.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXBlockArc::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXBlockBamf.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXBlockBamf.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXBlockBamf::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXEssentiaSource.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXEssentiaSource.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXEssentiaSource::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXShield.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXShield.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXShield::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXWispZap.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXWispZap.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXWispZap::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXFocusEffect.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXFocusEffect.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXFocusEffect::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpact.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpact.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpact::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpactBurst.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpactBurst.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXFocusPartImpactBurst::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXInfusionSource.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXInfusionSource.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXInfusionSource::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXInfusionStream.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXInfusionStream.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXInfusionStream::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXBoreDig.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXBoreDig.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXBoreDig::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXScanSource.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXScanSource.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXScanSource::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXSonic.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXSonic.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXSonic::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXBlockMist.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXBlockMist.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXBlockMist::handle);
        // Паритет Forge (3-day batch): разряд узлового болта + синк рунного заряда.
        registrar.playToClient(
                arcaneresearch.common.lib.network.fx.PacketFXNodeBolt.TYPE,
                arcaneresearch.common.lib.network.fx.PacketFXNodeBolt.STREAM_CODEC,
                arcaneresearch.common.lib.network.fx.PacketFXNodeBolt::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.playerdata.PacketRunicCharge.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketRunicCharge.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketRunicCharge::handle);
        // PORTED misc/playerdata/wand packets (Forge registration order):
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketSealToClient.TYPE,
                arcaneresearch.common.lib.network.misc.PacketSealToClient.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketSealToClient::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.playerdata.PacketWarpMessage.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketWarpMessage.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketWarpMessage::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketAuraToClient.TYPE,
                arcaneresearch.common.lib.network.misc.PacketAuraToClient.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketAuraToClient::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.playerdata.PacketSyncResearchFlagsToServer.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketSyncResearchFlagsToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketSyncResearchFlagsToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.misc.PacketFocusChangeToServer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketFocusChangeToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketFocusChangeToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer.TYPE,
                arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.misc.PacketItemKeyToServer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketItemKeyToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketItemKeyToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.playerdata.PacketFocusNodesToServer.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketFocusNodesToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketFocusNodesToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.playerdata.PacketPlayerFlagToServer.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketPlayerFlagToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketPlayerFlagToServer::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketSealFilterToClient.TYPE,
                arcaneresearch.common.lib.network.misc.PacketSealFilterToClient.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketSealFilterToClient::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketBiomeChange.TYPE,
                arcaneresearch.common.lib.network.misc.PacketBiomeChange.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketBiomeChange::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.misc.PacketLogisticsRequestToServer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketLogisticsRequestToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketLogisticsRequestToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.misc.PacketMiscStringToServer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketMiscStringToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketMiscStringToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.misc.PacketStartTheoryToServer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketStartTheoryToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketStartTheoryToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketSelectThaumotoriumRecipeToServer::handle);
        registrar.playToServer(
                arcaneresearch.common.lib.network.playerdata.PacketFocusNameToServer.TYPE,
                arcaneresearch.common.lib.network.playerdata.PacketFocusNameToServer.STREAM_CODEC,
                arcaneresearch.common.lib.network.playerdata.PacketFocusNameToServer::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.misc.PacketItemToClientContainer.TYPE,
                arcaneresearch.common.lib.network.misc.PacketItemToClientContainer.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketItemToClientContainer::handle);
        registrar.playToClient(
                arcaneresearch.common.lib.network.entity.PacketFocusPackToClient.TYPE,
                arcaneresearch.common.lib.network.entity.PacketFocusPackToClient.STREAM_CODEC,
                arcaneresearch.common.lib.network.entity.PacketFocusPackToClient::handle);
        // NEOFORGE_PORT: since 1.21.6 (PR #2272) single-handler playBidirectional
        // registers ONLY the serverbound handler; the clientbound handler for the
        // same type must be registered via RegisterClientPayloadHandlersEvent
        // (physical-client-only subscriber below), or the network registry lock
        // fails boot with "missing client-side handlers: [arcaneresearch:note]".
        registrar.playBidirectional(
                arcaneresearch.common.lib.network.misc.PacketNote.TYPE,
                arcaneresearch.common.lib.network.misc.PacketNote.STREAM_CODEC,
                arcaneresearch.common.lib.network.misc.PacketNote::handle);

    // NEOFORGE_PORT: all Forge payloads registered above (Forge's numeric-id ORDER is
    // irrelevant now - 26.x addresses payloads by type/name)
    // (ids = lowercase path; PacketNote once as bidirectional instead of twice).
    }

    // Helper layer — same names/signatures as Forge, NeoForge transport underneath.
    // NEOFORGE_PORT: Forge INSTANCE.send(PacketDistributor.PLAYER.with(...), packet)
    //   -> NeoForge PacketDistributor.sendToPlayer(player, payload). Cast is safe: every ported
    //   packet implements CustomPacketPayload (see per-packet javadoc + id table above).
    @SuppressWarnings("unchecked")
    public static <MSG> void sendToPlayer(MSG packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, (net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
    }

    @SuppressWarnings("unchecked")
    public static <MSG> void sendToAll(MSG packet) {
        PacketDistributor.sendToAllPlayers((net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
    }

    public static <MSG> void sendToServer(MSG packet) {
        // NEOFORGE_PORT: Forge INSTANCE.sendToServer(packet) (client -> server)
        //   -> NeoForge ClientPacketDistributor.sendToServer(payload) (client package).
        net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
                (net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
    }

    public static <MSG> void sendToAllTrackingChunk(MSG packet, net.minecraft.server.level.ServerLevel level,
                                                     net.minecraft.core.BlockPos pos) {
        // NEOFORGE_PORT: Forge TRACKING_CHUNK.with(level.getChunkAt(pos))
        //   -> NeoForge sendToPlayersTrackingChunk(level, ChunkPos, payload).
        PacketDistributor.sendToPlayersTrackingChunk(level, net.minecraft.world.level.ChunkPos.containing(pos),
                (net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
    }

    @SuppressWarnings("unchecked")
    public static <MSG> void sendToAllTracking(MSG packet, net.minecraft.world.level.block.entity.BlockEntity tile) {
        // NEOFORGE_PORT: Forge sendToAllTracking(packet, tile) helper (same name/signature)
        //   -> chunk-trackers of the tile's level/pos (server-side only, mirrors Forge behavior).
        if (tile.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel,
                    net.minecraft.world.level.ChunkPos.containing(tile.getBlockPos()),
                    (net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
        }
    }

    @SuppressWarnings("unchecked")
    public static <MSG> void sendToAllTracking(MSG packet, net.minecraft.world.entity.Entity entity) {
        // NEOFORGE_PORT: Forge TRACKING_ENTITY.with(() -> entity) -> sendToPlayersTrackingEntity.
        PacketDistributor.sendToPlayersTrackingEntity(entity,
                (net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
    }

    // Full helper set from Forge (sendToAllTracking(entity/blockEntity/chunk), sendToNear, sendToDimension)
    // will be restored 1:1 in the packet port phase.
    @SuppressWarnings("unchecked")
    public static <MSG> void sendToNear(MSG packet, net.minecraft.world.level.Level level,
                                        double x, double y, double z, double range) {
        // NEOFORGE_PORT: Forge sendToNear(packet, TargetPoint(x, y, z, range, dim)) removed ->
        //   PacketDistributor.sendToPlayersNear(level, except, x, y, z, range, payload).
        // Same visible behavior (players within a sphere, except none); server-side only.
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, x, y, z, range,
                    (net.minecraft.network.protocol.common.custom.CustomPacketPayload) packet);
        }
    }

    public static void handleStub(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
        });
    }

    // NEOFORGE_PORT: physical-client-only subscriber for clientbound handlers
    // (RegisterClientPayloadHandlersEvent pattern from NeoForge docs).
    @net.neoforged.fml.common.EventBusSubscriber(modid = Arcaneresearch.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class ClientHandlers {

        @SubscribeEvent
        public static void registerClient(
                net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent event) {
            // PacketNote is bidirectional; its serverbound half is registered above,
            // its clientbound half here. handle() branches on ctx.flow(), so the same
            // method reference serves both sides.
            event.register(
                    arcaneresearch.common.lib.network.misc.PacketNote.TYPE,
                    arcaneresearch.common.lib.network.misc.PacketNote::handle);
        }
    }
}
