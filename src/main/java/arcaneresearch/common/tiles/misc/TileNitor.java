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
        double cy = pos.getY() + NitorRules.FLAME_Y;
        double cz = pos.getZ() + 0.5;

        FXDispatcher.INSTANCE.drawNitorFlames(
                cx + NitorRules.flameOffset(random.nextGaussian()),
                cy + NitorRules.flameOffset(random.nextGaussian()),
                cz + NitorRules.flameOffset(random.nextGaussian()),
                NitorRules.flameVelocity(random.nextGaussian()),
                NitorRules.flameRise(random.nextFloat()),
                NitorRules.flameVelocity(random.nextGaussian()),
                color, 0);

        if (NitorRules.shouldDrawCore(tile.count++)) {
            FXDispatcher.INSTANCE.drawNitorCore(
                    pos.getX() + 0.5,
                    pos.getY() + NitorRules.CORE_Y,
                    pos.getZ() + 0.5,
                    0.0, 0.0, 0.0);
        }
    }
}