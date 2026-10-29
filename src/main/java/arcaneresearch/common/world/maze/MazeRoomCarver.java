package arcaneresearch.common.world.maze;

import arcaneresearch.common.blocks.aura.EldritchCrabBlock;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;
import arcaneresearch.common.tiles.aura.EldritchCrabTile;
import arcaneresearch.common.world.structures.StructureHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.core.registries.BuiltInRegistries;

public final class MazeRoomCarver {
    public static final String WALL = "arcaneresearch:eldritch_stone_tile";
    public static final String FLOOR = "arcaneresearch:ancient_stone";
    public static final String PILLAR = "arcaneresearch:eldritch_pillar";

    private static final BlockState AIR_STATE = Blocks.AIR.defaultBlockState();
    private static final BlockState CHEST_STATE = Blocks.CHEST.defaultBlockState();
    private static final BlockState SPAWNER_STATE = Blocks.SPAWNER.defaultBlockState();

    private static final Identifier RL_WALL = rl(WALL);
    private static final Identifier RL_FLOOR = rl(FLOOR);
    private static final Identifier RL_CRAB = rl("arcaneresearch:eldritch_crab");
    private static final Identifier RL_TRAP = rl("arcaneresearch:eldritch_trap");
    private static final Identifier RL_ALTAR = rl("arcaneresearch:eldritch_altar");
    private static final Identifier RL_STAIRS = rl("arcaneresearch:eldritch_stairs");

    private static Identifier rl(String id) {
        try {
            return Identifier.tryParse(id);
        } catch (Exception e) {
            return null;
        }
    }

    private static final class Cache {
        static final Block WALL_BLOCK = lookup(RL_WALL);
        static final Block FLOOR_BLOCK = lookup(RL_FLOOR);
        static final Block CRAB_BLOCK = lookup(RL_CRAB);
        static final Block TRAP_BLOCK = lookup(RL_TRAP);
        static final Block ALTAR_BLOCK = lookup(RL_ALTAR);
        static final Block STAIRS_BLOCK = lookup(RL_STAIRS);

        static final BlockState[] CRYSTALS = {
                stateOf("arcaneresearch:crystal_air"),
                stateOf("arcaneresearch:crystal_fire"),
                stateOf("arcaneresearch:crystal_water"),
                stateOf("arcaneresearch:crystal_earth"),
                stateOf("arcaneresearch:crystal_order"),
                stateOf("arcaneresearch:crystal_entropy")
        };
        static final BlockState URN_COMMON = stateOf("arcaneresearch:loot_urn_common");
        static final BlockState URN_UNCOMMON = stateOf("arcaneresearch:loot_urn_uncommon");
        static final BlockState URN_RARE = stateOf("arcaneresearch:loot_urn_rare");
        static final BlockState ALTAR_STATE = stateOf("arcaneresearch:eldritch_altar");
        static final BlockState PILLAR_STATE = stateOf(PILLAR);
        static final BlockState OBELISK_STATE = stateOf("arcaneresearch:eldritch_obelisk");
        static final BlockState OBELISK_BODY = stateOf("arcaneresearch:eldritch_obelisk_body");
        static final BlockState TRAP_STATE = stateOf("arcaneresearch:eldritch_trap");
        static final BlockState CRAB_STATE = stateOf("arcaneresearch:eldritch_crab");

        static Block lookup(Identifier rl) {
            return rl == null ? null : BuiltInRegistries.BLOCK.getValue(rl);
        }

        static BlockState stateOf(String id) {
            return StructureHelper.state(id);
        }

        private Cache() {}
    }

    private MazeRoomCarver() {
    }

