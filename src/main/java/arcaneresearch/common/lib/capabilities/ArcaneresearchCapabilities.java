package arcaneresearch.common.lib.capabilities;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// NEOFORGE_PORT: mirrors Forge 1.20.1 common/lib/capabilities/ArcaneresearchCapabilities.java.
// Forge used: CapabilityManager.get(new CapabilityToken<>()) + AttachCapabilitiesEvent<Entity>
//   (PlayerKnowledge.Provider / PlayerWarp.Provider) + LazyOptional + manual NBT copy on Clone.
//
// NeoForge 26.3 REQUIRED changes (storage layer only; helper names/signatures preserved where
// the new API allows, LazyOptional unwrapped to @Nullable):
//   - Capability objects -> AttachmentType objects in ATTACHMENT_TYPES DeferredRegister.
//   - AttachCapabilitiesEvent + Provider classes -> gone (AttachmentType.builder default suppliers).
//   - CapabilityHandler/RegisterCapabilitiesEvent -> gone (no equivalent; see CapabilityHandler stub).
//   - onPlayerClone manual copy -> gone: copyOnDeath() copies on death, and NeoForge also copies
//     entity attachments on return-from-End automatically (both Forge-Clone cases covered).
//   - Respawn/Login/ChangedDimension sync handlers -> preserved 1:1 (packet sync transport).
//   - @Mod.EventBusSubscriber Bus.FORGE -> Bus.GAME: NEITHER exists in NeoForge
//     (@Mod.EventBusSubscriber was removed); game-bus handlers register explicitly
//     via NeoForge.EVENT_BUS.register(...) below.
//   - Respawn/Login/ChangedDimension sync handlers -> preserved 1:1 (packet sync transport).
//
// TODO_PORT (compile-check in 26.3 workspace): none remaining for registration itself —
//   AttachmentType.serializable(ValueIOSerializable) + copyOnDeath() wired against 26.3.0.23-beta.
//   Persistence format mirrors Forge NBT keys (see DefaultImpl.serialize(ValueOutput)).
public class ArcaneresearchCapabilities {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Arcaneresearch.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IPlayerKnowledge>> KNOWLEDGE =
            ATTACHMENT_TYPES.register("knowledge", () -> AttachmentType
                    .<IPlayerKnowledge>serializable(() -> new PlayerKnowledge.DefaultImpl())
                    .copyOnDeath()
                    .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IPlayerWarp>> WARP =
            ATTACHMENT_TYPES.register("warp", () -> AttachmentType
                    .<IPlayerWarp>serializable(() -> new PlayerWarp.DefaultImpl())
                    .copyOnDeath()
                    .build());

    // NEOFORGE_PORT: Forge persisted golem seals in chunk NBT via ChunkDataEvent
    // (CompoundTag "Arcaneresearch"/"seals"). 26.x chunk data is an immutable
    // record, so seals persist in a serializable chunk attachment instead —
    // same content (list of seal CompoundTags), written/read by ChunkEvents.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<arcaneresearch.common.lib.events.ChunkEvents.SealChunkData>> SEAL_DATA =
            ATTACHMENT_TYPES.register("seal_data", () -> AttachmentType
                    .<arcaneresearch.common.lib.events.ChunkEvents.SealChunkData>builder(
                            () -> new arcaneresearch.common.lib.events.ChunkEvents.SealChunkData())
                    .serialize(arcaneresearch.common.lib.events.ChunkEvents.SealChunkData.CODEC)
                    .build());

    // NEOFORGE_PORT: same pattern for aura (base/vis/flux); written/read by AuraChunkHandler.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData>> AURA_DATA =
            ATTACHMENT_TYPES.register("aura_data", () -> AttachmentType
                    .<arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData>builder(
                            () -> new arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData())
                    .serialize(arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData.CODEC)
                    .build());

    // NEOFORGE_PORT: replaces Forge's per-instance empty curative-item lists (milk immunity),
    // see common/lib/potions/IncurableEffects.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<arcaneresearch.common.lib.potions.IncurableEffects.Data>> INCURABLE_EFFECTS =
            ATTACHMENT_TYPES.register("incurable_effects", () -> AttachmentType
                    .<arcaneresearch.common.lib.potions.IncurableEffects.Data>builder(
                            () -> new arcaneresearch.common.lib.potions.IncurableEffects.Data())
                    .serialize(arcaneresearch.common.lib.potions.IncurableEffects.Data.CODEC)
                    .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        arcaneresearch.common.lib.potions.IncurableEffects.register();
        // NEOFORGE_PORT: replaces @Mod.EventBusSubscriber (removed in NeoForge) for the
        // PlayerEvent handlers below.
        NeoForge.EVENT_BUS.register(ArcaneresearchCapabilities.class);
    }

    // Kept for 1:1 diff with Forge (registry names must stay modid:knowledge / modid:warp).
    public static final Identifier KNOWLEDGE_ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "knowledge");
    public static final Identifier WARP_ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "warp");

    @Nullable
    public static IPlayerKnowledge getKnowledge(@Nonnull Player player) {
        return player.getData(KNOWLEDGE.get());
    }

    @Nullable
    public static IPlayerWarp getWarp(@Nonnull Player player) {
        return player.getData(WARP.get());
    }

    public static boolean isResearchKnown(@Nonnull Player player, @Nonnull String research) {
        IPlayerKnowledge knowledge = getKnowledge(player);
        return knowledge != null && knowledge.isResearchKnown(research);
    }

    public static boolean isResearchComplete(@Nonnull Player player, @Nonnull String research) {
        IPlayerKnowledge knowledge = getKnowledge(player);
        return knowledge != null && knowledge.isResearchComplete(research);
    }

    // NEOFORGE_PORT: onAttachCapabilities removed — defaults come from AttachmentType builders above.
    // NEOFORGE_PORT: onPlayerClone manual NBT copy removed — copyOnDeath() + automatic
    //   End-return copy cover both cases (see class javadoc).

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {

            IPlayerKnowledge knowledge = getKnowledge(serverPlayer);
            if (knowledge != null) knowledge.sync(serverPlayer);

            IPlayerWarp warp = getWarp(serverPlayer);
            if (warp != null) warp.sync(serverPlayer);

            // NEOFORGE_PORT (26.3): seal resync on respawn was dropped in the port (Forge did it here too).
            arcaneresearch.common.golems.seals.SealHandler.syncAllSealsToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {

            IPlayerKnowledge knowledge = getKnowledge(serverPlayer);
            if (knowledge != null) knowledge.sync(serverPlayer);

            IPlayerWarp warp = getWarp(serverPlayer);
            if (warp != null) warp.sync(serverPlayer);

            arcaneresearch.common.golems.seals.SealHandler.syncAllSealsToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {

            IPlayerKnowledge knowledge = getKnowledge(serverPlayer);
            if (knowledge != null) knowledge.sync(serverPlayer);

            IPlayerWarp warp = getWarp(serverPlayer);
            if (warp != null) warp.sync(serverPlayer);

            arcaneresearch.common.golems.seals.SealHandler.syncAllSealsToPlayer(serverPlayer);
        }
    }
}
