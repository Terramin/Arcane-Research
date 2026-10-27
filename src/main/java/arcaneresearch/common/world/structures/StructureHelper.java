package arcaneresearch.common.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class StructureHelper {
    private StructureHelper() {
    }

    public static BlockState state(String id) {
        if (id == null || id.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        ResourceLocation loc;
        try {
            loc = ResourceLocation.tryParse(id);
        } catch (Exception ignored) {
            return Blocks.AIR.defaultBlockState();
        }
        if (loc == null) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = ForgeRegistries.BLOCKS.getValue(loc);
        if (block == null || block == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        return block.defaultBlockState();
    }

    public static boolean replaceable(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES);
    }

    public static boolean isSurfaceGround(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)
                || state.is(Blocks.STONE) || state.is(Blocks.SAND);
    }

    public static void set(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level == null || pos == null || state == null) {
            return;
        }
        level.setBlock(pos, state, 3);
    }

    public static void clearAbove(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null) {
            return;
        }
        if (!level.getBlockState(pos).isAir()) {
            level.removeBlock(pos, false);
        }
    }
}