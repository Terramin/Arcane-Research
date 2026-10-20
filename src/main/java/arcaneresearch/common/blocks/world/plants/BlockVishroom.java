package arcaneresearch.common.blocks.world.plants;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

// NEOFORGE_PORT (26.3): 1.20.1 BushBlock == 26.x VegetationBlock; 26.x BushBlock is the bonemeal-spreading bush.
public class BlockVishroom extends VegetationBlock {

    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 10.0, 13.0);

    public BlockVishroom(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_PURPLE)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .lightLevel(state -> 5)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {

        return state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.DIRT) ||
                state.is(Blocks.FARMLAND) ||
                state.is(Blocks.STONE) ||
                state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.MYCELIUM) ||
                state.is(BlockTags.BASE_STONE_OVERWORLD) ||
                state.is(BlockTags.HUGE_BROWN_MUSHROOM_CAN_PLACE_ON) ||
                state.is(BlockTags.HUGE_RED_MUSHROOM_CAN_PLACE_ON) ||
                state.is(BlockTags.DIRT);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return mayPlaceOn(belowState, level, below);
    }


    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {

        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            if (level.getRandom().nextInt(5) == 0) {
                living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {

            float xr = pos.getX() + 0.5f + (random.nextFloat() - random.nextFloat()) * 0.4f;
            float yr = pos.getY() + 0.3f;
            float zr = pos.getZ() + 0.5f + (random.nextFloat() - random.nextFloat()) * 0.4f;
            FXDispatcher.INSTANCE.drawWispyMotes(xr, yr, zr, 0.0, 0.0, 0.0, 10, 0.5f, 0.3f, 0.8f, 0.001f);
        }
    }
}