package arcaneresearch.common.tiles.devices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
public class TileLampFertility extends TileArcaneresearch implements IEssentiaTransport {
    private static final int RANGE = 7;
    private static final int MAX_CHARGES = 10;
    private static final int MAX_ANIMALS_PER_TYPE = 9;
    private static final int BREED_COST = 5;
    private static final int BREED_INTERVAL = 300;
    public int charges = 0;
    private int count = 0;
    private int drawDelay = 0;
    public TileLampFertility(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public TileLampFertility(BlockPos pos, BlockState state) {
        this(ModBlockEntities.LAMP_FERTILITY.get(), pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileLampFertility tile) {
        if (tile.charges < MAX_CHARGES) {
            if (tile.drawEssentia()) {
                tile.charges++;
                tile.setChanged();
                tile.syncTile(true);
            }
            if (tile.charges <= 1) {
                tile.setEnabled(state, false);
            } else if (!tile.gettingPower()) {
                tile.setEnabled(state, true);
            }
        }
        if (!tile.gettingPower() && tile.charges > 1 && tile.count++ % BREED_INTERVAL == 0) {
            tile.updateAnimals();
        }
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("charges", charges);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        charges = tag.contains("charges") ? tag.getInt("charges") : tag.getInt("Charges");
    }
    private void updateAnimals() {
        if (level == null) return;
        AABB area = new AABB(worldPosition).inflate(RANGE);
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, area);
        if (animals.isEmpty()) return;
        for (Animal animal : animals) {
            if (animal.getAge() != 0 || animal.isInLove()) continue;
            List<Animal> sameType = new ArrayList<>();
            for (Animal other : animals) {
                if (other.getClass().equals(animal.getClass())) {
                    sameType.add(other);
                }
            }
            if (sameType.size() > MAX_ANIMALS_PER_TYPE) continue;
            Animal partner = null;
            for (Animal candidate : sameType) {
                if (candidate == animal) continue;
                if (candidate.getAge() != 0 || candidate.isInLove()) continue;
                if (partner != null) {
                    charges -= BREED_COST;
                    candidate.setInLove(null);
                    partner.setInLove(null);
                    setChanged();
                    syncTile(true);
                    return;
                }
                partner = candidate;
            }
        }
    }
    private boolean drawEssentia() {
        if (++drawDelay % 5 != 0) return false;
        if (level == null) return false;
        Direction facing = getFacing();
        BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, facing);
        if (te instanceof IEssentiaTransport transport) {
            Direction opposite = facing.getOpposite();
            if (!transport.canOutputTo(opposite)) return false;
            if (transport.getSuctionAmount(opposite) < getSuctionAmount(facing) &&
                    transport.takeEssentia(Aspect.DESIRE, 1, opposite) == 1) {
                return true;
            }
        }
        return false;
    }
    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return Direction.DOWN;
    }
    private void setEnabled(BlockState state, boolean enabled) {
        if (level == null) return;
        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            boolean current = state.getValue(BlockStateProperties.ENABLED);
            if (current != enabled) {
                level.setBlock(worldPosition, state.setValue(BlockStateProperties.ENABLED, enabled), 3);
            }
        }
    }
    protected boolean gettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }
    @Override
    public boolean isConnectable(Direction face) {
        return face == getFacing();
    }
    @Override
    public boolean canInputFrom(Direction face) {
        return face == getFacing();
    }
    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }
    @Override
    public void setSuction(Aspect aspect, int amount) {
    }
    @Override
    public Aspect getSuctionType(Direction face) {
        return Aspect.DESIRE;
    }
    @Override
    public int getSuctionAmount(Direction face) {
        if (face == getFacing()) {
            return 128 - charges * 10;
        }
        return 0;
    }
    @Override
    public Aspect getEssentiaType(Direction face) {
        return null;
    }
    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }
    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }
    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return 0;
    }
    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
