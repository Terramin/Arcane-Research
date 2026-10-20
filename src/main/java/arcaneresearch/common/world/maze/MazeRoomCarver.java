package arcaneresearch.common.world.maze;
import arcaneresearch.common.blocks.aura.EldritchCrabBlock;
import arcaneresearch.common.tiles.aura.EldritchCrabTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;
import arcaneresearch.common.world.structures.StructureHelper;
import net.minecraft.util.RandomSource;
public final class MazeRoomCarver {
    public static final String WALL = "arcaneresearch:eldritch_stone_tile";
    public static final String FLOOR = "arcaneresearch:ancient_stone";
    public static final String PILLAR = "arcaneresearch:eldritch_pillar";
    private MazeRoomCarver() {
    }
    public static void fill(LevelAccessor level, int x1, int y1, int z1,
                            int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    StructureHelper.set(level, new BlockPos(x, y, z), state);
                }
            }
        }
    }
    public static void air(LevelAccessor level, int x1, int y1, int z1, int x2, int y2, int z2) {
        fill(level, x1, y1, z1, x2, y2, z2, Blocks.AIR.defaultBlockState());
    }
    public static void hollowRoom(LevelAccessor level, BlockPos floorCenter, int w, int h,
                                  BlockState wall, BlockState floor) {
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
    public static void spawner(LevelAccessor level, BlockPos pos,
                               net.minecraft.world.entity.EntityType<?> type,
                               net.minecraft.util.RandomSource random) {
        StructureHelper.set(level, pos, Blocks.SPAWNER.defaultBlockState());
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, random);
        }
    }
    public static void chest(LevelAccessor level, BlockPos pos, ResourceLocation loot, RandomSource random) {
        StructureHelper.set(level, pos, Blocks.CHEST.defaultBlockState());
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(loot, random.nextLong());
        }
    }
    public static void dungeonChest(LevelAccessor level, BlockPos pos, RandomSource random) {
        chest(level, pos, BuiltInLootTables.SIMPLE_DUNGEON, random);
    }
    public static void altar(LevelAccessor level, BlockPos pos, boolean spawner, byte spawnType) {
        StructureHelper.set(level, pos, StructureHelper.state("arcaneresearch:eldritch_altar"));
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof EldritchAltarTile altar) {
            altar.setSpawner(spawner);
            altar.setSpawnType(spawnType);
            altar.setChanged();
        }
    }
    public static void pillar(LevelAccessor level, BlockPos pos, int height) {
        BlockState pillar = StructureHelper.state(PILLAR);
        for (int i = 0; i < height; i++) {
            StructureHelper.set(level, pos.above(i), pillar);
        }
    }
    public static void urn(LevelAccessor level, BlockPos pos, RandomSource random) {
        float roll = random.nextFloat();
        String id = "arcaneresearch:loot_urn_common";
        if (roll < 0.1F) {
            id = "arcaneresearch:loot_urn_rare";
        } else if (roll < 0.3F) {
            id = "arcaneresearch:loot_urn_uncommon";
        }
        StructureHelper.set(level, pos, StructureHelper.state(id));
    }
    public static void wallCrystal(LevelAccessor level, BlockPos pos, RandomSource random) {
        if (!level.getBlockState(pos).isAir()) {
            return;
        }
        boolean backed = false;
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).isSolid()) {
                backed = true;
                break;
            }
        }
        if (!backed) {
            return;
        }
        String[] crystals = {
                "arcaneresearch:crystal_air", "arcaneresearch:crystal_fire", "arcaneresearch:crystal_water",
                "arcaneresearch:crystal_earth", "arcaneresearch:crystal_order", "arcaneresearch:crystal_entropy"};
        StructureHelper.set(level, pos,
                StructureHelper.state(crystals[random.nextInt(crystals.length)]));
    }
    public static void crabOnWalls(LevelAccessor level, RandomSource random,
                                   int cx, int cz, int y, int count) {
        if (count <= 0) {
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
        for (int i = 0; i < Math.min(count, spots.size()); i++) {
            placeCrab(level, spots.get(i), facings.get(i));
        }
    }
    private static boolean isLiningWall(LevelAccessor level, BlockPos pos) {
        try {
            BlockState state = level.getBlockState(pos);
            return state.is(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new ResourceLocation(WALL)));
        } catch (Exception e) {
            return false;
        }
    }
    private static Direction facingIntoRoom(LevelAccessor level, BlockPos wall, int cx, int cz) {
        int wallDist = Math.abs(wall.getX() - cx) + Math.abs(wall.getZ() - cz);
        Direction found = null;
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            boolean air;
            try {
                air = level.getBlockState(wall.relative(dir)).isAir();
            } catch (Exception e) {
                continue;
            }
            if (!air) {
                continue;
            }
            if (found != null) {
                return null;
            }
            BlockPos next = wall.relative(dir);
            int nextDist = Math.abs(next.getX() - cx) + Math.abs(next.getZ() - cz);
            if (nextDist >= wallDist) {
                return null;
            }
            found = dir;
        }
        return found;
    }
    private static void placeCrab(LevelAccessor level, BlockPos pos, Direction facing) {
        BlockState crabState = StructureHelper.state("arcaneresearch:eldritch_crab");
        try {
            if (crabState.hasProperty(arcaneresearch.common.blocks.aura.EldritchCrabBlock.FACING)) {
                crabState = crabState.setValue(arcaneresearch.common.blocks.aura.EldritchCrabBlock.FACING, facing);
            }
        } catch (Exception ignored) {
        }
        StructureHelper.set(level, pos, crabState);
        BlockEntity tile = null;
        try {
            tile = level.getBlockEntity(pos);
        } catch (Exception ignored) {
        }
        if (tile instanceof arcaneresearch.common.tiles.aura.EldritchCrabTile crabTile) {
            crabTile.setFacing(facing);
            crabTile.setChanged();
        }
    }
    public static void decorate(LevelAccessor level, RandomSource random,
                                int x1, int y, int z1, int x2, int z2, boolean nest) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
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
        int cx = (Math.min(x1, x2) + Math.max(x1, x2)) / 2;
        int cz = (Math.min(z1, z2) + Math.max(z1, z2)) / 2;
        int crabs = nest ? 2 : (random.nextInt(50) == 0 ? 1 : 0);
        crabOnWalls(level, random, cx, cz, y, crabs);
    }
    private static boolean isMazeFloor(BlockState state) {
        return state.is(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                        new ResourceLocation(FLOOR)))
                || state.is(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                        new ResourceLocation("arcaneresearch:eldritch_stone_tile")));
    }
    private static boolean adjacentSpecial(LevelAccessor level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(dir));
            if (state.is(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation("arcaneresearch:eldritch_crab")))
                    || state.is(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation("arcaneresearch:eldritch_trap")))
                    || state.is(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation("arcaneresearch:eldritch_altar")))) {
                return true;
            }
        }
        return false;
    }
    public static void trap(LevelAccessor level, BlockPos pos) {
        StructureHelper.set(level, pos, StructureHelper.state("arcaneresearch:eldritch_trap"));
    }
    public static void obelisk(LevelAccessor level, BlockPos pos, int height) {
        StructureHelper.set(level, pos, StructureHelper.state("arcaneresearch:eldritch_obelisk"));
        BlockState body = StructureHelper.state("arcaneresearch:eldritch_obelisk_body");
        for (int i = 1; i <= height; i++) {
            try {
                if (StructureHelper.replaceable(level, pos.above(i))) {
                    StructureHelper.set(level, pos.above(i), body);
                }
            } catch (Exception ignored) {
            }
        }
    }
    public static void stairs(LevelAccessor level, BlockPos pos,
                              net.minecraft.core.Direction facing, boolean top) {
        try {
            net.minecraft.world.level.block.Block block =
                    net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                            new ResourceLocation("arcaneresearch:eldritch_stairs"));
            if (block == null || block == Blocks.AIR) {
                return;
            }
            BlockState state = block.defaultBlockState();
            if (state.hasProperty(net.minecraft.world.level.block.StairBlock.FACING)) {
                state = state.setValue(net.minecraft.world.level.block.StairBlock.FACING, facing);
            }
            if (state.hasProperty(net.minecraft.world.level.block.StairBlock.HALF)) {
                state = state.setValue(net.minecraft.world.level.block.StairBlock.HALF,
                        top ? net.minecraft.world.level.block.state.properties.Half.TOP
                                : net.minecraft.world.level.block.state.properties.Half.BOTTOM);
            }
            if (state.hasProperty(net.minecraft.world.level.block.StairBlock.SHAPE)) {
                state = state.setValue(net.minecraft.world.level.block.StairBlock.SHAPE,
                        net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT);
            }
            StructureHelper.set(level, pos, state);
        } catch (Exception ignored) {
        }
    }
}
