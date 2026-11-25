package arcaneresearch.common.wards;

import arcaneresearch.Arcaneresearch;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Ward bookkeeping and protection. Warded blocks cannot be broken in survival;
 * creative breaking drops the ward. No block is ever replaced.
 */
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WardEvents {
    private WardEvents() {
    }

    /** Breaking cannot even start in survival; creative drops the ward. */
    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)
                || !WardManager.isWarded(serverLevel, event.getPos())) {
            return;
        }
        if (!event.getPlayer().isCreative()) {
            event.setCanceled(true);
            return;
        }
        WardStorage storage = WardManager.get(serverLevel);
        if (storage.forceRemove(event.getPos())) {
            WardManager.syncRemove(serverLevel, event.getPos(), null);
        }
    }

    /** Pistons cannot move a warded block (TC4 mobility flag). */
    @SubscribeEvent
    public static void onPiston(net.minecraftforge.event.level.PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        WardStorage storage = WardManager.get(serverLevel);
        if (storage.isWarded(event.getPos()) || storage.isWarded(event.getFaceOffsetPos())) {
            event.setCanceled(true);
            return;
        }
        net.minecraft.world.level.block.piston.PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver != null) {
            for (net.minecraft.core.BlockPos pushed : resolver.getToPush()) {
                if (storage.isWarded(pushed)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    /** Mobs (wither/dragon/zombie) cannot destroy a warded block by direct AI. */
    @SubscribeEvent
    public static void onEntityDestroy(net.minecraftforge.event.entity.living.LivingDestroyBlockEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel
                && WardManager.isWarded(serverLevel, event.getPos())) {
            event.setCanceled(true);
        }
    }

    /** Keep warded blocks out of explosion drops/effects. */
    @SubscribeEvent
    public static void onExplosion(net.minecraftforge.event.level.ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        WardStorage storage = WardManager.get(serverLevel);
        if (!storage.entries().isEmpty()) {
            event.getAffectedBlocks().removeIf(storage::isWarded);
        }
    }

    /** Non-owners cannot use a warded chest or other interactive block. */
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        WardEntry entry = WardManager.getEntry(serverLevel, event.getPos());
        if (entry != null && !entry.isOwnedBy(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            WardManager.syncFull(level, player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            WardManager.syncFull(level, player);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)
                || level.getGameTime() % 20L != 0L) {
            return;
        }
        WardStorage storage = WardManager.get(level);
        // Only drop wards whose block is actually gone. Unloaded chunks keep
        // their wards (they were not removed by anyone).
        java.util.List<net.minecraft.core.BlockPos> stale = new java.util.ArrayList<>();
        for (net.minecraft.core.BlockPos pos : storage.entries().keySet()) {
            if (level.isLoaded(pos) && level.getBlockState(pos).isAir()) {
                stale.add(pos);
            }
        }
        for (net.minecraft.core.BlockPos pos : stale) {
            WardEntry entry = storage.get(pos);
            if (storage.forceRemove(pos)) {
                WardManager.syncRemove(level, pos, entry);
            }
        }
    }
}
