package arcaneresearch.common.lib.capabilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.common.golems.seals.SealHandler;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArcaneresearchCapabilities {
    public static final Capability<IPlayerKnowledge> KNOWLEDGE = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<IPlayerWarp> WARP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation KNOWLEDGE_ID = new ResourceLocation(Arcaneresearch.MODID, "knowledge");
    public static final ResourceLocation WARP_ID = new ResourceLocation(Arcaneresearch.MODID, "warp");
    public static LazyOptional<IPlayerKnowledge> getKnowledge(Player player) {
        return player.getCapability(KNOWLEDGE);
    }
    public static LazyOptional<IPlayerWarp> getWarp(Player player) {
        return player.getCapability(WARP);
    }
    public static boolean isResearchKnown(Player player, String research) {
        return getKnowledge(player)
                .map(k -> k.isResearchKnown(research))
                .orElse(false);
    }
    public static boolean isResearchComplete(Player player, String research) {
        return getKnowledge(player)
                .map(k -> k.isResearchComplete(research))
                .orElse(false);
    }
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(KNOWLEDGE).isPresent()) {
                event.addCapability(KNOWLEDGE_ID, new PlayerKnowledge.Provider());
            }
            if (!event.getObject().getCapability(WARP).isPresent()) {
                event.addCapability(WARP_ID, new PlayerWarp.Provider());
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(KNOWLEDGE).ifPresent(oldKnowledge -> {
                event.getEntity().getCapability(KNOWLEDGE).ifPresent(newKnowledge -> {
                    newKnowledge.deserializeNBT(oldKnowledge.serializeNBT());
                });
            });
            event.getOriginal().getCapability(WARP).ifPresent(oldWarp -> {
                event.getEntity().getCapability(WARP).ifPresent(newWarp -> {
                    newWarp.deserializeNBT(oldWarp.serializeNBT());
                });
            });
        }
    }
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            getKnowledge(serverPlayer).ifPresent(k -> k.sync(serverPlayer));
            getWarp(serverPlayer).ifPresent(w -> w.sync(serverPlayer));
            SealHandler.syncAllSealsToPlayer(serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            getKnowledge(serverPlayer).ifPresent(k -> k.sync(serverPlayer));
            getWarp(serverPlayer).ifPresent(w -> w.sync(serverPlayer));
            SealHandler.syncAllSealsToPlayer(serverPlayer);
        }
    }
    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            getKnowledge(serverPlayer).ifPresent(k -> k.sync(serverPlayer));
            getWarp(serverPlayer).ifPresent(w -> w.sync(serverPlayer));
            SealHandler.syncAllSealsToPlayer(serverPlayer);
        }
    }
}