    public static void fill(LevelAccessor level, int x1, int y1, int z1,
                            int x2, int y2, int z2, BlockState state) {
        if (level == null || state == null) {
            return;
        }
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int yMin = Math.min(y1, y2);
        int yMax = Math.max(y1, y2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);
        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    StructureHelper.set(level, new BlockPos(x, y, z), state);
                }
            }
        }
    }

    public static void air(LevelAccessor level, int x1, int y1, int z1, int x2, int y2, int z2) {
        fill(level, x1, y1, z1, x2, y2, z2, AIR_STATE);
    }

    public static void hollowRoom(LevelAccessor level, BlockPos floorCenter, int w, int h,
                                  BlockState wall, BlockState floor) {
        if (level == null || floorCenter == null) {
            return;
        }
        int x = floorCenter.getX();
        int y = floorCenter.getY();
        int z = floorCenter.getZ();
        int hw = w / 2;
        fill(level, x - hw, y, z - hw, x + hw, y, z + hw, floor);
        fill(level, x - hw, y + 1, z - hw, x + hw, y + h, z + hw, wall);
        air(level, x - hw + 1, y + 1, z - hw + 1, x + hw - 1, y + h - 1, z + hw - 1);
        fill(level, x - hw, y + h + 1, z - hw, x + hw, y + h + 1, z + hw, wall);
    }

    public static void corridor(LevelAccessor level, BlockPos from, int dx, int dz, int length,
                                BlockState wall, BlockState floor) {
        if (level == null || from == null) {
            return;
        }
        int x = from.getX();
        int y = from.getY();
        int z = from.getZ();
        for (int i = 0; i < length; i++) {
            int cx = x + dx * i;
            int cz = z + dz * i;
            fill(level, cx - 1, y, cz - 1, cx + 1, y, cz + 1, floor);
            fill(level, cx - 1, y + 1, cz - 1, cx + 1, y + 4, cz + 1, wall);
            air(level, cx, y + 1, cz, cx, y + 3, cz);
            if (dx != 0) {
                air(level, cx, y + 1, cz - 1, cx, y + 3, cz + 1);
            } else {
                air(level, cx - 1, y + 1, cz, cx + 1, y + 3, cz);
            }
        }
    }

    public static void spawner(LevelAccessor level, BlockPos pos, EntityType<?> type, RandomSource random) {
        if (level == null || pos == null) {
            return;
        }
        StructureHelper.set(level, pos, SPAWNER_STATE);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof SpawnerBlockEntity spawner && type != null && random != null) {
            spawner.setEntityId(type, random);
        }
    }

    public static void chest(LevelAccessor level, BlockPos pos, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot, RandomSource random) {
        if (level == null || pos == null || loot == null || random == null) {
            return;
        }
        StructureHelper.set(level, pos, CHEST_STATE);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(loot, random.nextLong());
        }
    }

    public static void dungeonChest(LevelAccessor level, BlockPos pos, RandomSource random) {
        chest(level, pos, BuiltInLootTables.SIMPLE_DUNGEON, random);
    }

    public static void altar(LevelAccessor level, BlockPos pos, boolean spawner, byte spawnType) {
        if (level == null || pos == null) {
            return;
        }
        StructureHelper.set(level, pos, Cache.ALTAR_STATE);
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof EldritchAltarTile altar) {
            altar.setSpawner(spawner);
            altar.setSpawnType(spawnType);
            altar.setChanged();
        }
    }

    public static void pillar(LevelAccessor level, BlockPos pos, int height) {
        if (level == null || pos == null) {
            return;
        }
        BlockState pillar = Cache.PILLAR_STATE;
        for (int i = 0; i < height; i++) {
            StructureHelper.set(level, pos.above(i), pillar);
        }
    }

    public static void urn(LevelAccessor level, BlockPos pos, RandomSource random) {
        if (level == null || pos == null || random == null) {
            return;
        }
        float roll = random.nextFloat();
        BlockState urn;
        if (roll < 0.1F) {
            urn = Cache.URN_RARE;
        } else if (roll < 0.3F) {
            urn = Cache.URN_UNCOMMON;
        } else {
            urn = Cache.URN_COMMON;
        }
        StructureHelper.set(level, pos, urn);
    }

    public static void wallCrystal(LevelAccessor level, BlockPos pos, RandomSource random) {
        if (level == null || pos == null || random == null) {
            return;
        }
        if (!level.getBlockState(pos).isAir()) {
            return;
        }
        boolean backed = false;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).canOcclude()) {
                backed = true;
                break;
            }
        }
        if (!backed) {
            return;
        }
        BlockState[] crystals = Cache.CRYSTALS;
        StructureHelper.set(level, pos, crystals[random.nextInt(crystals.length)]);
    }

    public static void crabOnWalls(LevelAccessor level, RandomSource random,
                                   int cx, int cz, int y, int count) {
        if (level == null || random == null || count <= 0) {
            return;
        }
        java.util.ArrayList<BlockPos> spots = new java.util.ArrayList<>();
        java.util.ArrayList<Direction> facings = new java.util.ArrayList<>();
        for (int x = cx - 7; x <= cx + 7; x++) {
            for (int z = cz - 7; z <= cz + 7; z++) {
                if (Math.abs(x - cx) != 7 && Math.abs(z - cz) != 7) {
                    continue;
                }
                for (int dy = 1; dy <= 2; dy++) {
                    BlockPos wall = new BlockPos(x, y + dy, z);
                    if (!isLiningWall(level, wall) || adjacentSpecial(level, wall)) {
                        continue;
                    }
                    Direction into = facingIntoRoom(level, wall, cx, cz);
                    if (into == null) {
                        continue;
                    }
                    spots.add(wall);
                    facings.add(into);
                }
            }
        }
        for (int i = spots.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos tmpPos = spots.get(i);
            spots.set(i, spots.get(j));
            spots.set(j, tmpPos);
            Direction tmpDir = facings.get(i);
            facings.set(i, facings.get(j));
            facings.set(j, tmpDir);
        }
        int limit = Math.min(count, spots.size());
        for (int i = 0; i < limit; i++) {
            placeCrab(level, spots.get(i), facings.get(i));
        }
    }

    private static boolean isLiningWall(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        Block wallBlock = Cache.WALL_BLOCK;
        if (wallBlock == null) {
            return false;
        }
        try {
            return level.getBlockState(pos).is(wallBlock);
        } catch (Exception e) {
            return false;
        }
    }

    private static Direction facingIntoRoom(LevelAccessor level, BlockPos wall, int cx, int cz) {
        if (level == null || wall == null) {
            return null;
        }
        int wallDist = Math.abs(wall.getX() - cx) + Math.abs(wall.getZ() - cz);
        Direction found = null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos next = wall.relative(dir);
            boolean air;
            try {
                air = level.getBlockState(next).isAir();
            } catch (Exception e) {
                continue;
            }
            if (!air) {
                continue;
            }
            if (found != null) {
                return null;
            }
            int nextDist = Math.abs(next.getX() - cx) + Math.abs(next.getZ() - cz);
            if (nextDist >= wallDist) {
                return null;
            }
            found = dir;
        }
        return found;
    }

    private static void placeCrab(LevelAccessor level, BlockPos pos, Direction facing) {
        if (level == null || pos == null || facing == null) {
            return;
        }
        BlockState crabState = Cache.CRAB_STATE;
        if (crabState.hasProperty(EldritchCrabBlock.FACING)) {
            crabState = crabState.setValue(EldritchCrabBlock.FACING, facing);
        }
        StructureHelper.set(level, pos, crabState);
        BlockEntity tile;
        try {
            tile = level.getBlockEntity(pos);
        } catch (Exception e) {
            return;
        }
        if (tile instanceof EldritchCrabTile crabTile) {
            crabTile.setFacing(facing);
            crabTile.setChanged();
        }
    }

    public static void decorate(LevelAccessor level, RandomSource random,
                                int x1, int y, int z1, int x2, int z2, boolean nest) {
        if (level == null || random == null) {
            return;
        }
        int xMin = Math.min(x1, x2);
        int xMax = Math.max(x1, x2);
        int zMin = Math.min(z1, z2);
        int zMax = Math.max(z1, z2);
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                BlockPos floorPos = new BlockPos(x, y, z);
                if (!isMazeFloor(level.getBlockState(floorPos))) {
                    continue;
                }
                BlockPos above = floorPos.above();
                if (!level.getBlockState(above).isAir()) {
                    continue;
                }
                if (random.nextInt(25) != 0) {
                    continue;
                }
                urn(level, above, random);
            }
        }
        int cx = (xMin + xMax) / 2;
        int cz = (zMin + zMax) / 2;
        int crabs = nest ? 2 : (random.nextInt(50) == 0 ? 1 : 0);
        crabOnWalls(level, random, cx, cz, y, crabs);
    }

    private static boolean isMazeFloor(BlockState state) {
        if (state == null) {
            return false;
        }
        Block floorBlock = Cache.FLOOR_BLOCK;
        Block wallBlock = Cache.WALL_BLOCK;
        return (floorBlock != null && state.is(floorBlock))
                || (wallBlock != null && state.is(wallBlock));
    }

    private static boolean adjacentSpecial(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        Block crab = Cache.CRAB_BLOCK;
        Block trap = Cache.TRAP_BLOCK;
        Block altar = Cache.ALTAR_BLOCK;
        if (crab == null && trap == null && altar == null) {
            return false;
        }
        for (Direction dir : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(dir));
            if ((crab != null && state.is(crab))
                    || (trap != null && state.is(trap))
                    || (altar != null && state.is(altar))) {
                return true;
            }
        }
        return false;
    }

    public static void trap(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        StructureHelper.set(level, pos, Cache.TRAP_STATE);
    }

    public static void obelisk(LevelAccessor level, BlockPos pos, int height) {
        if (level == null || pos == null) {
            return;
        }
        StructureHelper.set(level, pos, Cache.OBELISK_STATE);
        BlockState body = Cache.OBELISK_BODY;
        for (int i = 1; i <= height; i++) {
            BlockPos above = pos.above(i);
            try {
                if (StructureHelper.replaceable(level, above)) {
                    StructureHelper.set(level, above, body);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void stairs(LevelAccessor level, BlockPos pos, Direction facing, boolean top) {
        if (level == null || pos == null || facing == null) {
            return;
        }
        Block block = Cache.STAIRS_BLOCK;
        if (block == null || block == Blocks.AIR) {
            return;
        }
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(StairBlock.FACING)) {
            state = state.setValue(StairBlock.FACING, facing);
        }
        if (state.hasProperty(StairBlock.HALF)) {
            state = state.setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM);
        }
        if (state.hasProperty(StairBlock.SHAPE)) {
            state = state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
        }
        StructureHelper.set(level, pos, state);
    }
}
