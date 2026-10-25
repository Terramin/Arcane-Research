package arcaneresearch.common.lib.events;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXBlockBamf;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.common.world.aura.AuraThreadManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    public static final Predicate<SwapperPredicate> DEFAULT_PREDICATE = pred -> true;
    private static final Map<String, Integer> serverTicks = new HashMap<>();
    private static final Map<String, LinkedBlockingQueue<RunnableEntry>> serverRunList = new ConcurrentHashMap<>();
    private static final LinkedBlockingQueue<RunnableEntry> clientRunList = new LinkedBlockingQueue<>();
    private static final Map<String, LinkedBlockingQueue<VirtualSwapper>> swapList = new ConcurrentHashMap<>();
    private static final Map<String, LinkedBlockingQueue<BreakData>> breakList = new ConcurrentHashMap<>();
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.CLIENT) {
            return;
        }
        if (!(event.level instanceof ServerLevel level)) {
            return;
        }
        String dimKey = level.dimension().location().toString();
        if (event.phase == TickEvent.Phase.START) {
            if (!AuraThreadManager.hasThread(level.dimension()) && AuraHandler.getAuraWorld(level.dimension()) != null) {
                AuraThreadManager.startThread(level.dimension());
            }
        } else {
            if (!serverTicks.containsKey(dimKey)) {
                serverTicks.put(dimKey, 0);
            }
            processRunnables(dimKey);
            tickBlockSwap(level);
            tickBlockBreak(level);
            ArrayList<WorldEvents.NoteBlockData> nbe = WorldEvents.noteBlockEvents.get(dimKey);
            if (nbe != null) {
                nbe.clear();
            }
            int ticks = serverTicks.get(dimKey);
            if (ticks % 20 == 0) {
                TaskHandler.clearSuspendedOrExpiredTasks(level);
                ResourceKey<Level> dimension = level.dimension();
                CopyOnWriteArrayList<ChunkPos> dirtyChunks = AuraHandler.dirtyChunks.get(dimension);
                if (dirtyChunks != null && !dirtyChunks.isEmpty()) {
                    for (ChunkPos pos : dirtyChunks) {
                        ChunkAccess access = level.getChunkSource().getChunk(pos.x, pos.z, false);
                        if (access instanceof LevelChunk levelChunk) {
                            levelChunk.setUnsaved(true);
                        }
                    }
                    dirtyChunks.clear();
                }
                if (AuraHandler.riftTrigger.containsKey(dimension)) {
                    if (!ModConfig.wussMode) {
                        BlockPos riftPos = AuraHandler.riftTrigger.get(dimension);
                        EntityFluxRift.createRift(level, riftPos);
                    }
                    AuraHandler.riftTrigger.remove(dimension);
                }
            }
            SealHandler.tickSealEntities(level);
            serverTicks.put(dimKey, ticks + 1);
        }
    }
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!clientRunList.isEmpty()) {
            LinkedBlockingQueue<RunnableEntry> temp = new LinkedBlockingQueue<>();
            while (!clientRunList.isEmpty()) {
                RunnableEntry entry = clientRunList.poll();
                if (entry != null) {
                    if (entry.delay > 0) {
                        entry.delay--;
                        temp.offer(entry);
                    } else {
                        try {
                            entry.runnable.run();
                        } catch (Exception e) {
                            Arcaneresearch.LOGGER.warn("Error executing delayed client runnable", e);
                        }
                    }
                }
            }
            clientRunList.addAll(temp);
        }
    }
    private static void processRunnables(String dimKey) {
        LinkedBlockingQueue<RunnableEntry> rlist = serverRunList.get(dimKey);
        if (rlist == null) {
            serverRunList.put(dimKey, new LinkedBlockingQueue<>());
            return;
        }
        if (!rlist.isEmpty()) {
            LinkedBlockingQueue<RunnableEntry> temp = new LinkedBlockingQueue<>();
            while (!rlist.isEmpty()) {
                RunnableEntry entry = rlist.poll();
                if (entry != null) {
                    if (entry.delay > 0) {
                        entry.delay--;
                        temp.offer(entry);
                    } else {
                        try {
                            entry.runnable.run();
                        } catch (Exception e) {
                            Arcaneresearch.LOGGER.warn("Error executing delayed server runnable", e);
                        }
                    }
                }
            }
            rlist.addAll(temp);
        }
    }
    private static void tickBlockSwap(ServerLevel level) {
        String dimKey = level.dimension().location().toString();
        LinkedBlockingQueue<VirtualSwapper> queue = swapList.get(dimKey);
        if (queue == null || queue.isEmpty()) return;
        LinkedBlockingQueue<VirtualSwapper> nextQueue = new LinkedBlockingQueue<>();
        while (!queue.isEmpty()) {
            VirtualSwapper vs = queue.poll();
            if (vs == null) continue;
            BlockState currentState = level.getBlockState(vs.pos);
            boolean allow = currentState.getDestroySpeed(level, vs.pos) >= 0.0f;
            if (vs.source != null && !vs.source.equals(currentState)) {
                allow = false;
            }
            if (vs.visCost > 0.0f && AuraHelper.getVis(level, vs.pos) < vs.visCost) {
                allow = false;
            }
            if (vs.player != null && !level.mayInteract(vs.player, vs.pos)) {
                continue;
            }
            if (!allow || !vs.allowSwap.test(new SwapperPredicate(level, vs.player, vs.pos))) {
                continue;
            }
            if (vs.targetState != null) {
                if (currentState.equals(vs.targetState)) {
                    continue;
                }
            } else if (vs.target != null && !vs.target.isEmpty()) {
                Block targetBlock = Block.byItem(vs.target.getItem());
                if (targetBlock != Blocks.AIR && currentState.is(targetBlock)) {
                    continue;
                }
            }
            int slot = -1;
            if (!vs.consumeTarget || vs.target == null || vs.target.isEmpty()) {
                slot = 1;
            } else if (vs.player != null) {
                slot = findItemSlot(vs.player, vs.target);
                if (vs.player.isCreative()) {
                    slot = 1;
                }
            }
            if (slot < 0) continue;
            if (vs.player != null && !vs.player.isCreative()) {
                if (vs.consumeTarget && slot >= 0) {
                    vs.player.getInventory().removeItem(slot, 1);
                }
                if (vs.pickup) {
                    java.util.List<ItemStack> pickupDrops = new java.util.ArrayList<>();
                    if (vs.silk) {
                        ItemStack silkDrop = arcaneresearch.common.lib.utils.BlockUtils
                                .getSilkTouchDrop(currentState, level, vs.pos);
                        if (silkDrop != null && !silkDrop.isEmpty()) {
                            pickupDrops.add(silkDrop);
                        }
                    } else {
                        ItemStack tool = ItemStack.EMPTY;
                        if (vs.fortune > 0) {
                            tool = new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
                            tool.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE,
                                    Math.min(vs.fortune, 4));
                        }
                        pickupDrops = Block.getDrops(currentState, level, vs.pos,
                                level.getBlockEntity(vs.pos), vs.player, tool);
                    }
                    for (ItemStack drop : pickupDrops) {
                        if (!drop.isEmpty()) {
                            if (!vs.player.getInventory().add(drop)) {
                                level.addFreshEntity(new ItemEntity(level,
                                        vs.pos.getX() + 0.5, vs.pos.getY() + 0.5, vs.pos.getZ() + 0.5, drop));
                            }
                        }
                    }
                }
                if (vs.visCost > 0.0f) {
                    AuraHelper.drainVis(level, vs.pos, vs.visCost, false);
                }
            }
            if (vs.target == null || vs.target.isEmpty()) {
                level.removeBlock(vs.pos, false);
            } else {
                BlockState placeState = vs.targetState;
                if (placeState == null) {
                    Block targetBlock = Block.byItem(vs.target.getItem());
                    if (targetBlock != null && targetBlock != Blocks.AIR) {
                        placeState = targetBlock.defaultBlockState();
                    }
                }
                if (placeState != null) {
                    level.setBlock(vs.pos, placeState, 3);
                } else {
                    level.removeBlock(vs.pos, false);
                    arcaneresearch.common.entities.EntitySpecialItem entityItem =
                            new arcaneresearch.common.entities.EntitySpecialItem(level,
                                    vs.pos.getX() + 0.5, vs.pos.getY() + 0.1, vs.pos.getZ() + 0.5,
                                    vs.target.copy());
                    entityItem.setDeltaMovement(0, 0, 0);
                    level.addFreshEntity(entityItem);
                }
            }
            if (vs.fx) {
                PacketHandler.sendToAllTrackingChunk(
                        new PacketFXBlockBamf(vs.pos, vs.color, true, vs.fancy, null),
                        level, vs.pos
                );
            }
            if (vs.lifespan > 0 && vs.source != null) {
                for (int xx = -1; xx <= 1; xx++) {
                    for (int yy = -1; yy <= 1; yy++) {
                        for (int zz = -1; zz <= 1; zz++) {
                            if (xx == 0 && yy == 0 && zz == 0) continue;
                            BlockPos adjacent = vs.pos.offset(xx, yy, zz);
                            BlockState adjacentState = level.getBlockState(adjacent);
                            if (adjacentState.equals(vs.source) && isBlockExposed(level, adjacent)) {
                                nextQueue.offer(new VirtualSwapper(
                                        adjacent, vs.source, vs.target, vs.targetState, vs.consumeTarget,
                                        vs.lifespan - 1, vs.player, vs.fx, vs.fancy,
                                        vs.color, vs.pickup, vs.silk, vs.fortune,
                                        vs.allowSwap, vs.visCost
                                ));
                            }
                        }
                    }
                }
            }
        }
        if (!nextQueue.isEmpty()) {
            swapList.put(dimKey, nextQueue);
        }
    }
    private static void tickBlockBreak(ServerLevel level) {
        String dimKey = level.dimension().location().toString();
        LinkedBlockingQueue<BreakData> queue = breakList.get(dimKey);
        if (queue == null || queue.isEmpty()) return;
        LinkedBlockingQueue<BreakData> nextQueue = new LinkedBlockingQueue<>();
        while (!queue.isEmpty()) {
            BreakData bd = queue.poll();
            if (bd == null) continue;
            BlockState currentState = level.getBlockState(bd.pos);
            if (bd.visCost > 0.0f && AuraHelper.getVis(level, bd.pos) < bd.visCost) {
                continue;
            }
            if (!currentState.equals(bd.source)) {
                if (bd.fx) {
                    level.destroyBlockProgress(bd.pos.hashCode(), bd.pos, -1);
                }
                continue;
            }
            if (bd.player != null && !level.mayInteract(bd.player, bd.pos)) {
                continue;
            }
            if (currentState.getDestroySpeed(level, bd.pos) < 0.0f) {
                continue;
            }
            if (bd.fx) {
                int progress = (int) ((1.0f - bd.durabilityCurrent / bd.durabilityMax) * 10.0f);
                level.destroyBlockProgress(bd.pos.hashCode(), bd.pos, progress);
            }
            bd.durabilityCurrent -= bd.strength;
            if (bd.durabilityCurrent <= 0.0f) {
                level.destroyBlock(bd.pos, true, bd.player);
                if (bd.fx) {
                    level.destroyBlockProgress(bd.pos.hashCode(), bd.pos, -1);
                }
                if (bd.visCost > 0.0f) {
                    AuraHelper.drainVis(level, bd.pos, bd.visCost, false);
                }
            } else {
                nextQueue.offer(bd);
            }
        }
        if (!nextQueue.isEmpty()) {
            breakList.put(dimKey, nextQueue);
        }
    }
    private static boolean isBlockExposed(Level level, BlockPos pos) {
        return level.getBlockState(pos.above()).isAir() ||
                level.getBlockState(pos.below()).isAir() ||
                level.getBlockState(pos.north()).isAir() ||
                level.getBlockState(pos.south()).isAir() ||
                level.getBlockState(pos.east()).isAir() ||
                level.getBlockState(pos.west()).isAir();
    }
    private static int findItemSlot(Player player, ItemStack target) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(stack, target)) {
                return i;
            }
        }
        return -1;
    }
    public static void addSwapper(Level level, BlockPos pos, BlockState source, ItemStack target,
                                  boolean consumeTarget, int life, Player player, boolean fx, boolean fancy,
                                  int color, boolean pickup, boolean silk, int fortune,
                                  Predicate<SwapperPredicate> allowSwap, float visCost) {
        addSwapper(level, pos, source, target, null, consumeTarget, life, player, fx, fancy,
                color, pickup, silk, fortune, allowSwap, visCost);
    }
    public static void addSwapper(Level level, BlockPos pos, BlockState source, ItemStack target,
                                  BlockState targetState, boolean consumeTarget, int life, Player player, boolean fx, boolean fancy,
                                  int color, boolean pickup, boolean silk, int fortune,
                                  Predicate<SwapperPredicate> allowSwap, float visCost) {
        String dimKey = level.dimension().location().toString();
        LinkedBlockingQueue<VirtualSwapper> queue = swapList.computeIfAbsent(dimKey, k -> new LinkedBlockingQueue<>());
        queue.offer(new VirtualSwapper(pos, source, target, targetState, consumeTarget, life, player,
                fx, fancy, color, pickup, silk, fortune, allowSwap, visCost));
    }
    public static void addBreaker(Level level, BlockPos pos, BlockState source, Player player,
                                  boolean fx, boolean silk, int fortune, float strength,
                                  float durabilityCurrent, float durabilityMax, int delay, float visCost, Runnable onComplete) {
        String dimKey = level.dimension().location().toString();
        if (delay > 0) {
            addRunnableServer(level, () -> addBreaker(level, pos, source, player, fx, silk, fortune,
                    strength, durabilityCurrent, durabilityMax, 0, visCost, onComplete), delay);
        } else {
            LinkedBlockingQueue<BreakData> queue = breakList.computeIfAbsent(dimKey, k -> new LinkedBlockingQueue<>());
            queue.offer(new BreakData(strength, durabilityCurrent, durabilityMax, pos, source,
                    player, fx, silk, fortune, visCost));
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    public static void addRunnableServer(Level level, Runnable runnable, int delay) {
        if (level.isClientSide()) return;
        String dimKey = level.dimension().location().toString();
        LinkedBlockingQueue<RunnableEntry> rlist = serverRunList.computeIfAbsent(dimKey, k -> new LinkedBlockingQueue<>());
        rlist.add(new RunnableEntry(runnable, delay));
    }
    public static void addRunnableClient(Level level, Runnable runnable, int delay) {
        if (!level.isClientSide()) return;
        clientRunList.add(new RunnableEntry(runnable, delay));
    }
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraThreadManager.onLevelLoad(level);
        }
    }
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AuraThreadManager.onLevelUnload(level);
        }
    }
    @SubscribeEvent
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        AuraThreadManager.stopAllThreads();
        AuraHandler.dirtyChunks.clear();
        AuraHandler.riftTrigger.clear();
        serverTicks.clear();
        serverRunList.clear();
        swapList.clear();
        breakList.clear();
        Arcaneresearch.LOGGER.info("Arcaneresearch server events cleaned up");
    }
    public static class RunnableEntry {
        Runnable runnable;
        int delay;
        public RunnableEntry(Runnable runnable, int delay) {
            this.runnable = runnable;
            this.delay = delay;
        }
    }
    public static class SwapperPredicate {
        public Level level;
        public Player player;
        public BlockPos pos;
        public SwapperPredicate(Level level, Player player, BlockPos pos) {
            this.level = level;
            this.player = player;
            this.pos = pos;
        }
    }
    public static class VirtualSwapper {
        BlockPos pos;
        BlockState source;
        ItemStack target;
        BlockState targetState;
        boolean consumeTarget;
        int lifespan;
        Player player;
        boolean fx;
        boolean fancy;
        int color;
        boolean pickup;
        boolean silk;
        int fortune;
        Predicate<SwapperPredicate> allowSwap;
        float visCost;
        VirtualSwapper(BlockPos pos, BlockState source, ItemStack target, boolean consumeTarget,
                       int lifespan, Player player, boolean fx, boolean fancy, int color,
                       boolean pickup, boolean silk, int fortune,
                       Predicate<SwapperPredicate> allowSwap, float visCost) {
            this(pos, source, target, null, consumeTarget, lifespan, player, fx, fancy, color,
                    pickup, silk, fortune, allowSwap, visCost);
        }
        VirtualSwapper(BlockPos pos, BlockState source, ItemStack target, BlockState targetState,
                       boolean consumeTarget,
                       int lifespan, Player player, boolean fx, boolean fancy, int color,
                       boolean pickup, boolean silk, int fortune,
                       Predicate<SwapperPredicate> allowSwap, float visCost) {
            this.pos = pos;
            this.source = source;
            this.target = target;
            this.targetState = targetState;
            this.consumeTarget = consumeTarget;
            this.lifespan = lifespan;
            this.player = player;
            this.fx = fx;
            this.fancy = fancy;
            this.color = color;
            this.pickup = pickup;
            this.silk = silk;
            this.fortune = fortune;
            this.allowSwap = allowSwap;
            this.visCost = visCost;
        }
    }
    public static class BreakData {
        float strength;
        float durabilityCurrent;
        float durabilityMax;
        BlockPos pos;
        BlockState source;
        Player player;
        boolean fx;
        boolean silk;
        int fortune;
        float visCost;
        public BreakData(float strength, float durabilityCurrent, float durabilityMax,
                         BlockPos pos, BlockState source, Player player,
                         boolean fx, boolean silk, int fortune, float visCost) {
            this.strength = strength;
            this.durabilityCurrent = durabilityCurrent;
            this.durabilityMax = durabilityMax;
            this.pos = pos;
            this.source = source;
            this.player = player;
            this.fx = fx;
            this.silk = silk;
            this.fortune = fortune;
            this.visCost = visCost;
        }
    }
}
