package arcaneresearch.common.world.maze;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.init.ModEntities;
import arcaneresearch.common.world.structures.StructureHelper;

import net.minecraft.util.RandomSource;

/**
 * Room builders per maze feature id, mirroring TC5 Gen* rooms with real
 * block placement. Each room occupies one chunk cell (16x16) at height y.
 * Feature ids: 1 portal, 2-5 boss, 6 key, 7 nest, 8 library, 9-10 turret,
 * 11 passage room, else plain passage carved along open walls.
 */
public final class MazeRooms {
    private MazeRooms() {
    }

    public static void build(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        switch (cell.feature) {
            case 1:
                portalRoom(level, random, cx, cz, y, cell);
                break;
            case 2:
                bossVaultRoom(level, random, cx, cz, y, cell);
                break;
            case 3:
            case 4:
            case 5:
                bossRoom(level, random, cx, cz, y, cell);
                break;
            case 6:
                keyRoom(level, random, cx, cz, y, cell);
                break;
            case 7:
                nestRoom(level, random, cx, cz, y, cell);
                break;
            case 8:
                libraryRoom(level, random, cx, cz, y, cell);
                break;
            case 9:
            case 10:
                turretRoom(level, random, cx, cz, y, cell);
                break;
            case 11:
                chamber(level, random, cx, cz, y, cell);
                break;
            default:
                passage(level, random, cx, cz, y, cell);
                break;
        }
    }

    static BlockPos cellFloor(int cx, int cz, int y) {
        return new BlockPos(cx * 16 + 8, y, cz * 16 + 8);
    }

    static BlockState wall() {
        return StructureHelper.state(MazeRoomCarver.WALL);
    }

    static BlockState floor() {
        return StructureHelper.state(MazeRoomCarver.FLOOR);
    }

    /** Облицовка eldritch-камнем: кольцо стен ±7 и плита потолка y+5. */
    static BlockState lining() {
        return StructureHelper.state("arcaneresearch:eldritch_stone_tile");
    }

    static void shell(LevelAccessor level, RandomSource random, int cx, int cz, int y) {
        BlockPos c = cellFloor(cx, cz, y);
        // Снаружи — bedrock (как TC4 id 1), пол — ancient.
        MazeRoomCarver.fill(level, c.getX() - 8, y - 1, c.getZ() - 8, c.getX() + 8, y + 6, c.getZ() + 8,
                Blocks.BEDROCK.defaultBlockState());
        MazeRoomCarver.air(level, c.getX() - 7, y + 1, c.getZ() - 7, c.getX() + 7, y + 5, c.getZ() + 7);
        MazeRoomCarver.fill(level, c.getX() - 7, y, c.getZ() - 7, c.getX() + 7, y, c.getZ() + 7,
                floor());
        // Внутри — eldritch-камень: стены ±7 и потолок y+5, воздух ±6 y+1..y+4.
        // Ровно стыкуется с коридорами (воздух y+1..y+4, потолок y+5).
        MazeRoomCarver.fill(level, c.getX() - 7, y + 1, c.getZ() - 7, c.getX() + 7, y + 5, c.getZ() + 7,
                lining());
        MazeRoomCarver.air(level, c.getX() - 6, y + 1, c.getZ() - 6, c.getX() + 6, y + 4, c.getZ() + 6);
    }

