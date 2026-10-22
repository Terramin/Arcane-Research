package thaumictinker.gas;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

import arcaneresearch.client.fx.FXDispatcher;

 


 
public class NitorGasBlock extends GasBlock {

    public static final BooleanProperty STRONG = BooleanProperty.create("strong");

    public NitorGasBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STRONG, false));
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(STRONG) ? 15 : 12;
    }

    public static boolean isCarried(List<Player> players) {
        for (Player player : players) {
            if (GasFeature.carriesLight(player)) {
                return true;
            }
        }
        return false;
    }

    public static int tickRate(Level level) {
        return level.dimension() == Level.NETHER ? 60 : 20;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STRONG);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, tickRate(level));
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int distance = state.getValue(STRONG) ? 6 : 1;
        AABB area = new AABB(pos.getX() - distance, pos.getY() - distance, pos.getZ() - distance,
                pos.getX() + distance, pos.getY() + distance, pos.getZ() + distance);
        if (isCarried(level.getEntitiesOfClass(Player.class, area))) {
            level.scheduleTick(pos, this, tickRate(level));
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() < 0.03F) {
            FXDispatcher.INSTANCE.sparkle(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                    1.0F, 0.8F, 0.35F);
        }
    }
}
