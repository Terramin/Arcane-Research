package arcaneresearch.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

public class TileNitor extends TileArcaneresearch {

    private int count = 0;

    public TileNitor(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileNitor(BlockPos pos, BlockState state) {
        this(ModBlockEntities.NITOR.get(), pos, state);
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileNitor tile) {
        int color = state.getMapColor(level, pos).col;
        FXDispatcher.INSTANCE.drawNitorFlames(
                pos.getX() + 0.5f + level.getRandom().nextGaussian() * 0.025,
                pos.getY() + 0.45f + level.getRandom().nextGaussian() * 0.025,
                pos.getZ() + 0.5f + level.getRandom().nextGaussian() * 0.025,
                level.getRandom().nextGaussian() * 0.0025,
                level.getRandom().nextFloat() * 0.06,
                level.getRandom().nextGaussian() * 0.0025,
                color, 0);
        if (tile.count++ % 10 == 0) {
            FXDispatcher.INSTANCE.drawNitorCore(pos.getX() + 0.5f, pos.getY() + 0.49f, pos.getZ() + 0.5f, 0.0, 0.0, 0.0);
        }
    }
}