    /**
     * Портальная комната, порт TC4 GenPortal.generatePortal (детально):
     * bedrock-корка, каменные кольца, пирамиды пола/потолка, ряды ступеней,
     * угловые ниши, крышка + портал + обелиск в центре. База y-1 чтобы пол
     * (y+1 → feet 51) совпал с коридорами. Легенда id: 1 bedrock по воздуху,
     * 8 стена, 2/19 пол, 9 воздух, 10/11 ступени. После узора — центр-зал
     * для прохода, алтарь и коридоры.
     */
    static void portalRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        int yy = y - 1;
        int x = cx * 16;
        int z = cz * 16;
        BlockPos c = cellFloor(cx, cz, y);
        int i;
        int b;
        for (i = 1; i <= 15; i++) {
            for (b = 1; b <= 15; b++) {
                for (int ch = 0; ch < 13; ch++) {
                    if (i == 1 || i == 15 || b == 1 || b == 15) {
                        bedrockIfAir(level, x + i, yy + ch, z + b);
                    }
                }
            }
        }
        for (i = 2; i <= 14; i++) {
            for (b = 2; b <= 14; b++) {
                for (int ch = 1; ch < 12; ch++) {
                    if ((i == 2 || i == 14 || b == 2 || b == 14)
                            && (i != 2 || b <= 3 || b >= 12 || !cell.west || ch >= 10)
                            && (i != 14 || b <= 3 || b >= 12 || !cell.east || ch >= 10)
                            && (b != 2 || i <= 3 || i >= 12 || !cell.north || ch >= 10)
                            && (b != 14 || i <= 3 || i >= 12 || !cell.south || ch >= 10)) {
                        StructureHelper.set(level, new BlockPos(x + i, yy + ch, z + b), wall());
                    }
                }
            }
        }
        for (i = 3; i <= 13; i++) {
            for (b = 3; b <= 13; b++) {
                for (int ch = 2; ch < 11; ch++) {
                    if ((i == 3 || i == 13 || b == 3 || b == 13)
                            && (i > 4 || b > 4) && (i > 4 || b < 12)
                            && (i < 12 || b > 4) && (i < 12 || b < 12)) {
                        StructureHelper.set(level, new BlockPos(x + i, yy + ch, z + b), floor());
                    }
                }
            }
        }
        for (i = 2; i <= 14; i++) {
            for (b = 2; b <= 14; b++) {
                bedrockIfAir(level, x + i, yy - 1, z + b);
                StructureHelper.set(level, new BlockPos(x + i, yy, z + b), wall());
                StructureHelper.set(level, new BlockPos(x + i, yy + 1, z + b), floor());
                bedrockIfAir(level, x + i, yy + 13, z + b);
                StructureHelper.set(level, new BlockPos(x + i, yy + 12, z + b), wall());
                StructureHelper.set(level, new BlockPos(x + i, yy + 11, z + b), floor());
                if (i > 1 && i < 15 && b > 1 && b < 15) {
                    int q = Math.min(Math.abs(8 - i), Math.abs(8 - b));
                    for (int j = 0; j < q - 1; j++) {
                        StructureHelper.set(level, new BlockPos(x + i, yy + 1 + j, z + b), floor());
                    }
                }
                if (i > 3 && i < 13 && b > 3 && b < 13) {
                    int q = Math.min(Math.abs(8 - i), Math.abs(8 - b));
                    for (int j = 0; j < q; j++) {
                        StructureHelper.set(level, new BlockPos(x + i, yy + 11 - j, z + b), floor());
                    }
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            MazeRoomCarver.stairs(level, new BlockPos(x + 6 + g, yy + 2, z + 4),
                    net.minecraft.core.Direction.NORTH, false);
            MazeRoomCarver.stairs(level, new BlockPos(x + 6 + g, yy + 2, z + 12),
                    net.minecraft.core.Direction.SOUTH, false);
            MazeRoomCarver.stairs(level, new BlockPos(x + 12, yy + 2, z + 6 + g),
                    net.minecraft.core.Direction.EAST, false);
            MazeRoomCarver.stairs(level, new BlockPos(x + 4, yy + 2, z + 6 + g),
                    net.minecraft.core.Direction.WEST, false);
        }
        // Позиция вызова соединений как в TC4/reforged: между ободными ступенями
        // и угловыми нишами. Устьевые штампы PAT_CONNECT перекрывают bedrock
        // скорлупы (ряд 1) и проём кольца (ряд 2) камнем — то, что у TC4 делает
        // generateConnections(3, true), а наши коридоры-трубы затем stitching
        // граничный ряд (d 0) сами.
        mouthGallery(level, cx, cz, y, cell);
        connect(level, random, cx, cz, y, cell);
        for (int a = 3; a <= 13; a++) {
            for (b = 3; b <= 13; b++) {
                for (int ch = 1; ch < 12; ch++) {
                    if ((a <= 4 && b <= 4) || (a <= 4 && b >= 12)
                            || (a >= 12 && b <= 4) || (a >= 12 && b >= 12)) {
                        StructureHelper.set(level, new BlockPos(x + a, yy + ch, z + b),
                                Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 3, z + 5),
                net.minecraft.core.Direction.NORTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 4, yy + 3, z + 5),
                net.minecraft.core.Direction.NORTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 3, z + 4),
                net.minecraft.core.Direction.WEST, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 8, z + 5),
                net.minecraft.core.Direction.NORTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 4, yy + 8, z + 5),
                net.minecraft.core.Direction.NORTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 8, z + 4),
                net.minecraft.core.Direction.WEST, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 12, yy + 3, z + 5),
                net.minecraft.core.Direction.NORTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 3, z + 5),
                net.minecraft.core.Direction.NORTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 3, z + 4),
                net.minecraft.core.Direction.EAST, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 12, yy + 8, z + 5),
                net.minecraft.core.Direction.NORTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 8, z + 5),
                net.minecraft.core.Direction.NORTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 8, z + 4),
                net.minecraft.core.Direction.EAST, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 3, z + 11),
                net.minecraft.core.Direction.SOUTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 4, yy + 3, z + 11),
                net.minecraft.core.Direction.SOUTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 3, z + 12),
                net.minecraft.core.Direction.WEST, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 8, z + 11),
                net.minecraft.core.Direction.SOUTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 4, yy + 8, z + 11),
                net.minecraft.core.Direction.SOUTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 5, yy + 8, z + 12),
                net.minecraft.core.Direction.WEST, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 12, yy + 3, z + 11),
                net.minecraft.core.Direction.SOUTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 3, z + 11),
                net.minecraft.core.Direction.SOUTH, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 3, z + 12),
                net.minecraft.core.Direction.EAST, false);
        MazeRoomCarver.stairs(level, new BlockPos(x + 12, yy + 8, z + 11),
                net.minecraft.core.Direction.SOUTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 8, z + 11),
                net.minecraft.core.Direction.SOUTH, true);
        MazeRoomCarver.stairs(level, new BlockPos(x + 11, yy + 8, z + 12),
                net.minecraft.core.Direction.EAST, true);
        // Центр-зал для прохода (пол yy+1 цел).
        MazeRoomCarver.air(level, x + 5, yy + 2, z + 5, x + 11, yy + 7, z + 11);
        // Центр: крышка, портал, обелиск. Рама портала держится
        // (крышка снизу, обелиск сверху — см. isFrame).
        StructureHelper.set(level, new BlockPos(x + 8, yy + 2, z + 8),
                StructureHelper.state("arcaneresearch:eldritch_cap"));
        StructureHelper.set(level, new BlockPos(x + 8, yy + 3, z + 8),
                StructureHelper.state("arcaneresearch:eldritch_portal"));
        MazeRoomCarver.obelisk(level, new BlockPos(x + 8, yy + 4, z + 8), 4);
        MazeRoomCarver.altar(level, c.offset(0, 1, 4), true, (byte) 1);
    }

    /** id 1 TC4: bedrock только по воздуху (не трогает построенное). */
    static void bedrockIfAir(LevelAccessor level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getBlockState(pos).isAir()) {
            StructureHelper.set(level, pos, Blocks.BEDROCK.defaultBlockState());
        }
    }

    /** Срезы PAT_CONNECT TC4/reforged GenCommon, строка = высота h
     * (h 0 — потолочный бандаж). Идентификаторы: 1 bedrock-по-воздуху,
     * 2 ancient_stone (пол/панели), 8 «ничто» (у нас видимый
     * eldritch_stone_tile), 9 воздух, 3/4/5/6 ступени (низ/верх у окна),
     * 0 — пропуск. */
    private static final int[][] PAT_CONNECT = {
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
            {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 2, 5, 9, 9, 9, 6, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 9, 9, 9, 9, 9, 2, 8, 1},
            {1, 8, 2, 3, 9, 9, 9, 4, 2, 8, 1},
            {1, 8, 8, 2, 2, 2, 2, 2, 8, 8, 1},
            {1, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}
    };

    /**
     * Устьевые галереи PAT_CONNECT у порт-зала: порт вызова
     * TC4/reforged generateConnections(3, true), которым GenPortal
     * перекрывает bedrock-скорлупу (ряд 1) и проём каменного кольца
     * (ряд 2) камнем, оставляя окно-устье у рамы (ряд 3 — открытый
     * пролёт без id 8). Именно из-за отсутствия этого штампа bedrock
     * корлупы «заезжал» в устье наших коридоров и над окном оставался
     * 8-широкий провал кольца.
     *
     * Отличия от TC4: только Своя половина стыка — d 1..3 (ряды скорлупа/
     * кольцо/рама): граничный ряд (d 0) пишут наши коридоры-трубы, соседа
     * не трогаем. Вертикаль y+8-h (сдвиг -2 от TC4 y+10-h), чтобы пол
     * штампа (строка h 8) лег на пол наших коридоров (y). Штампы
     * идемпотентны, порядок постройки не важен.
     */
    static void mouthGallery(LevelAccessor level, int cx, int cz, int y, Cell cell) {
        int x = cx * 16;
        int z = cz * 16;
        for (int side = 0; side < 4; side++) {
            boolean open = side == 0 ? cell.north : side == 1 ? cell.south
                    : side == 2 ? cell.east : cell.west;
            if (!open) {
                continue;
            }
            boolean alongZ = side < 2;
            for (int d = 1; d <= 3; d++) {
                // Сужение окна, как в TC4 justthetip: ряд перед рамой 1..9,
                // ряд рамы 2..8 и без id 8 (пролёт прочистили, камень
                // оставляем — так и вышел открытый воронок).
                int w0 = d <= 1 ? 0 : d == 2 ? 1 : 2;
                int w1 = d <= 1 ? 10 : d == 2 ? 9 : 8;
                boolean skipEight = d == 3;
                for (int w = w0; w <= w1; w++) {
                    for (int h = 0; h <= 10; h++) {
                        int id = PAT_CONNECT[h][w];
                        if (id == 0 || (skipEight && id == 8)) {
                            continue;
                        }
                        int px = alongZ ? x + 3 + w
                                : (side == 2 ? x + 16 - d : x + d);
                        int pz = alongZ ? (side == 0 ? z + d : z + 16 - d)
                                : z + 3 + w;
                        patBlock(level, px, y + 8 - h, pz, id, alongZ);
                    }
                }
            }
        }
    }

    /**
     * Блок непосредственно штампов (порт TC4 placeBlock): 1 bedrock по
     * воздуху, 2 ancient_stone, 8 стена-облицовка, 9 воздух, 3..6 ступени
     * eldritch_stairs (низ/верх, поворот — направление стыка: NS → W/E,
     * EW → N/S, как меты 1.7.10).
     */
    static void patBlock(LevelAccessor level, int px, int py, int pz, int id,
                         boolean alongZ) {
        BlockPos pos = new BlockPos(px, py, pz);
        switch (id) {
            case 1:
                bedrockIfAir(level, px, py, pz);
                break;
            case 2:
                StructureHelper.set(level, pos, floor());
                break;
            case 8:
                StructureHelper.set(level, pos, wall());
                break;
            case 9:
                StructureHelper.set(level, pos, Blocks.AIR.defaultBlockState());
                break;
            case 3:
            case 4:
            case 5:
            case 6:
                net.minecraft.core.Direction facing = alongZ
                        ? (id == 3 || id == 5 ? net.minecraft.core.Direction.WEST
                                : net.minecraft.core.Direction.EAST)
                        : (id == 3 || id == 5 ? net.minecraft.core.Direction.NORTH
                                : net.minecraft.core.Direction.SOUTH);
                MazeRoomCarver.stairs(level, pos, facing, id >= 5);
                break;
            default:
                break;
        }
    }

    static void bossRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        BlockPos c = cellFloor(cx, cz, y);
        MazeRoomCarver.spawner(level, c.above(2), ModEntities.ELDRITCH_GUARDIAN.get(), random);
        MazeRoomCarver.dungeonChest(level, c.offset(-4, 1, -4), random);
        MazeRoomCarver.dungeonChest(level, c.offset(4, 1, 4), random);
        MazeRoomCarver.urn(level, c.offset(5, 1, 5), random);
        MazeRoomCarver.wallCrystal(level, c.offset(6, 2, 4), random);
        MazeRoomCarver.wallCrystal(level, c.offset(-6, 2, -4), random);
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, false);
        connect(level, random, cx, cz, y, cell);
        connectFellows(level, cx, cz, y);
    }

    /**
     * Сокровищница боссов (северо-западный квадрант арены 2x2, feature 2):
     * спавнеры голема и надзирателя, замок цикла и шесть сундуков.
     * Всё содержимое — в углах и центре, вне полос коридоров.
     */
    static void bossVaultRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        BlockPos c = cellFloor(cx, cz, y);
        MazeRoomCarver.spawner(level, c.offset(-3, 1, -3), ModEntities.ELDRITCH_GOLEM.get(), random);
        MazeRoomCarver.spawner(level, c.offset(3, 1, 3), ModEntities.ELDRITCH_WARDEN.get(), random);
        MazeRoomCarver.dungeonChest(level, c.offset(-5, 1, -5), random);
        MazeRoomCarver.dungeonChest(level, c.offset(5, 1, -5), random);
        MazeRoomCarver.dungeonChest(level, c.offset(-5, 1, 5), random);
        MazeRoomCarver.dungeonChest(level, c.offset(5, 1, 5), random);
        MazeRoomCarver.dungeonChest(level, c.offset(4, 1, 6), random);
        MazeRoomCarver.dungeonChest(level, c.offset(-4, 1, -6), random);
        MazeRoomCarver.trap(level, c.offset(2, 0, 2));
        MazeRoomCarver.trap(level, c.offset(-2, 0, -2));
        MazeRoomCarver.wallCrystal(level, c.offset(6, 2, -4), random);
        MazeRoomCarver.wallCrystal(level, c.offset(-6, 2, 4), random);
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, false);
        connect(level, random, cx, cz, y, cell);
        connectFellows(level, cx, cz, y);
    }

    static void keyRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        BlockPos c = cellFloor(cx, cz, y);
        MazeRoomCarver.dungeonChest(level, c.above(), random);
        MazeRoomCarver.spawner(level, c.offset(3, 2, 3), ModEntities.CULTIST_KNIGHT.get(), random);
        MazeRoomCarver.urn(level, c.offset(-5, 1, -5), random);
        MazeRoomCarver.urn(level, c.offset(5, 1, 5), random);
        MazeRoomCarver.trap(level, c.offset(2, 0, 2));
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, false);
        connect(level, random, cx, cz, y, cell);
    }

    static void nestRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        BlockPos c = cellFloor(cx, cz, y);
        MazeRoomCarver.spawner(level, c.offset(-3, 2, -3), ModEntities.MIND_SPIDER.get(), random);
        MazeRoomCarver.spawner(level, c.offset(3, 2, 3), ModEntities.TAINT_CRAWLER.get(), random);
        MazeRoomCarver.fill(level, c.getX() - 2, y + 1, c.getZ() - 2, c.getX() + 2, y + 1, c.getZ() + 2,
                Blocks.COBWEB.defaultBlockState());
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, true);
        connect(level, random, cx, cz, y, cell);
    }

    static void libraryRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        BlockPos c = cellFloor(cx, cz, y);
        // Шкафы только по углам: полосы во всю ширину перекрывали
        // створы коридоров (|x|<=3 и |z|<=3 — зона проходов).
        BlockState shelf = Blocks.BOOKSHELF.defaultBlockState();
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                MazeRoomCarver.fill(level, c.getX() + sx * 4, y + 1, c.getZ() + sz * 4,
                        c.getX() + sx * 6, y + 2, c.getZ() + sz * 6, shelf);
            }
        }
        MazeRoomCarver.dungeonChest(level, c.offset(0, 1, 0), random);
        MazeRoomCarver.spawner(level, c.offset(0, 3, 0), ModEntities.CULTIST_CLERIC.get(), random);
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, false);
        connect(level, random, cx, cz, y, cell);
    }

    static void turretRoom(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        BlockPos c = cellFloor(cx, cz, y);
        MazeRoomCarver.spawner(level, c.offset(-4, 2, 0), ModEntities.CULTIST_KNIGHT.get(), random);
        MazeRoomCarver.spawner(level, c.offset(4, 2, 0), ModEntities.CULTIST_KNIGHT.get(), random);
        MazeRoomCarver.dungeonChest(level, c.offset(0, 1, -4), random);
        MazeRoomCarver.trap(level, c.offset(2, 0, 2));
        MazeRoomCarver.trap(level, c.offset(-2, 0, -2));
        MazeRoomCarver.wallCrystal(level, c.offset(5, 2, 5), random);
        MazeRoomCarver.wallCrystal(level, c.offset(-5, 2, -5), random);
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, false);
        connect(level, random, cx, cz, y, cell);
    }

    static void chamber(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        shell(level, random, cx, cz, y);
        // Столбы убраны из генерации измерения по требованию.
        BlockPos c = cellFloor(cx, cz, y);
        MazeRoomCarver.decorate(level, random, c.getX() - 6, y, c.getZ() - 6,
                c.getX() + 6, c.getZ() + 6, false);
        connect(level, random, cx, cz, y, cell);
    }

    static void passage(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        BlockPos c = cellFloor(cx, cz, y);
        // Мини-зал 7x7: снаружи bedrock, внутри eldritch-камень
        // (стены ±3, потолок y+5), воздух ±2 y+1..y+4 — как у больших комнат.
        MazeRoomCarver.fill(level, c.getX() - 4, y - 1, c.getZ() - 4, c.getX() + 4, y + 6, c.getZ() + 4,
                Blocks.BEDROCK.defaultBlockState());
        MazeRoomCarver.air(level, c.getX() - 3, y + 1, c.getZ() - 3, c.getX() + 3, y + 4, c.getZ() + 3);
        MazeRoomCarver.fill(level, c.getX() - 3, y, c.getZ() - 3, c.getX() + 3, y, c.getZ() + 3, floor());
        MazeRoomCarver.fill(level, c.getX() - 3, y + 1, c.getZ() - 3, c.getX() + 3, y + 5, c.getZ() + 3,
                lining());
        MazeRoomCarver.air(level, c.getX() - 2, y + 1, c.getZ() - 2, c.getX() + 2, y + 4, c.getZ() + 2);
        connect(level, random, cx, cz, y, cell);
    }

    /**
     * Полуразмер комнаты: оболочка ±8, мини-зал пустышки ±4.
     * Пустышки — feature 0 и 21..24 (все идут в passage()).
     */
    static int roomSpan(Cell cell) {
        if (cell == null) {
            return -1;
        }
        int f = cell.feature;
        if (f == 0 || (f >= 21 && f <= 24)) {
            return 4;
        }
        return 8;
    }

    /**
     * Позиция внутренней облицовки: ±7 у оболочек, ±3 у пустышек.
     * Коридор обязан пробить ОБЕ облицовки (внешнюю bedrock-стену
     * и внутреннюю каменную), иначе комнаты остаются запечатанными:
     * интервал только по внешним стенам [8..8] оставлял облицовку целой.
     */
    static int roomLining(Cell cell) {
        if (cell == null) {
            return -1;
        }
        int f = cell.feature;
        if (f == 0 || (f >= 21 && f <= 24)) {
            return 3;
        }
        return 7;
    }

    /**
     * Проёмы между квадрантами арены 2x2 (features 2-5). Генератор не ставит
     * флаги между квадрантами — без этого каждая четверть замурована наглухо
     * и соседние комнаты арены не соединены. Квадранты — оболочки (±8),
     * общий интервал — одна общая стена.
     */
    static void connectFellows(LevelAccessor level, int cx, int cz, int y) {
        BlockPos c = cellFloor(cx, cz, y);
        if (isFellow(cx, cz - 1)) {
            corridorNS(level, c, c.getZ() - 9, c.getZ() - 7, y, 7, 7);
        }
        if (isFellow(cx, cz + 1)) {
            corridorNS(level, c, c.getZ() + 7, c.getZ() + 9, y, 7, 7);
        }
        if (isFellow(cx - 1, cz)) {
            corridorEW(level, c, c.getX() - 9, c.getX() - 7, y, 7, 7);
        }
        if (isFellow(cx + 1, cz)) {
            corridorEW(level, c, c.getX() + 7, c.getX() + 9, y, 7, 7);
        }
    }

    static boolean isFellow(int cx, int cz) {
        Cell cell = MazeHandler.get(new CellLoc(cx, cz));
        return cell != null && cell.feature >= 2 && cell.feature <= 5;
    }

    /**
     * Коридоры между комнатами, порт TC4 GenCommon.generateConnections.
     * Длина НЕ статична: коридор идёт ровно от своей облицовки до облицовки
     * соседа (±7 у оболочек, ±3 у пустышек): пробиваются оба слоя стен
     * с обеих сторон. Внутрь комнат коридор не заезжает. Оба соседа пишут
     * одни и те же клетки — результат не зависит от порядка постройки.
     * Сечение по высоте совпадает с комнатами (пол y, воздух y+1..y+4,
     * потолок y+5, крыша y+6), ступеней нет. Материалы как в TC4: снаружи
     * bedrock (id 1), внутри eldritch-камень (id 8), пол — ancient (id 2).
     */
    static void connect(LevelAccessor level, RandomSource random, int cx, int cz, int y, Cell cell) {
        BlockPos c = cellFloor(cx, cz, y);
        if (cell.north) {
            carveSideNS(level, c, y, -1, cell, MazeHandler.get(new CellLoc(cx, cz - 1)));
        }
        if (cell.south) {
            carveSideNS(level, c, y, 1, cell, MazeHandler.get(new CellLoc(cx, cz + 1)));
        }
        if (cell.west) {
            carveSideEW(level, c, y, -1, cell, MazeHandler.get(new CellLoc(cx - 1, cz)));
        }
        if (cell.east) {
            carveSideEW(level, c, y, 1, cell, MazeHandler.get(new CellLoc(cx + 1, cz)));
        }
    }

    /**
     * Коридор на одну сторону: от своей облицовки (inclusive) до облицовки
     * соседа. Пробиваются оба слоя стен с обеих сторон; оба соседа пишут
     * одни клетки — идемпотентно.
     */
    static void carveSideNS(LevelAccessor level, BlockPos c, int y, int dir, Cell own, Cell other) {
        int ownLining = roomLining(own);
        int otherLining = roomLining(other);
        if (ownLining < 0 || otherLining < 0) {
            return;
        }
        corridorNS(level, c, c.getZ() + dir * ownLining, c.getZ() + dir * (16 - otherLining), y,
                ownLining, otherLining);
    }

    static void carveSideEW(LevelAccessor level, BlockPos c, int y, int dir, Cell own, Cell other) {
        int ownLining = roomLining(own);
        int otherLining = roomLining(other);
        if (ownLining < 0 || otherLining < 0) {
            return;
        }
        corridorEW(level, c, c.getX() + dir * ownLining, c.getX() + dir * (16 - otherLining), y,
                ownLining, otherLining);
    }

    static void corridorNS(LevelAccessor level, BlockPos c, int z0, int z1, int floorY,
                           int ownLining, int otherLining) {
        for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
            int distOwn = Math.abs(z - c.getZ());
            int distOther = 16 - distOwn;
            corridorSlice(level, c.getX(), z, floorY, true,
                    distOwn > ownLining && distOther > otherLining);
        }
    }

    static void corridorEW(LevelAccessor level, BlockPos c, int x0, int x1, int floorY,
                           int ownLining, int otherLining) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            int distOwn = Math.abs(x - c.getX());
            int distOther = 16 - distOwn;
            corridorSlice(level, x, c.getZ(), floorY, false,
                    distOwn > ownLining && distOther > otherLining);
        }
    }

    /**
     * Срез коридора. Внутри комнат — ТОЛЬКО воздух прохода: пол
     * и основание там уже есть от комнаты, перезапись камнем портила
     * bedrock-основание. Снаружи — полный закрытый тюбик: дно bedrock,
     * пол с bedrock-краями, крыша y+6 bedrock целиком.
     */
    static void corridorSlice(LevelAccessor level, int x, int z, int floorY, boolean alongZ,
                              boolean structural) {
        BlockState floor = floor();
        BlockState wall = wall();
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        for (int w = -3; w <= 3; w++) {
            int px = alongZ ? x + w : x;
            int pz = alongZ ? z : z + w;
            int aw = Math.abs(w);
            if (!structural) {
                if (aw <= 1) {
                    for (int dy = 1; dy <= 4; dy++) {
                        StructureHelper.set(level, new BlockPos(px, floorY + dy, pz),
                                Blocks.AIR.defaultBlockState());
                    }
                }
                continue;
            }
            StructureHelper.set(level, new BlockPos(px, floorY - 1, pz), bedrock);
            StructureHelper.set(level, new BlockPos(px, floorY, pz),
                    aw <= 2 ? floor : bedrock);
            for (int dy = 1; dy <= 4; dy++) {
                StructureHelper.set(level, new BlockPos(px, floorY + dy, pz),
                        aw <= 1 ? Blocks.AIR.defaultBlockState() : (aw == 2 ? wall : bedrock));
            }
            StructureHelper.set(level, new BlockPos(px, floorY + 5, pz),
                    aw <= 2 ? wall : bedrock);
            StructureHelper.set(level, new BlockPos(px, floorY + 6, pz), bedrock);
        }
    }
}

