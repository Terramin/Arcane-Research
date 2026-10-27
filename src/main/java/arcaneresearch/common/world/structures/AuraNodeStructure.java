package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
import arcaneresearch.common.world.biomes.BiomeHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AuraNodeStructure extends Feature<NoneFeatureConfiguration> {
    public AuraNodeStructure(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockState nodeState = StructureHelper.state("arcaneresearch:aura_node");
        if (nodeState.isAir()) {
            Arcaneresearch.LOGGER.warn("AuraNodeStructure: aura_node block not registered");
            return false;
        }

        BlockPos pos = origin;
        boolean canPlace = false;
        for (int i = 0; i < 6; i++) {
            if (StructureHelper.replaceable(level, pos)) {
                canPlace = true;
                break;
            }
            pos = pos.above();
        }
        if (!canPlace && !StructureHelper.replaceable(level, pos)) {
            return false;
        }

        StructureHelper.set(level, pos, nodeState);
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
            return switch (random.nextInt(10)) {
                case 0, 1, 2 -> NodeType.DARK;
                case 3, 4, 5 -> NodeType.UNSTABLE;
                case 6, 7, 8 -> NodeType.PURE;
                default -> NodeType.HUNGRY;
            };
        }
        return NodeType.NORMAL;
    }

    static NodeModifier rollModifier(RandomSource random) {
        if (random.nextInt(6) == 0) {
            return switch (random.nextInt(3)) {
                case 0 -> NodeModifier.BRIGHT;
                case 1 -> NodeModifier.PALE;
                default -> NodeModifier.FADING;
            };
        }
        return null;
    }

    static AspectList rollAspects(Holder<Biome> holder, RandomSource random) {
        List<Aspect> primals = Aspect.getPrimalAspects();
        if (primals == null || primals.isEmpty()) {
            return new AspectList();
        }

        int auraMod = 0;
        Aspect primary = null;
        if (holder != null) {
            try {
                auraMod = Math.round(BiomeHandler.getAuraModifier(holder) * 30.0F);
                primary = BiomeHandler.getRandomBiomeTag(holder, random);
            } catch (Exception ignored) {
            }
        }

        int aura = Math.max(6, 10 + auraMod);
        int half = aura / 2;
        int value = half + random.nextInt(half + 1);

        if (primary == null) {
            primary = primals.get(random.nextInt(primals.size()));
        }

        AspectList aspects = new AspectList();
        aspects.add(primary, value);
        if (random.nextBoolean()) {
            aspects.add(primals.get(random.nextInt(primals.size())), Math.max(1, value / 2));
        }
        return aspects;
    }
}