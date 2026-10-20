package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWandable;
import arcaneresearch.common.blocks.aura.BlockAuraNode;
import arcaneresearch.common.items.resources.ItemWispEssence;
import arcaneresearch.common.lib.aura.*;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class AuraNodeTile extends TileArcaneresearch implements INode, IWandable {
    protected AspectList aspects = new AspectList();
    protected AspectList aspectsBase = new AspectList();
    int count = 0;
    int regeneration = -1;
    int wait = 0;
    String id = null;
    byte nodeLock = 0;
    long lastActive = 0L;
    boolean catchUp = false;
    private NodeType nodeType = NodeType.NORMAL;
    private NodeModifier nodeModifier = null;
    private transient net.minecraft.world.entity.Entity drainTarget;
    private transient Aspect drainAspect;
    private transient long lastTapGameTime = -100L;

    private int drainEntityId = -1;
    private String drainAspectTag = "";
    private long drainTime = -100L;

    private transient long drainSeenTick = -100L;
    private transient BlockPos hungryFlowFrom;
    private transient long hungryFlowTime = -100L;

    public AuraNodeTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AuraNodeTile tile) {
        tile.count++;
        boolean change = false;
        change = tile.handleHungryNodeFirst(level, change);

        tile.checkLock();
        change = tile.handleDischarge(level, change);
        change = tile.handleRecharge(level, change);
        change = tile.handleTaintNode(level, change);
        change = tile.handleNodeStability(level, change);

        change = tile.handleHungryNodeSecond(level, change);
        if (change) {
            tile.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        if (tile.wait > 0) {
            tile.wait--;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AuraNodeTile tile) {
        tile.count++;

        if (tile.getNodeType() == NodeType.HUNGRY && level.getRandom().nextInt(6) == 0) {
            try {
                net.minecraft.world.phys.Vec3 v1 = new net.minecraft.world.phys.Vec3(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
                int tx = pos.getX() + level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
                int ty = pos.getY() + level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
                int tz = pos.getZ() + level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
                ty = Math.min(ty, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types
                        .MOTION_BLOCKING, tx, tz));
                net.minecraft.world.phys.Vec3 v2 = new net.minecraft.world.phys.Vec3(
                        tx + 0.5D, ty + 0.5D, tz + 0.5D);
                net.minecraft.world.phys.BlockHitResult hit = level.clip(
                        new net.minecraft.world.level.ClipContext(v1, v2,
                                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                                net.minecraft.world.level.ClipContext.Fluid.ANY,
                                // NEOFORGE_PORT (26.3): CollisionContext.of(null) NPEs (type switch);
                                // 1.20.1 mapped a null entity to CollisionContext.empty().
                                net.minecraft.world.phys.shapes.CollisionContext.empty()));
                if (hit != null
                        && hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                        && hit.getBlockPos().distSqr(pos) < 256.0D
                        && !level.getBlockState(hit.getBlockPos()).isAir()) {
                    tile.rememberHungryFlow(hit.getBlockPos());
                }
            } catch (Exception ignored) {
            }
        }

    }

    public void rememberDrain(net.minecraft.world.entity.Entity target, Aspect aspect) {
        this.drainTarget = target;
        this.drainAspect = aspect;
        if (level != null) {
            this.lastTapGameTime = level.getLevelData().getGameTime();
        }

        if (level != null && !level.isClientSide() && target != null) {
            this.drainEntityId = target.getId();
            this.drainAspectTag = aspect != null ? aspect.getTag() : "";
            this.drainTime = level.getLevelData().getGameTime();
            syncTile(false);
        }
    }

    public void clearDrain() {
        this.drainTarget = null;
        this.drainAspect = null;
        if (level != null && !level.isClientSide()) {
            this.drainEntityId = -1;
            this.drainAspectTag = "";
            syncTile(false);
        }
    }

    public void rememberHungryFlow(BlockPos from) {
        this.hungryFlowFrom = from;
        if (level != null) {
            this.hungryFlowTime = level.getLevelData().getGameTime();
        }
    }

    public BlockPos getHungryFlowFrom() {
        return this.hungryFlowFrom;
    }

    public long getHungryFlowTime() {
        return this.hungryFlowTime;
    }

    public net.minecraft.world.entity.Entity getDrainTarget() {

        if (this.drainTarget == null && this.drainEntityId >= 0 && level != null
                && level.isClientSide()) {
            try {
                net.minecraft.world.entity.Entity e =
                        level.getEntity(this.drainEntityId);
                if (e != null) {
                    return e;
                }
            } catch (Exception ignored) {
            }
        }
        return this.drainTarget;
    }

    public Aspect getDrainAspect() {
        if (this.drainAspect == null && this.drainAspectTag != null
                && !this.drainAspectTag.isEmpty()) {
            Aspect a = Aspect.getAspect(this.drainAspectTag);
            if (a != null) {
                return a;
            }
        }
        return this.drainAspect;
    }

    public long getLastTapGameTime() {
        if (level != null && level.isClientSide()) {
            return Math.max(this.lastTapGameTime, this.drainSeenTick);
        }
        return this.lastTapGameTime;
    }

    public String getId() {
        if (this.id == null) {
            generateId();
        }
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String generateId() {
        Level level = getLevel();
        String dim = level != null ? level.dimension().identifier().toString() : "minecraft:overworld";
        this.id = dim + ":" + getBlockPos().getX() + ":" + getBlockPos().getY() + ":" + getBlockPos().getZ();
        return this.id;
    }

    public byte getLock() {
        return this.nodeLock;
    }

    public void checkLock() {
        try {
            if (level == null || worldPosition.getY() <= level.getMinY()) {
                return;
            }
            byte oldLock = this.nodeLock;
            this.nodeLock = 0;
            // Порт TC4 TileNode: lock держит только незапитанный стабилизатор.
            BlockPos belowPos = worldPosition.below();
            if (level.getBlockEntity(belowPos) instanceof StabilizerTile stab
                    && !level.hasNeighborSignal(belowPos)) {
                this.nodeLock = (byte) Math.max(0, Math.min(2, stab.getLock()));
            }
            if (oldLock != this.nodeLock) {
                this.regeneration = -1;
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public AspectList getAspects() {
        return this.aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
        this.aspects = aspects;
        this.aspectsBase = aspects.copy();
    }

    @Override
    public AspectList getAspectsBase() {
        return this.aspectsBase;
    }

    /**
     * Паритет 1.7.10 BlockAiry.func_149681_a (breakBlock, мета 0): при
     * разрушении ноды для каждого её аспекта выбрасываются мудровые
     * эссенции — каждый стак несёт 2 эссенции, число спавнов = amount/10 + 1
     * при amount &gt;= 5. Новое состояние «не нода» ⇒ дроп; креатив — тоже
     * дроп, как в оригинале.
     */
    // NEOFORGE_PORT: moved here from BlockAuraNode.onRemove (Block.onRemove removed
    // in 26.x). LevelChunk.setBlockState calls preRemoveSideEffects with the tile
    // still present, so getAspects() is valid.
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState newState) {
        // NEOFORGE_PORT (26.3): LevelChunk passes the OLD state here and only calls this
        // when the block changed, so the former "!newState.is(own block)" guard was always
        // false and the drop never ran. Reference ran it from BlockAuraNode.onRemove.
        if (level != null && !level.isClientSide() && getBlockState().getBlock() instanceof BlockAuraNode) {
            AspectList al = getAspects();
            if (al != null && al.size() > 0) {
                for (Aspect aspect : al.getAspects()) {
                    int amount = al.getAmount(aspect);
                    if (amount >= 5) {
                        // паритет: кладём столько же спавнов, сколько стак
                        // итераций давал цикл a = 0..amount/10
                        int stacks = amount / 10 + 1;
                        for (int a = 0; a < stacks; a++) {
                            ItemStack ess = ItemWispEssence.createStack(
                                    ModItems.WISP_ESSENCE.get(), aspect, 1);
                            ItemEntity entity = new ItemEntity(level,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ess);
                            level.addFreshEntity(entity);
                        }
                    }
                }
            }
        }
        super.preRemoveSideEffects(pos, newState);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        int left = amount + this.aspects.getAmount(aspect) - this.aspectsBase.getAmount(aspect);
        left = Math.max(left, 0);
        this.aspects.add(aspect, amount - left);
        setChanged();
        return left;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return this.aspects.reduce(aspect, amount);
    }

    @Override
    @Deprecated
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    @Deprecated
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    public Aspect takeRandomPrimalFromSource() {
        ArrayList<Aspect> primals = Aspect.getPrimalAspects();
        Aspect asp = primals.get(level.getRandom().nextInt(primals.size()));
        if (asp != null && this.aspects.reduce(asp, 1)) {
            return asp;
        }
        return null;
    }

    @Override
    public NodeType getNodeType() {
        return this.nodeType;
    }

    @Override
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public NodeModifier getNodeModifier() {
        return this.nodeModifier;
    }

    @Override
    public void setNodeModifier(NodeModifier nodeModifier) {
        this.nodeModifier = nodeModifier;
    }

    @Override
    public int getNodeVisBase(Aspect aspect) {
        return this.aspectsBase.getAmount(aspect);
    }

    @Override
    public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
        if (this.aspectsBase.getAmount(aspect) < nodeVisBase) {
            this.aspectsBase.merge(aspect, nodeVisBase);
        } else {
            this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);
        }
    }

    public void nodeChange() {
        this.regeneration = -1;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private boolean handleHungryNodeFirst(Level level, boolean change) {
        if (getNodeType() != NodeType.HUNGRY) {
            return change;
        }

        java.util.List<Entity> ents = level.getEntities((Entity) null,
                new AABB(worldPosition).inflate(15.0D, 15.0D, 15.0D));
        if (ents != null && !ents.isEmpty()) {
            for (Entity eo : ents) {
                if (eo instanceof Player p && p.getAbilities().invulnerable) {
                    continue;
                }
                if (eo.isAlive() && !eo.isInvulnerable()) {
                    double d = eo.distanceToSqr(worldPosition.getX() + 0.5D,
                            worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D);
                    if (d < 4.0D) {
                        eo.hurt(level.damageSources().magic(), 1.0F);
                        if (!eo.isAlive() && !level.isClientSide()) {

                            ArrayList<Aspect> primals = Aspect.getPrimalAspects();
                            Aspect a = primals.get(level.getRandom().nextInt(primals.size()));
                            if (getAspects().getAmount(a) < getNodeVisBase(a)) {
                                addToContainer(a, 1);
                                change = true;
                            } else if (level.getRandom().nextInt(1 + getNodeVisBase(a) * 2) < 1) {
                                this.aspectsBase.add(a, 1);
                                change = true;
                            }
                        }
                    }
                    double var3 = (worldPosition.getX() + 0.5D - eo.getX()) / 15.0D;
                    double var5 = (worldPosition.getY() + 0.5D - eo.getY()) / 15.0D;
                    double var7 = (worldPosition.getZ() + 0.5D - eo.getZ()) / 15.0D;
                    double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
                    double var11 = 1.0D - var9;
                    if (var11 > 0.0D && var9 > 1.0E-4D) {
                        var11 *= var11;
                        Vec3 motion = eo.getDeltaMovement();
                        eo.setDeltaMovement(motion.x + var3 / var9 * var11 * 0.15D,
                                motion.y + var5 / var9 * var11 * 0.25D,
                                motion.z + var7 / var9 * var11 * 0.15D);
                    }
                }
            }
        }
        return change;
    }

    private boolean handleDischarge(Level level, boolean change) {
        if (getNodeModifier() == NodeModifier.FADING) {
            return change;
        }
        // Паритет TC4 TileNode.handleDischarge: lock 1 не задирает соседей
        // вообще (== 1 дословно — у продвинутого поведение то же, что в TC4).
        if (getLock() == 1) {
            return change;
        }
        boolean shiny = (getNodeType() == NodeType.HUNGRY || getNodeModifier() == NodeModifier.BRIGHT);
        int inc = (getNodeModifier() == null) ? 2 : (shiny ? 1 : (getNodeModifier() == NodeModifier.PALE ? 3 : 2));
        if (this.count % inc != 0) {
            return change;
        }
        if (getNodeModifier() == NodeModifier.PALE && level.getRandom().nextBoolean()) {
            return change;
        }
        int x = level.getRandom().nextInt(5) - level.getRandom().nextInt(5);
        int y = level.getRandom().nextInt(5) - level.getRandom().nextInt(5);
        int z = level.getRandom().nextInt(5) - level.getRandom().nextInt(5);
        if (x != 0 || y != 0 || z != 0) {
            BlockPos np = worldPosition.offset(x, y, z);
            if (level.getBlockEntity(np) instanceof AuraNodeTile other) {
                // Паритет TC4: у ноды под стабилизатором не отнять.
                if (other.getLock() > 0) {
                    return change;
                }
                int ndavg = (other.getAspects().size() + other.getAspectsBase().size()) / 2;
                int thisavg = (getAspects().size() + getAspectsBase().size()) / 2;
                if (ndavg < thisavg && other.getAspects().size() > 0) {
                    Aspect[] all = other.getAspects().getAspects();
                    Aspect a = all[level.getRandom().nextInt(all.length)];
                    boolean u = false;
                    if (getAspects().getAmount(a) < getNodeVisBase(a) && other.takeFromContainer(a, 1)) {
                        addToContainer(a, 1);
                        u = true;
                    } else if (other.takeFromContainer(a, 1)) {
                        if (level.getRandom().nextInt(1 + (int) (getNodeVisBase(a) / (shiny ? 1.5D : 1.0D))) == 0) {
                            this.aspectsBase.add(a, 1);
                            if (getNodeModifier() == NodeModifier.PALE && level.getRandom().nextInt(100) == 0) {
                                setNodeModifier(null);
                                this.regeneration = -1;
                            }
                            if (level.getRandom().nextInt(3) == 0) {
                                other.setNodeVisBase(a, (short) (other.getNodeVisBase(a) - 1));
                            }
                        }
                        u = true;
                    }
                    if (u) {
                        other.wait = other.regeneration / 2;
                        other.setChanged();
                        AuraFX.nodeSparkle(level, np, a.getColor());
                        // Паритет TC4 TileNode.handleDischarge: фрактальная дуга
                        // от жертвы к ноде (там PacketFXBlockZap + nodeBolt).
                        // NEOFORGE_PORT: Forge TargetPoint(x, y, z, range, dim) ->
                        // sendToNear(packet, level, x, y, z, range).
                        if (!level.isClientSide()) {
                            arcaneresearch.common.lib.network.PacketHandler.sendToNear(
                                    new arcaneresearch.common.lib.network.fx.PacketFXNodeBolt(
                                            np.getX() + 0.5D, np.getY() + 0.5D, np.getZ() + 0.5D,
                                            worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                                            worldPosition.getZ() + 0.5D,
                                            level.getRandom().nextLong()),
                                    level,
                                    np.getX() + 0.5D, np.getY() + 0.5D, np.getZ() + 0.5D, 32.0D);
                            level.playSound(null, np.getX() + 0.5D, np.getY() + 0.5D, np.getZ() + 0.5D,
                                    arcaneresearch.common.lib.wands.WandSounds.zap(),
                                    net.minecraft.sounds.SoundSource.BLOCKS, 0.1F,
                                    1.0F + level.getRandom().nextFloat() * 0.2F);
                        }
                        change = true;
                    }
                }
            }
        }
        return change;
    }

    private boolean handleRecharge(Level level, boolean change) {
        if (this.regeneration < 0) {
            this.regeneration = 600;
            if (getNodeModifier() != null) {
                switch (getNodeModifier()) {
                    case BRIGHT -> this.regeneration = 400;
                    case PALE -> this.regeneration = 900;
                    case FADING -> this.regeneration = 0;
                }
            }
            if (getLock() == 1) {
                this.regeneration *= 2;
            }
            if (getLock() == 2) {
                this.regeneration *= 20;
            }
        }
        if (this.catchUp) {
            this.catchUp = false;
            long ct = System.currentTimeMillis();
            int inc = this.regeneration * 75;
            int amt = (inc > 0) ? (int) ((ct - this.lastActive) / inc) : 0;
            if (amt > 0) {
                for (int a = 0; a < Math.min(amt, this.aspectsBase.size()); a++) {
                    AspectList al = new AspectList();
                    for (Aspect aspect : getAspects().getAspects()) {
                        if (getAspects().getAmount(aspect) < getNodeVisBase(aspect)) {
                            al.add(aspect, 1);
                        }
                    }
                    if (al.size() > 0) {
                        Aspect[] all = al.getAspects();
                        addToContainer(all[level.getRandom().nextInt(all.length)], 1);
                    }
                }
            }
        }
        if (this.count % 1200 == 0) {
            for (Aspect aspect : getAspects().getAspects().clone()) {
                if (getAspects().getAmount(aspect) <= 0) {
                    setNodeVisBase(aspect, (short) (getNodeVisBase(aspect) - 1));
                    if (level.getRandom().nextInt(20) == 0 || getNodeVisBase(aspect) <= 0) {
                        getAspects().remove(aspect, getAspects().getAmount(aspect));
                        if (level.getRandom().nextInt(5) == 0) {
                            if (getNodeModifier() == NodeModifier.BRIGHT) {
                                setNodeModifier(null);
                            } else if (getNodeModifier() == null) {
                                setNodeModifier(NodeModifier.PALE);
                            }
                            if (getNodeModifier() == NodeModifier.PALE && level.getRandom().nextInt(5) == 0) {
                                setNodeModifier(NodeModifier.FADING);
                            }
                        }
                    }
                    nodeChange();
                    break;
                }
            }
            if (getAspects().size() <= 0) {
                removeThisNode(level);
            }
        }
        if (this.wait > 0) {
            this.wait--;
        }
        if (this.regeneration > 0 && this.wait == 0 && this.count % this.regeneration == 0) {
            this.lastActive = System.currentTimeMillis();
            AspectList al = new AspectList();
            for (Aspect aspect : getAspects().getAspects()) {
                if (getAspects().getAmount(aspect) < getNodeVisBase(aspect)) {
                    al.add(aspect, 1);
                }
            }
            if (al.size() > 0) {
                Aspect[] all = al.getAspects();
                addToContainer(all[level.getRandom().nextInt(all.length)], 1);
                change = true;
            }
        }
        return change;
    }

    private void removeThisNode(Level level) {
        if (level.getBlockState(worldPosition).getBlock() instanceof BlockAuraNode) {
            level.removeBlock(worldPosition, false);
        } else {
            level.removeBlockEntity(worldPosition);
        }
    }

    private boolean handleTaintNode(Level level, boolean change) {

        if (getNodeType() == NodeType.TAINTED && this.count % 50 == 0) {
            arcaneresearch.common.world.aura.AuraHandler.addFlux(level, worldPosition, 1.0F);
        }
        return change;
    }

    private boolean handleNodeStability(Level level, boolean change) {
        if (this.count % 100 == 0) {
            if (getNodeType() == NodeType.UNSTABLE && level.getRandom().nextBoolean()) {
                if (getLock() == 0) {
                    Aspect aspect = takeRandomPrimalFromSource();
                    if (aspect != null) {

                        ItemStack crystal = ArcaneresearchApiHelper.makeCrystal(aspect);
                        if (crystal != null && !crystal.isEmpty() && level instanceof ServerLevel server) {
                            ItemEntity drop = new ItemEntity(server,
                                    worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                                    worldPosition.getZ() + 0.5D, crystal);
                            server.addFreshEntity(drop);
                            change = true;
                        }
                    }
                } else if (level.getRandom().nextInt(10000 / Math.max(getLock(), 1)) == 42) {
                    setNodeType(NodeType.NORMAL);
                    change = true;
                }
            }
            if (getNodeModifier() == NodeModifier.FADING && getLock() > 0
                    && level.getRandom().nextInt(12500 / Math.max(getLock(), 1)) == 69) {
                setNodeModifier(NodeModifier.PALE);
                change = true;
            }
        }
        return change;
    }

    private boolean handleHungryNodeSecond(Level level, boolean change) {
        if (getNodeType() == NodeType.HUNGRY && this.count % 50 == 0) {
            int tx = worldPosition.getX() + level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
            int ty = worldPosition.getY() + level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
            int tz = worldPosition.getZ() + level.getRandom().nextInt(16) - level.getRandom().nextInt(16);
            int top = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    new BlockPos(tx, 0, tz)).getY();
            if (ty > top) {
                ty = top;
            }
            Vec3 v1 = new Vec3(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D);
            Vec3 v2 = new Vec3(tx + 0.5D, ty + 0.5D, tz + 0.5D);
            BlockHitResult mop = level.clip(new ClipContext(v1, v2,
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                    // NEOFORGE_PORT (26.3): CollisionContext.of(null) NPEs (type switch) and crashed
                    // the hungry-node server tick; 1.20.1 mapped a null entity to CollisionContext.empty().
                    net.minecraft.world.phys.shapes.CollisionContext.empty()));
            if (mop != null && mop.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                    && mop.getBlockPos().distSqr(worldPosition) < 65536.0D) {
                tx = mop.getBlockPos().getX();
                ty = mop.getBlockPos().getY();
                tz = mop.getBlockPos().getZ();
                BlockState bi = level.getBlockState(mop.getBlockPos());
                if (!bi.isAir()) {
                    float h = bi.getDestroySpeed(level, mop.getBlockPos());
                    if (h >= 0.0F && h < 5.0F) {
                        level.destroyBlock(mop.getBlockPos(), true);
                    }
                }
            }
        }
        return change;
    }

    @Override
    public boolean onWandRightClick(Level level, ItemStack wandstack, Player player, BlockPos pos,
                                    net.minecraft.core.Direction facing) {
        return false;
    }

    @Override
    public void onUsingWandTick(ItemStack wandstack, Player player, int count) {

        Aspect drained = NodeTapHelper.tap(level, worldPosition, getAspects(),
                (aspect, amount) -> takeFromContainer(aspect, amount),
                this::markDirtyAndSync, wandstack, player, count);
        if (drained != null) {
            rememberDrain(player, drained);
        }
    }

    @Override
    public void onWandStoppedUsing(ItemStack wandstack, Level level, Player player, int count) {
        clearDrain();
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putString("nodeId", getId());
        tag.putByte("type", (byte) getNodeType().ordinal());
        tag.putByte("modifier", getNodeModifier() == null ? (byte) -1 : (byte) getNodeModifier().ordinal());
        tag.putLong("lastActive", this.lastActive);
        this.aspects.writeToNBT(tag);
        java.util.List<CompoundTag> baseList = new java.util.ArrayList<>();
        for (Aspect aspect : this.aspectsBase.getAspects()) {
            if (aspect != null) {
                CompoundTag f = new CompoundTag();
                f.putString("key", aspect.getTag());
                f.putInt("amount", this.aspectsBase.getAmount(aspect));
                baseList.add(f);
            }
        }
        writeTagList(tag, "AspectsBase", baseList);

        tag.putInt("drainId", this.drainEntityId);
        tag.putString("drainAspect", this.drainAspectTag != null ? this.drainAspectTag : "");
        tag.putLong("drainTime", this.drainTime);
    }

    @Override
    public void loadAdditional(ValueInput tag) {

        super.loadAdditional(tag);
        if (level == null || !level.isClientSide()) {
            this.drainEntityId = -1;
            this.drainAspectTag = "";
            this.drainTime = -100L;
        }
        updateCatchUp();
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        // NEOFORGE_PORT: Forge body; CompoundTag getters -> ValueInput Or-getters,
        // AspectsBase list via readTagList helper (same key/shape as writeSyncNBT).
        super.readSyncNBT(tag);
        long prevDrain = this.drainTime;
        try {
            loadNodeData(tag);
            this.lastActive = tag.getLongOr("lastActive", 0L);
            AspectList al = new AspectList();
            for (CompoundTag rs : readTagList(tag, "AspectsBase")) {
                if (rs.contains("key")) {
                    Aspect a = Aspect.getAspect(rs.getStringOr("key", ""));
                    if (a != null) {
                        al.add(a, rs.getIntOr("amount", 0));
                    }
                }
            }
            // NEOFORGE_PORT: ValueInput has no getShortOr; read via Codec.SHORT (cf. TileAlembic).
            short oldBase = tag.read("nodeVisBase", com.mojang.serialization.Codec.SHORT).orElse((short) 0);
            this.aspectsBase = new AspectList();
            if (oldBase > 0 && al.size() == 0) {
                for (Aspect a : this.aspects.getAspects()) {
                    this.aspectsBase.merge(a, oldBase);
                }
            } else {
                this.aspectsBase = al.copy();
            }
            this.drainEntityId = tag.getIntOr("drainId", -1);
            this.drainAspectTag = tag.getStringOr("drainAspect", "");
            this.drainTime = tag.getLongOr("drainTime", -100L);

            if (level != null && level.isClientSide()) {
                long incoming = this.drainTime;
                if (incoming > prevDrain && incoming > 0L) {
                    this.drainSeenTick = level.getLevelData().getGameTime();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void updateCatchUp() {
        int regen = 600;
        if (getNodeModifier() != null) {
            switch (getNodeModifier()) {
                case BRIGHT -> regen = 400;
                case PALE -> regen = 900;
                case FADING -> regen = 0;
            }
        }
        long ct = System.currentTimeMillis();
        int inc = regen * 75;
        if (regen > 0 && this.lastActive > 0L && ct > this.lastActive + inc) {
            this.catchUp = true;
        }
    }

    public void loadNodeData(ValueInput tag) {
        this.id = tag.getStringOr("nodeId", "");
        setNodeType(NodeType.values()[tag.getByteOr("type", (byte) 0) % NodeType.values().length]);
        byte mod = tag.getByteOr("modifier", (byte) 0);
        setNodeModifier(mod >= 0 && mod < NodeModifier.values().length ? NodeModifier.values()[mod] : null);
        this.aspects.readFromNBT(tag);
    }

    public void saveNodeData(ValueOutput tag) {
        tag.putString("nodeId", getId());
        tag.putByte("type", (byte) getNodeType().ordinal());
        tag.putByte("modifier", getNodeModifier() == null ? (byte) -1 : (byte) getNodeModifier().ordinal());
        this.aspects.writeToNBT(tag);
    }
}
