package arcaneresearch.common.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

 




 
public final class StructureHelper {
    private StructureHelper() {
    }

      
    public static BlockState state(String id) {
        
        Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
        if (block == null || block == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        return block.defaultBlockState();
    }

      
    public static boolean replaceable(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.is(BlockTags.LEAVES);
    }

      
    public static boolean isSurfaceGround(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)
                || state.is(Blocks.STONE) || state.is(Blocks.SAND);
    }

    public static void set(LevelAccessor level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }

    public static void clearAbove(LevelAccessor level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            level.removeBlock(pos, false);
        }
    }
}
