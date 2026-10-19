package arcaneresearch.common.blocks.world.taint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
// NEOFORGE_PORT: IPlantable removed in NeoForge (see instanceof BushBlock below).
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeed;
import arcaneresearch.common.lib.utils.BlockUtils;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.common.world.aura.AuraHandler;
import arcaneresearch.init.ModBlocks;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class TaintHelper {

    private static final ConcurrentHashMap<ResourceKey<Level>, ArrayList<BlockPos>> taintSeeds = new ConcurrentHashMap<>();

    public static void addTaintSeed(Level level, BlockPos pos) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<BlockPos> locs = taintSeeds.computeIfAbsent(dim, k -> new ArrayList<>());
        if (!locs.contains(pos)) {
            locs.add(pos);
        }
    }

    public static void removeTaintSeed(Level level, BlockPos pos) {
        ResourceKey<Level> dim = level.dimension();
        ArrayList<BlockPos> locs = taintSeeds.get(dim);
        if (locs != null && !locs.isEmpty()) {
            locs.remove(pos);
        }
    }

    public static boolean isNearTaintSeed(Level level, BlockPos pos) {
        double area = ModConfig.taintSpreadArea * ModConfig.taintSpreadArea;
        ResourceKey<Level> dim = level.dimension();
        ArrayList<BlockPos> locs = taintSeeds.get(dim);

        if (locs != null && !locs.isEmpty()) {
            for (BlockPos p : new ArrayList<>(locs)) {
                if (p.distSqr(pos) <= area) {

                    if (EntityUtils.getEntitiesInRange(level, p, 1.0, EntityTaintSeed.class).isEmpty()) {
                        removeTaintSeed(level, p);
                        return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        double area = ModConfig.taintSpreadArea * ModConfig.taintSpreadArea;
        double fringe = ModConfig.taintSpreadArea * 0.8 * (ModConfig.taintSpreadArea * 0.8);
        ResourceKey<Level> dim = level.dimension();
        ArrayList<BlockPos> locs = taintSeeds.get(dim);

        if (locs != null && !locs.isEmpty()) {
            for (BlockPos p : new ArrayList<>(locs)) {
                double d = p.distSqr(pos);
                if (d < area && d > fringe) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void spreadFibres(Level level, BlockPos pos) {
        spreadFibres(level, pos, false);
    }

    public static void spreadFibres(Level level, BlockPos pos, boolean ignore) {
        if (level.isClientSide()) return;

        if (!ignore && ModConfig.wussMode) {
            return;
        }

        float mod = 0.001f + AuraHandler.getFluxSaturation(level, pos) * 2.0f;
        if (!ignore && level.getRandom().nextFloat() > ModConfig.taintSpreadRate / 100.0f * mod) {
            return;
        }

        if (!isNearTaintSeed(level, pos)) {
            return;
        }

        int xx = pos.getX() + level.getRandom().nextInt(3) - 1;
        int yy = pos.getY() + level.getRandom().nextInt(3) - 1;
        int zz = pos.getZ() + level.getRandom().nextInt(3) - 1;
        BlockPos target = new BlockPos(xx, yy, zz);

        if (target.equals(pos)) {
            return;
        }

        BlockState bs = level.getBlockState(target);
        Block block = bs.getBlock();
        float hardness = bs.getDestroySpeed(level, target);

        if (hardness < 0.0f || hardness > 10.0f) {
            return;
        }

        MapColor material = bs.getMapColor(level, target);

        if (!bs.is(net.minecraft.tags.BlockTags.LEAVES) &&
                bs.getFluidState().isEmpty() &&
                (level.isEmptyBlock(target) ||
                        bs.canBeReplaced() ||
                        block instanceof FlowerBlock ||
                        block instanceof net.minecraft.world.level.block.BushBlock) &&
                BlockUtils.isAdjacentToSolidBlock(level, target) &&
                !BlockTaintFibre.isOnlyAdjacentToTaint(level, target)) {

            if (ModBlocks.TAINT_FIBRE != null) {
                level.setBlockAndUpdate(target, ModBlocks.TAINT_FIBRE.get().defaultBlockState());
                level.blockEvent(target, ModBlocks.TAINT_FIBRE.get(), 1, 0);
                AuraHelper.drainFlux(level, target, 0.01f, false);
            }
            return;
        }

        if (bs.is(net.minecraft.tags.BlockTags.LEAVES)) {
            Direction face = null;
            if (ModBlocks.TAINT_LOG != null) {
                face = BlockUtils.getFaceBlockTouching(level, target, ModBlocks.TAINT_LOG.get());
            }
            if (level.getRandom().nextFloat() < 0.6 && face != null && ModBlocks.TAINT_FEATURE != null) {

                level.setBlockAndUpdate(target, ModBlocks.TAINT_FEATURE.get().defaultBlockState()
                        .setValue(BlockTaintFeature.FACING, face.getOpposite()));
            } else if (ModBlocks.TAINT_FIBRE != null) {
                level.setBlockAndUpdate(target, ModBlocks.TAINT_FIBRE.get().defaultBlockState());
                level.blockEvent(target, ModBlocks.TAINT_FIBRE.get(), 1, 0);
                AuraHelper.drainFlux(level, target, 0.01f, false);
            }
            return;
        }

        if (BlockTaintFibre.isHemmedByTaint(level, target) && hardness < 5.0f) {

            if (bs.is(net.minecraft.tags.BlockTags.LOGS)
                    && !(block instanceof ITaintBlock)) {
                if (ModBlocks.TAINT_LOG != null) {

                    Direction.Axis axis = Direction.Axis.Y;
                    if (bs.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS)) {
                        axis = bs.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS);
                    }
                    level.setBlockAndUpdate(target, ModBlocks.TAINT_LOG.get().defaultBlockState()
                            .setValue(BlockTaintLog.AXIS, axis));
                }
                return;
            }

            if (block == Blocks.RED_MUSHROOM_BLOCK || block == Blocks.BROWN_MUSHROOM_BLOCK ||
                    block == Blocks.MUSHROOM_STEM ||
                    block == Blocks.CACTUS || block == Blocks.SPONGE || block == Blocks.WET_SPONGE ||
                    block == Blocks.PUMPKIN || block == Blocks.CARVED_PUMPKIN || block == Blocks.JACK_O_LANTERN ||
                    block == Blocks.MELON ||
                    bs.is(net.minecraft.tags.BlockTags.CORAL_BLOCKS) ||
                    bs.is(net.minecraft.tags.BlockTags.CORALS) ||
                    bs.is(net.minecraft.tags.BlockTags.CORAL_PLANTS) ||
                    material == MapColor.COLOR_GREEN ||
                    material == MapColor.PLANT ||
                    material == MapColor.WOOD) {
                if (ModBlocks.TAINT_CRUST != null) {
                    level.setBlockAndUpdate(target, ModBlocks.TAINT_CRUST.get().defaultBlockState());
                    level.blockEvent(target, ModBlocks.TAINT_CRUST.get(), 1, 0);
                    AuraHelper.drainFlux(level, target, 0.01f, false);
                }
                return;
            }

            if (material == MapColor.SAND || material == MapColor.DIRT ||
                    material == MapColor.GRASS || material == MapColor.CLAY ||
                    block == Blocks.GRAVEL || block == Blocks.SUSPICIOUS_GRAVEL ||
                    block == Blocks.DIRT_PATH || block == Blocks.FARMLAND ||
                    block == Blocks.MUD || block == Blocks.SOUL_SAND ||
                    block == Blocks.SOUL_SOIL || block == Blocks.MYCELIUM ||
                    block == Blocks.PODZOL) {
                if (ModBlocks.TAINT_SOIL != null) {
                    level.setBlockAndUpdate(target, ModBlocks.TAINT_SOIL.get().defaultBlockState());
                    level.blockEvent(target, ModBlocks.TAINT_SOIL.get(), 1, 0);
                    AuraHelper.drainFlux(level, target, 0.01f, false);
                }
                return;
            }

            if (material == MapColor.STONE ||
                    block == Blocks.NETHERRACK || block == Blocks.END_STONE ||
                    block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN ||
                    bs.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) {
                if (ModBlocks.TAINT_ROCK != null) {
                    level.setBlockAndUpdate(target, ModBlocks.TAINT_ROCK.get().defaultBlockState());
                    level.blockEvent(target, ModBlocks.TAINT_ROCK.get(), 1, 0);
                    AuraHelper.drainFlux(level, target, 0.01f, false);
                }
                return;
            }
        }

        if (ModBlocks.TAINT_SOIL != null && ModBlocks.TAINT_ROCK != null &&
                (block == ModBlocks.TAINT_SOIL.get() || block == ModBlocks.TAINT_ROCK.get())
                && level.isEmptyBlock(target.above())
                && AuraHelper.getFlux(level, target) >= 5.0f
                && level.getRandom().nextFloat() < ModConfig.taintSpreadRate / 100.0f * 0.33f
                && isAtTaintSeedEdge(level, target)) {
            EntityTaintSeed seed = new EntityTaintSeed(level);
            seed.teleportTo(target.getX() + 0.5, target.above().getY(), target.getZ() + 0.5);
            seed.setYRot(level.getRandom().nextFloat() * 360.0f); seed.setXRot(0.0f);;
            if (seed.checkSpawnRules(level, net.minecraft.world.entity.EntitySpawnReason.EVENT)) {
                AuraHelper.drainFlux(level, target, 5.0f, false);
                level.addFreshEntity(seed);
            }
        }
    }

    public static void clearDimension(ResourceKey<Level> dimension) {
        taintSeeds.remove(dimension);
    }

    public static void clearAll() {
        taintSeeds.clear();
    }
}
