package thaumictinker.gas;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

 


 
public abstract class GasBlock extends Block {

    protected GasBlock(Properties properties) {
        super(properties);
    }

    public static BlockBehaviour.Properties gas(BlockBehaviour.Properties properties) {
        return properties
                .mapColor(MapColor.NONE)
                .noCollission()
                .noOcclusion()
                .noLootTable()
                .replaceable()
                .randomTicks()
                .instabreak()
                .sound(SoundType.EMPTY)
                .pushReaction(PushReaction.DESTROY)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .isRedstoneConductor((state, level, pos) -> false);
    }

    public static boolean canHoldGas(BlockState state) {
        return state.isAir() || state.getBlock() instanceof GasBlock;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
