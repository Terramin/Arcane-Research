package arcaneresearch.common.tiles.aura;
import arcaneresearch.common.world.maze.Cell;
import arcaneresearch.common.world.maze.CellLoc;
import arcaneresearch.common.world.maze.MazeFeature;
import arcaneresearch.common.world.maze.MazeHandler;
import arcaneresearch.common.world.maze.TeleporterArcaneresearch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;
import arcaneresearch.common.entities.monster.cult.EntityCultistKnight;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModEntities;
import java.util.List;
public class EldritchAltarTile extends TileArcaneresearch {
    public boolean spawner = false;
    public byte spawnType = 0;
    public byte eyes = 0;
    public boolean open = false;
    private boolean spawnedClerics = false;
    private boolean spawnedGuardian = false;
    private int counter = 0;
    public EldritchAltarTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchAltarTile tile) {
        tile.serverTick();
    }
    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (isSpawner() && this.counter++ >= 80 && this.counter % 40 == 0) {
            if (this.spawnType == 0) {
                if (!this.spawnedClerics) {
                    spawnClerics();
                } else {
                    spawnGuards();
                }
            } else if (this.spawnType == 1) {
                spawnGuardian();
            }
        }
    }
    private void spawnGuards() {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB box = new AABB(this.worldPosition).inflate(24.0D, 16.0D, 24.0D);
        List<EntityCultistCleric> clerics = level.getEntitiesOfClass(EntityCultistCleric.class, box);
        if (clerics.size() < 1) {
            setSpawner(false);
            return;
        }
        List<EntityCultist> cultists = level.getEntitiesOfClass(EntityCultist.class, box);
        if (cultists.size() >= 8) {
            return;
        }
        EntityCultistKnight knight = new EntityCultistKnight(ModEntities.CULTIST_KNIGHT.get(), serverLevel);
        int x = this.worldPosition.getX() + level.random.nextIntBetweenInclusive(4, 10)
                * level.random.nextIntBetweenInclusive(-1, 1);
        int y = this.worldPosition.getY() + level.random.nextIntBetweenInclusive(0, 3)
                * level.random.nextIntBetweenInclusive(-1, 1);
        int z = this.worldPosition.getZ() + level.random.nextIntBetweenInclusive(4, 10)
                * level.random.nextIntBetweenInclusive(-1, 1);
        BlockPos at = new BlockPos(x, y, z);
        if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
            return;
        }
        knight.moveTo(x + 0.5D, y, z + 0.5D);
        if (!level.noCollision(knight)
                || !serverLevel.getEntities(knight, knight.getBoundingBox(),
                        e -> e != knight && !e.isSpectator() && e.canBeCollidedWith()).isEmpty()) {
            return;
        }
        knight.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(at),
                MobSpawnType.EVENT, null, null);
        serverLevel.addFreshEntity(knight);
    }
    private void spawnGuardian() {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.spawnedGuardian) {
            return;
        }
        AABB box = new AABB(this.worldPosition).inflate(24.0D, 16.0D, 24.0D);
        List<EntityEldritchGuardian> guardians =
                level.getEntitiesOfClass(EntityEldritchGuardian.class, box);
        if (!guardians.isEmpty()) {
            return;
        }
        EntityEldritchGuardian guardian =
                new EntityEldritchGuardian(ModEntities.ELDRITCH_GUARDIAN.get(), serverLevel);
        int x = this.worldPosition.getX() + level.random.nextIntBetweenInclusive(4, 10)
                * (level.random.nextBoolean() ? 1 : -1);
        int y = this.worldPosition.getY() + level.random.nextIntBetweenInclusive(0, 3)
                * (level.random.nextBoolean() ? 1 : -1);
        int z = this.worldPosition.getZ() + level.random.nextIntBetweenInclusive(4, 10)
                * (level.random.nextBoolean() ? 1 : -1);
        BlockPos at = new BlockPos(x, y, z);
        if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
            return;
        }
        guardian.moveTo(x + 0.5D, y, z + 0.5D);
        if (!level.noCollision(guardian)
                || !serverLevel.getEntities(guardian, guardian.getBoundingBox(),
                        e -> e != guardian && !e.isSpectator() && e.canBeCollidedWith()).isEmpty()) {
            return;
        }
        guardian.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(at),
                MobSpawnType.EVENT, null, null);
        if (serverLevel.addFreshEntity(guardian)) {
            this.spawnedGuardian = true;
            setChanged();
        }
    }
    private void spawnClerics() {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int success = 0;
        int[][] corners = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}};
        for (int[] corner : corners) {
            EntityCultistCleric cleric =
                    new EntityCultistCleric(ModEntities.CULTIST_CLERIC.get(), serverLevel);
            BlockPos at = this.worldPosition.offset(corner[0], 0, corner[1]);
            if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
                continue;
            }
            cleric.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D);
            if (!level.noCollision(cleric)
                    || !serverLevel.getEntities(cleric, cleric.getBoundingBox(),
                            e -> e != cleric && !e.isSpectator() && e.canBeCollidedWith()).isEmpty()) {
                continue;
            }
            cleric.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(at),
                    MobSpawnType.EVENT, null, null);
            if (serverLevel.addFreshEntity(cleric)) {
                success++;
                cleric.setRitualist(true);
            }
        }
        if (success > 2) {
            this.spawnedClerics = true;
            setChanged();
        }
    }
    public boolean isSpawner() {
        return this.spawner;
    }
    public void setSpawner(boolean spawner) {
        this.spawner = spawner;
        setChanged();
    }
    public byte getSpawnType() {
        return this.spawnType;
    }
    public void setSpawnType(byte spawnType) {
        this.spawnType = spawnType;
        setChanged();
    }
    public byte getEyes() {
        return this.eyes;
    }
    public void setEyes(byte eyes) {
        this.eyes = eyes;
        setChanged();
    }
    public boolean isOpen() {
        return this.open;
    }
    public void setOpen(boolean open) {
        this.open = open;
        setChanged();
    }
    public boolean checkForMaze() {
        Level level = getLevel();
        if (level == null || level.isClientSide || !(level instanceof ServerLevel server)) {
            return false;
        }
        arcaneresearch.common.world.maze.MazeFeature.ensureMaze(server);
        arcaneresearch.common.world.maze.Cell arrival = arcaneresearch.common.world.maze.MazeHandler.get(
                new arcaneresearch.common.world.maze.CellLoc(
                        arcaneresearch.common.world.maze.TeleporterArcaneresearch.ARRIVAL_CHUNK_X,
                        arcaneresearch.common.world.maze.TeleporterArcaneresearch.ARRIVAL_CHUNK_Z));
        return arrival != null && arrival.feature == 1;
    }
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(),
                this.worldPosition.getX() + 1, this.worldPosition.getY() + 1,
                this.worldPosition.getZ() + 1);
    }
    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        writeAltar(tag);
    }
    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        readAltar(tag);
    }
    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeAltar(tag);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readAltar(tag);
    }
    protected void writeAltar(CompoundTag tag) {
        tag.putBoolean("spawner", this.spawner);
        tag.putByte("spawntype", this.spawnType);
        tag.putBoolean("spawnedClerics", this.spawnedClerics);
        tag.putBoolean("spawnedGuardian", this.spawnedGuardian);
        tag.putByte("eyes", this.eyes);
        tag.putBoolean("open", this.open);
    }
    protected void readAltar(CompoundTag tag) {
        this.spawner = tag.getBoolean("spawner");
        this.spawnType = tag.getByte("spawntype");
        this.spawnedClerics = tag.getBoolean("spawnedClerics");
        this.spawnedGuardian = tag.getBoolean("spawnedGuardian");
        this.eyes = tag.getByte("eyes");
        this.open = tag.getBoolean("open");
    }
}
