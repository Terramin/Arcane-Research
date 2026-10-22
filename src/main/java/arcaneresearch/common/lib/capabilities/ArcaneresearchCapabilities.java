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

    
    
    
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<arcaneresearch.common.lib.events.ChunkEvents.SealChunkData>> SEAL_DATA =
            ATTACHMENT_TYPES.register("seal_data", () -> AttachmentType
                    .<arcaneresearch.common.lib.events.ChunkEvents.SealChunkData>builder(
                            () -> new arcaneresearch.common.lib.events.ChunkEvents.SealChunkData())
                    .serialize(arcaneresearch.common.lib.events.ChunkEvents.SealChunkData.CODEC)
                    .build());

    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData>> AURA_DATA =
            ATTACHMENT_TYPES.register("aura_data", () -> AttachmentType
                    .<arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData>builder(
                            () -> new arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData())
                    .serialize(arcaneresearch.common.world.aura.AuraChunkHandler.AuraChunkData.CODEC)
                    .build());

    
    
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<arcaneresearch.common.lib.potions.IncurableEffects.Data>> INCURABLE_EFFECTS =
            ATTACHMENT_TYPES.register("incurable_effects", () -> AttachmentType
                    .<arcaneresearch.common.lib.potions.IncurableEffects.Data>builder(
                            () -> new arcaneresearch.common.lib.potions.IncurableEffects.Data())
                    .serialize(arcaneresearch.common.lib.potions.IncurableEffects.Data.CODEC)
                    .build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        arcaneresearch.common.lib.potions.IncurableEffects.register();
        
        
        NeoForge.EVENT_BUS.register(ArcaneresearchCapabilities.class);
    }

    
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

    
    
    

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {

            IPlayerKnowledge knowledge = getKnowledge(serverPlayer);
            if (knowledge != null) knowledge.sync(serverPlayer);

            IPlayerWarp warp = getWarp(serverPlayer);
            if (warp != null) warp.sync(serverPlayer);

            
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
