package arcaneresearch.common.tiles.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.blocks.devices.BlockCondenserLattice;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class TileCondenser extends TileArcaneresearch implements IEssentiaTransport {
    private static final int MAX_ESSENTIA = 100;
    private static final int MAX_FLUX = 100;
    private static final int MAX_LATTICE_COUNT = 40;
    private static final int BASE_INTERVAL = 600;
    private static final int MIN_INTERVAL = 5;
    public float latticeCount = -1.0f;
    public int interval = 0;
    public int cost = 0;
    private int essentia = 0;
    private int flux = 0;
    private int count = 0;
    private Set<Long> blockList = new HashSet<>();
    private List<Long> uncloggedList = new ArrayList<>();
    public TileCondenser(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileCondenser(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CONDENSER.get(), pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileCondenser tile) {
        if (tile.latticeCount < 0) {
            tile.triggerCheck();
        }
        tile.count++;
        if (!tile.isEnabled(state) || tile.latticeCount <= 0) return;
        if (tile.count % 5 == 0 && tile.essentia < MAX_ESSENTIA) {
            tile.fillEssentia();
        }
        if (tile.interval > 0 &&
                tile.essentia >= tile.cost &&
                tile.flux < MAX_FLUX &&
                tile.count % tile.interval == 0) {
            float auraFlux = AuraHelper.getFlux(level, pos);
            if (auraFlux >= 1.0f) {
                AuraHelper.drainFlux(level, pos, 1.0f, false);
                tile.essentia -= tile.cost;
                tile.flux++;
                if (level.random.nextInt(50) == 0) {
                    tile.makeLatticeDirty();
                }
                tile.syncTile(false);
                tile.setChanged();
            }
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileCondenser tile) {
        if (tile.latticeCount < 0) {
            tile.triggerCheck();
        }
        if (tile.isEnabled(state) && tile.latticeCount > 0 && tile.essentia > 0 && !tile.uncloggedList.isEmpty()
                && tile.count % Math.max(3, tile.interval / 50) == 0) {
            BlockPos p = BlockPos.of(tile.uncloggedList.get(level.random.nextInt(tile.uncloggedList.size())));
            if (p != null) {
                FXDispatcher.INSTANCE.spark(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                        4.5f + level.random.nextFloat(),
                        0.33f + level.random.nextFloat() * 0.66f, 0.33f + level.random.nextFloat() * 0.66f,
                        0.33f + level.random.nextFloat() * 0.66f, 0.8f);
            }
        }
        tile.count++;
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putShort("essentia", (short) essentia);
        tag.putShort("flux", (short) flux);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        essentia = tag.getShort("essentia");
        flux = tag.getShort("flux");
    }
    private boolean isEnabled(BlockState state) {
        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
        }
        return true;
    }
    private void fillEssentia() {
        if (level == null) return;
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, face);
            if (te instanceof IEssentiaTransport transport) {
                Direction opposite = face.getOpposite();
                if (!transport.canOutputTo(opposite)) return;
                if (transport.getEssentiaAmount(opposite) > 0 &&
                        transport.getSuctionAmount(opposite) < getSuctionAmount(face) &&
                        getSuctionAmount(face) >= transport.getMinimumSuction()) {
                    Aspect type = transport.getEssentiaType(opposite);
                    if (type != null && type != Aspect.FLUX) {
                        int taken = transport.takeEssentia(type, 1, opposite);
                        essentia += taken;
                        if (essentia >= MAX_ESSENTIA) {
                            syncTile(false);
                            setChanged();
                            break;
                        }
                    } else if (type == Aspect.FLUX) {
                        makeLatticeDirty();
                    }
                    if (type != null) {
                        syncTile(false);
                        setChanged();
                    }
                }
            }
        }
    }
    private void makeLatticeDirty() {
        if (!uncloggedList.isEmpty() && level != null) {
            int q = level.random.nextInt(uncloggedList.size());
            if (q == 0) {
                q = level.random.nextInt(uncloggedList.size());
            }
            BlockPos p = BlockPos.of(uncloggedList.get(q));
            if (p != null && level.getBlockState(p).is(ModBlocks.CONDENSER_LATTICE.get())) {
                level.setBlock(p, ModBlocks.CONDENSER_LATTICE_DIRTY.get().defaultBlockState(), 3);
                BlockState dirtyState = level.getBlockState(p);
                if (dirtyState.getBlock() instanceof BlockCondenserLattice dirty) {
                    dirty.triggerUpdate(level, p);
                }
            }
        }
    }
    public void triggerCheck() {
        blockList.clear();
        uncloggedList.clear();
        latticeCount = 0;
        interval = 0;
        Set<Long> visited = new HashSet<>();
        performCheck(worldPosition, true, false, visited);
        if (latticeCount <= 0) {
            latticeCount = 0;
        } else {
            if (latticeCount > MAX_LATTICE_COUNT) {
                latticeCount = MAX_LATTICE_COUNT;
            }
            interval = Math.round(BASE_INTERVAL - latticeCount * 15.0f);
            if (interval < MIN_INTERVAL) {
                interval = MIN_INTERVAL;
            }
            cost = (int) (4.0 + Math.sqrt(blockList.size()));
        }
    }
    private void performCheck(BlockPos pos, boolean skip, boolean clogged, Set<Long> visited) {
        if (latticeCount < 0 || level == null) return;
        visited.add(pos.asLong());
        boolean found = false;
        int sides = 0;
        for (Direction face : Direction.values()) {
            if (skip && face != Direction.UP) continue;
            BlockPos checkPos = pos.relative(face);
            BlockState state = level.getBlockState(checkPos);
            boolean isLattice = state.is(ModBlocks.CONDENSER_LATTICE.get());
            boolean isDirtyLattice = state.is(ModBlocks.CONDENSER_LATTICE_DIRTY.get());
            if (skip && isDirtyLattice) {
                clogged = true;
            }
            if (isLattice || isDirtyLattice) {
                sides++;
            }
            if (!visited.contains(checkPos.asLong())) {
                if (face == Direction.DOWN) {
                    if (state.is(ModBlocks.CONDENSER.get())) {
                        latticeCount = -99;
                        return;
                    }
                }
                if (worldPosition.getY() < checkPos.getY()) {
                    double distSq = worldPosition.distSqr(checkPos);
                    if (distSq <= 74) {
                        if (isLattice || isDirtyLattice) {
                            blockList.add(checkPos.asLong());
                            if (isLattice) {
                                uncloggedList.add(checkPos.asLong());
                            }
                            found = true;
                            performCheck(checkPos, false, clogged || isDirtyLattice, visited);
                            if (skip) break;
                        }
                    }
                }
            }
        }
        if (found && !clogged) {
            float bonus = 1.0f - 0.15f * sides;
            latticeCount += bonus;
        }
    }
    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.UP;
    }
    @Override
    public boolean canInputFrom(Direction face) {
        return face != Direction.UP && face != Direction.DOWN;
    }
    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.DOWN;
    }
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }
    @Override
    public Aspect getSuctionType(Direction face) {
        return null;
    }
    @Override
    public int getSuctionAmount(Direction face) {
        if (face == Direction.DOWN || essentia >= MAX_ESSENTIA) {
            return 0;
        }
        return 128;
    }
    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        if (!canOutputTo(face)) return 0;
        if (aspect != null && aspect != Aspect.FLUX) return 0;
        int taken = Math.min(amount, flux);
        if (taken > 0) {
            flux -= taken;
            syncTile(false);
            setChanged();
        }
        return taken;
    }
    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        int added = canInputFrom(face) ? Math.min(amount, MAX_ESSENTIA - essentia) : 0;
        if (added > 0) {
            syncTile(false);
            setChanged();
        }
        return added;
    }
    @Override
    public Aspect getEssentiaType(Direction face) {
        return Aspect.FLUX;
    }
    @Override
    public int getEssentiaAmount(Direction face) {
        return flux;
    }
    @Override
    public int getMinimumSuction() {
        return 0;
    }
    public int getEssentia() {
        return essentia;
    }
    public int getFlux() {
        return flux;
    }
    public float getLatticeCount() {
        return latticeCount;
    }
}
