package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.blocks.aura.BlockObsidianTotem;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.common.world.biomes.BiomeHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObsidianTotemFeature extends Feature<NoneFeatureConfiguration> {

    private static final int RNG_RANGE          = 16;
    private static final int BIOME_CHECK_Y      = 50;
    private static final int MAX_FREE_SCAN      = 6;
    private static final int MIN_FREE_REQUIRED  = 2;
    private static final int MAX_PILLAR_HEIGHT  = 4;
    private static final int SET_BLOCK_FLAGS    = 2;
    private static final int MAX_ASPECT_ROLLS   = 24;

    private static final BlockObsidianTotem.TotemFace[] PILLAR_FACES = {
            BlockObsidianTotem.TotemFace.PILLAR_1,
            BlockObsidianTotem.TotemFace.PILLAR_2,
            BlockObsidianTotem.TotemFace.PILLAR_3,
            BlockObsidianTotem.TotemFace.PILLAR_4
    };

    private static final Map<BlockObsidianTotem.TotemFace, BlockState> TOTEM_STATES =
            new ConcurrentHashMap<>();

    private static volatile BlockState auraNodeState;

    public ObsidianTotemFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private static BlockState totemState(BlockObsidianTotem.TotemFace face) {
        return TOTEM_STATES.computeIfAbsent(face, f ->
                AuraBlocks.OBSIDIAN_TOTEM.get().defaultBlockState()
                        .setValue(BlockObsidianTotem.VARIANT, f));
    }

    private static BlockState auraNodeState() {
        BlockState s = auraNodeState;
        if (s == null) {
            s = AuraBlocks.AURA_NODE.get().defaultBlockState();
            auraNodeState = s;
        }
        return s;
    }

    private static BlockObsidianTotem.TotemFace randomFace(RandomSource random) {
        return PILLAR_FACES[random.nextInt(PILLAR_FACES.length)];
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level  = context.level();
        RandomSource  random = context.random();
        if (!ModConfig.generateStructures) return false;

        BlockPos origin = context.origin();

        // RNG-порядок сохранён: сначала X, потом Z
        int x = origin.getX() + random.nextInt(RNG_RANGE);
        int z = origin.getZ() + random.nextInt(RNG_RANGE);

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        cursor.set(x, BIOME_CHECK_Y, z);
        if (BiomeHandler.isBiomeBlacklisted(level.getBiome(cursor))) return false;

        int minBuild = level.getMinBuildHeight();
        int maxBuild = level.getMaxBuildHeight();

        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        if (topY <= minBuild + 1 || topY + 6 >= maxBuild) return false;

        cursor.set(x, topY, z);
        if (!isTotemGround(level.getBlockState(cursor))) return false;

        // Свободное место над землёй (до MAX_FREE_SCAN)
        int free = 0;
        int scanY = topY + 1;
        while (free < MAX_FREE_SCAN) {
            cursor.set(x, scanY, z);
            if (!level.isEmptyBlock(cursor)) break;
            free++;
            scanY++;
        }
        if (free < MIN_FREE_REQUIRED) return false;

        // База
        cursor.set(x, topY, z);
        level.setBlock(cursor, totemState(BlockObsidianTotem.TotemFace.BASE), SET_BLOCK_FLAGS);

        // Столб + возможный узел
        int height = 1;
        boolean capped = false;
        List<Aspect> primals = Aspect.getPrimalAspects();

        while (height <= MAX_PILLAR_HEIGHT && height <= free - 1 && !capped) {
            cursor.set(x, topY + height, z);
            if (!level.isEmptyBlock(cursor)) break;

            if (height > 1 && random.nextInt(4) == 0) {
                placeNode(level, random, cursor, primals);
                capped = true;
            } else {
                level.setBlock(cursor, totemState(randomFace(random)), SET_BLOCK_FLAGS);
                height++;
            }
        }

        // Если не закаплено через 4 столба — ставим узел на 5-м
        if (!capped && height > MAX_PILLAR_HEIGHT) {
            cursor.set(x, topY + 5, z);
            if (cursor.getY() < maxBuild && level.isEmptyBlock(cursor)) {
                placeNode(level, random, cursor, primals);
                capped = true;
            }
        }

        return capped;
    }

    private static void placeNode(WorldGenLevel level, RandomSource random,
                                  BlockPos pos, List<Aspect> primals) {
        level.setBlock(pos, auraNodeState(), SET_BLOCK_FLAGS);
        if (!(level.getBlockEntity(pos) instanceof AuraNodeTile node)) return;

        int roll = random.nextInt(100);
        NodeType type = roll < 85 ? NodeType.NORMAL
                : (roll < 95 ? NodeType.UNSTABLE : NodeType.HUNGRY);

        AspectList aspects = new AspectList();
        int want = 2 + random.nextInt(3);
        int placed = 0;
        int guard = 0;
        while (placed < want && guard++ < MAX_ASPECT_ROLLS) {
            Aspect aspect = primals.get(random.nextInt(primals.size()));
            if (aspect != null && aspects.getAmount(aspect) <= 0) {
                aspects.add(aspect, 15 + random.nextInt(26));
                placed++;
            }
        }

        node.setNodeType(type);
        node.setAspects(aspects);
        node.setChanged();
    }

    private static boolean isTotemGround(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(Blocks.STONE)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE);
    }
}