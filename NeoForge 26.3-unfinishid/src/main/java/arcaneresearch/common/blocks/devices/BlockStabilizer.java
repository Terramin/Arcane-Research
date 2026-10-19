package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.crafting.IInfusionStabiliserExt;
import arcaneresearch.common.tiles.devices.TileStabilizer;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockStabilizer extends Block implements EntityBlock, IInfusionStabiliserExt {

    private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    private static final float STABILIZATION_BONUS = 0.25f;

    public BlockStabilizer(BlockBehaviour.Properties props) {
        super(props

                .mapColor(MapColor.STONE)
                .strength(2.0f, 20.0f)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.IMMOVEABLE)
                .noOcclusion());
    }

    public static int colorMultiplier(int charge) {
        float f = charge / 15.0f;
        float f1 = f * 0.5f + 0.5f;
        if (charge == 0) {
            f1 = 0.3f;
        }
        int i = net.minecraft.util.Mth.clamp((int) (f1 * 255.0f), 0, 255);
        return 0xFF000000 | i << 16 | i << 8 | i;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public float getStabilizationAmount(Level level, BlockPos pos) {

        return STABILIZATION_BONUS;
    }

    public boolean canStabilizeInfusion(Level level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canStabaliseInfusion(Level level, BlockPos pos) {

        return canStabilizeInfusion(level, pos);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileStabilizer(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.STABILIZER.get()) {
            if (level.isClientSide()) {
                return (lvl, p, st, be) -> TileStabilizer.clientTick(lvl, p, st, (TileStabilizer) be);
            }
            return (lvl, p, st, be) -> TileStabilizer.serverTick(lvl, p, st, (TileStabilizer) be);
        }
        return null;
    }
}
