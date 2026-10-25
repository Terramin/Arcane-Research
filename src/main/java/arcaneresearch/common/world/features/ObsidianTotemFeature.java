package arcaneresearch.common.world.features;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
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
public class ObsidianTotemFeature extends Feature<NoneFeatureConfiguration> {
    public ObsidianTotemFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (!ModConfig.generateStructures) {
            return false;
        }
        BlockPos origin = context.origin();
        int x = origin.getX() + random.nextInt(16);
        int z = origin.getZ() + random.nextInt(16);
        if (BiomeHandler.isBiomeBlacklisted(
                level.getBiome(new BlockPos(x, 50, z)))) {
            return false;
        }
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        if (topY <= level.getMinBuildHeight() + 1 || topY + 6 >= level.getMaxBuildHeight()) {
            return false;
        }
        BlockPos groundPos = new BlockPos(x, topY, z);
        if (!isTotemGround(level.getBlockState(groundPos))) {
            return false;
        }
        int free = 0;
        while (free < 6 && level.isEmptyBlock(groundPos.above(free + 1))) {
            free++;
        }
        if (free < 2) {
            return false;
        }
        level.setBlock(groundPos, AuraBlocks.OBSIDIAN_TOTEM.get().defaultBlockState()
                .setValue(BlockObsidianTotem.VARIANT, BlockObsidianTotem.TotemFace.BASE), 2);
        int height = 1;
        boolean capped = false;
        while (height <= 4 && height <= free - 1 && !capped) {
            BlockPos pillarPos = groundPos.above(height);
            if (!level.isEmptyBlock(pillarPos)) {
                break;
            }
            if (height > 1 && random.nextInt(4) == 0) {
                placeNode(level, random, pillarPos);
                capped = true;
            } else {
                level.setBlock(pillarPos, AuraBlocks.OBSIDIAN_TOTEM.get().defaultBlockState()
                        .setValue(BlockObsidianTotem.VARIANT, randomFace(random)), 2);
                height++;
            }
        }
        if (!capped && height > 4) {
            BlockPos capPos = groundPos.above(5);
            if (capPos.getY() < level.getMaxBuildHeight() && level.isEmptyBlock(capPos)) {
                placeNode(level, random, capPos);
                capped = true;
            }
        }
        return capped;
    }
    private BlockObsidianTotem.TotemFace randomFace(RandomSource random) {
        BlockObsidianTotem.TotemFace[] faces = {
                BlockObsidianTotem.TotemFace.PILLAR_1,
                BlockObsidianTotem.TotemFace.PILLAR_2,
                BlockObsidianTotem.TotemFace.PILLAR_3,
                BlockObsidianTotem.TotemFace.PILLAR_4};
        return faces[random.nextInt(faces.length)];
    }
    private void placeNode(WorldGenLevel level, RandomSource random, BlockPos pos) {
        level.setBlock(pos, AuraBlocks.AURA_NODE.get().defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AuraNodeTile node)) {
            return;
        }
        int roll = random.nextInt(100);
        NodeType type = roll < 85 ? NodeType.NORMAL
                : (roll < 95 ? NodeType.UNSTABLE : NodeType.HUNGRY);
        List<Aspect> primals = Aspect.getPrimalAspects();
        AspectList aspects = new AspectList();
        int want = 2 + random.nextInt(3);
        int placed = 0;
        int guard = 0;
        while (placed < want && guard++ < 24) {
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
    private boolean isTotemGround(BlockState state) {
        if (state.is(BlockTags.DIRT)) {
            return true;
        }
        if (state.is(BlockTags.SAND)) {
            return true;
        }
        return state.is(Blocks.STONE) || state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE);
    }
}
