package arcaneresearch.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.api.aura.AuraHelper;

public class BlockLeavesTC extends LeavesBlock {

    private final boolean glows;

    public BlockLeavesTC(Properties properties, boolean glows) {
        super(net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
        this.glows = glows;
    }

    public static BlockLeavesTC createGreatwood(BlockBehaviour.Properties props) {
        return new BlockLeavesTC(props
                .mapColor(MapColor.PLANT)
                .strength(0.2f)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos, aabb) -> false),
                false);
    }

    public static BlockLeavesTC createSilverwood(BlockBehaviour.Properties props) {
        return new BlockLeavesTC(props
                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                .strength(0.2f)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .lightLevel(state -> 4)
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos, aabb) -> false),
                true);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {

        BlockState state = super.getStateForPlacement(context);
        if (state != null && context.getPlayer() != null) {
            return state.setValue(PERSISTENT, Boolean.TRUE);
        }
        return state;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 60;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (glows && AuraHelper.getVis(level, pos) < AuraHelper.getAuraBase(level, pos)) {
            AuraHelper.addVis(level, pos, 0.01f);
        }
    }
}
