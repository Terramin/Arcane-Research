package arcaneresearch.common.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.common.world.biomes.BiomeHandler;

import java.util.List;

/**
 * Wild aura node spawner, ported from TC4 generateWildNodes/createRandomNodeAt.
 * Placed via datapack (rarity in the placed feature); the node gets a random
 * type, modifier and biome-flavoured aspects.
 */
// NEOFORGE_PORT: Feature is now an interface (no generics, no FeaturePlaceContext /
// NoneFeatureConfiguration): implements Feature with MapCodec codec() + place(
// WorldGenLevel, ChunkGenerator, RandomSource, BlockPos). Codec is MapCodec.unit(...):
// features are placed from code with fixed params (never deserialized from datapacks),
// registry-id dispatch resolves the registered instance on load.
public class AuraNodeStructure implements Feature {
    public static final MapCodec<AuraNodeStructure> CODEC =
            MapCodec.unit(new AuraNodeStructure());

    public AuraNodeStructure() {
    }

    @Override
    public MapCodec<AuraNodeStructure> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        BlockPos pos = origin;
        for (int i = 0; i < 6; i++) {
            if (level.getBlockState(pos).isAir() || StructureHelper.replaceable(level, pos)) {
                break;
            }
            pos = pos.above();
        }
        if (!level.getBlockState(pos).isAir() && !StructureHelper.replaceable(level, pos)) {
            return false;
        }
        StructureHelper.set(level, pos, StructureHelper.state("arcaneresearch:aura_node"));
        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof AuraNodeTile node)) {
            return false;
        }
        Holder<Biome> holder = level.getBiome(pos);
        node.setNodeType(rollType(random));
        node.setNodeModifier(rollModifier(random));
        node.setAspects(rollAspects(holder, random));
        node.setChanged();
        return true;
    }

    static NodeType rollType(RandomSource random) {
        if (random.nextInt(12) == 0) {
            switch (random.nextInt(10)) {
                case 0:
                case 1:
                case 2:
                    return NodeType.DARK;
                case 3:
                case 4:
                case 5:
                    return NodeType.UNSTABLE;
                case 6:
                case 7:
                case 8:
                    return NodeType.PURE;
                default:
                    return NodeType.HUNGRY;
            }
        }
        return NodeType.NORMAL;
    }

    static NodeModifier rollModifier(RandomSource random) {
        if (random.nextInt(6) == 0) {
            switch (random.nextInt(3)) {
                case 0:
                    return NodeModifier.BRIGHT;
                case 1:
                    return NodeModifier.PALE;
                default:
                    return NodeModifier.FADING;
            }
        }
        return null;
    }

    static AspectList rollAspects(Holder<Biome> holder, RandomSource random) {
        int aura = 10 + Math.round(BiomeHandler.getAuraModifier(holder) * 30.0F);
        int value = aura / 2 + random.nextInt(aura / 2 + 1);
        AspectList aspects = new AspectList();
        Aspect primary = BiomeHandler.getRandomBiomeTag(holder, random);
        if (primary == null) {
            List<Aspect> primals = Aspect.getPrimalAspects();
            primary = primals.get(random.nextInt(primals.size()));
        }
        aspects.add(primary, value);
        if (random.nextBoolean()) {
            List<Aspect> primals = Aspect.getPrimalAspects();
            aspects.add(primals.get(random.nextInt(primals.size())), Math.max(1, value / 2));
        }
        return aspects;
    }
}
