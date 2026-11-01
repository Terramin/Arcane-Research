package arcaneresearch.common.tiles.misc;

import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        RandomSource random = level.random;
        MapColor mapColor = state.getMapColor(level, pos);
        int color = mapColor.col;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.45;
        double cz = pos.getZ() + 0.5;

        FXDispatcher.INSTANCE.drawNitorFlames(
                cx + random.nextGaussian() * 0.025,
                cy + random.nextGaussian() * 0.025,
                cz + random.nextGaussian() * 0.025,
                random.nextGaussian() * 0.0025,
                random.nextFloat() * 0.06,
                random.nextGaussian() * 0.0025,
                color, 0);

        if (tile.count++ % 10 == 0) {
            FXDispatcher.INSTANCE.drawNitorCore(
                    pos.getX() + 0.5,
                    pos.getY() + 0.49,
                    pos.getZ() + 0.5,
                    0.0, 0.0, 0.0);
        }
    }
}