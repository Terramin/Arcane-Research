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
import net.minecraft.world.entity.EntitySpawnReason; // NEOFORGE_PORT: was EntitySpawnReason
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Eldritch altar tile, ported from the 1.8.9 TileEldritchAltar.
 * Spawns clerics once, then knights while clerics watch, or a guardian
 * (spawnType 1). The original maze gate has no counterpart in this port
 * and is skipped; eyes/open state is kept for the renderer.
 */
public class EldritchAltarTile extends TileArcaneresearch {
    public boolean spawner = false;
    public byte spawnType = 0;
    public byte eyes = 0;
    public boolean open = false;

    private boolean spawnedClerics = false;
    /** Страж портала — один на алтарь (TC4): без флага новый лез каждые 40 тиков. */
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
        if (level == null || level.isClientSide()) {
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
        List<EntityCultistCleric> clerics = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityCultistCleric.class), box, e -> true);
        if (clerics.size() < 1) {
            setSpawner(false);
            return;
        }
        List<EntityCultist> cultists = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityCultist.class), box, e -> true);
        if (cultists.size() >= 8) {
            return;
        }
        EntityCultistKnight knight = new EntityCultistKnight(ModEntities.CULTIST_KNIGHT.get(), serverLevel);
        // TC4: MathHelper.func_76136_a(rand, 4, 10) * func_76136_a(rand, -1, 1)
        // — множитель может быть и 0.
        int x = this.worldPosition.getX() + level.getRandom().nextIntBetweenInclusive(4, 10)
                * level.getRandom().nextIntBetweenInclusive(-1, 1);
        int y = this.worldPosition.getY() + level.getRandom().nextIntBetweenInclusive(0, 3)
                * level.getRandom().nextIntBetweenInclusive(-1, 1);
        int z = this.worldPosition.getZ() + level.getRandom().nextIntBetweenInclusive(4, 10)
                * level.getRandom().nextIntBetweenInclusive(-1, 1);
        BlockPos at = new BlockPos(x, y, z);
        if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
            return;
        }
        knight.teleportTo(x + 0.5D, y, z + 0.5D);
        // TC4: checkNoEntityCollision + getEntitiesWithinAABB(...).isEmpty().
        if (!level.noCollision(knight)
                || !serverLevel.getEntities(knight, knight.getBoundingBox(),
                        e -> e != knight && !e.isSpectator() && !e.isRemoved()).isEmpty()) {
            return;
        }
        knight.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
        serverLevel.addFreshEntity(knight);
    }

    private void spawnGuardian() {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        // Один страж на алтарь: иначе после убийства каждые 40 тиков лез
        // новый и открытый портал «постоянно призывает».
        if (this.spawnedGuardian) {
            return;
        }
        // Кап: пока страж жив в радиусе — не дублируем.
        AABB box = new AABB(this.worldPosition).inflate(24.0D, 16.0D, 24.0D);
        List<EntityEldritchGuardian> guardians =
                level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(EntityEldritchGuardian.class), box, e -> true);
        if (!guardians.isEmpty()) {
            return;
        }
        EntityEldritchGuardian guardian =
                new EntityEldritchGuardian(ModEntities.ELDRITCH_GUARDIAN.get(), serverLevel);
        int x = this.worldPosition.getX() + level.getRandom().nextIntBetweenInclusive(4, 10)
                * level.getRandom().nextIntBetweenInclusive(-1, 1);
        int y = this.worldPosition.getY() + level.getRandom().nextIntBetweenInclusive(0, 3)
                * level.getRandom().nextIntBetweenInclusive(-1, 1);
        int z = this.worldPosition.getZ() + level.getRandom().nextIntBetweenInclusive(4, 10)
                * level.getRandom().nextIntBetweenInclusive(-1, 1);
        BlockPos at = new BlockPos(x, y, z);
        if (!level.getBlockState(at.below()).isFaceSturdy(level, at.below(), Direction.UP)) {
            return;
        }
        guardian.teleportTo(x + 0.5D, y, z + 0.5D);
        // TC4: eg.func_70601_bi() (getCanSpawnHere) — сущности в месте тоже
        // проверяются (в оригинале — часть вызова).
        if (!level.noCollision(guardian)
                || !serverLevel.getEntities(guardian, guardian.getBoundingBox(),
                        e -> e != guardian && !e.isSpectator() && !e.isRemoved()).isEmpty()) {
            return;
        }
        guardian.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
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
            cleric.teleportTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D);
            if (!level.noCollision(cleric)
                    || !serverLevel.getEntities(cleric, cleric.getBoundingBox(),
                            e -> e != cleric && !e.isSpectator() && !e.isRemoved()).isEmpty()) {
                continue;
            }
            cleric.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
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

    /**
     * Порт TC4 TileEldritchAltar.checkForMaze (смысл, не центровка):
     * гарантирует готовый главный лабиринт и наличие портал-комнаты
     * в точке прибытия. Свою раскладку вокруг алтаря НЕ кладёт —
     * она бы перезаписала карту и развалила коридоры (комнаты строятся
     * по карте, старые чанки не перестраиваются).
     */
    public boolean checkForMaze() {
        Level level = getLevel();
        if (level == null || level.isClientSide() || !(level instanceof ServerLevel server)) {
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
        // 1:1 TC4 (глаза внутри блока).
        return new AABB(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(),
                this.worldPosition.getX() + 1, this.worldPosition.getY() + 1,
                this.worldPosition.getZ() + 1);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        writeAltar(tag);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        readAltar(tag);
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        writeAltar(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        readAltar(tag);
    }

    protected void writeAltar(ValueOutput tag) {
        tag.putBoolean("spawner", this.spawner);
        tag.putByte("spawntype", this.spawnType);
        tag.putBoolean("spawnedClerics", this.spawnedClerics);
        tag.putBoolean("spawnedGuardian", this.spawnedGuardian);
        tag.putByte("eyes", this.eyes);
        tag.putBoolean("open", this.open);
    }

    protected void readAltar(net.minecraft.world.level.storage.ValueInput tag) {
        this.spawner = tag.getBooleanOr("spawner", false);
        this.spawnType = tag.getByteOr("spawntype", (byte) 0);
        this.spawnedClerics = tag.getBooleanOr("spawnedClerics", false);
        this.spawnedGuardian = tag.getBooleanOr("spawnedGuardian", false);
        this.eyes = tag.getByteOr("eyes", (byte) 0);
        this.open = tag.getBooleanOr("open", false);
    }
}
