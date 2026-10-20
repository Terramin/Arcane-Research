package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
// NEOFORGE_PORT: ObfuscationReflectionHelper removed (plain reflection used instead).
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModBlockEntities;

public class TileBellows extends TileArcaneresearch {

    public float inflation = 1.0f;
    public int delay = 0;
    private boolean direction = false;
    private boolean firstRun = true;

    public TileBellows(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileBellows(BlockPos pos, BlockState state) {
        this(ModBlockEntities.BELLOWS.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileBellows tile) {
        if (!isEnabled(state)) return;

        if (tile.firstRun) {
            tile.inflation = 0.35f + level.getRandom().nextFloat() * 0.55f;
            tile.firstRun = false;
        }

        if (tile.inflation > 0.35f && !tile.direction) {
            tile.inflation -= 0.075f;
        }
        if (tile.inflation <= 0.35f && !tile.direction) {
            tile.direction = true;
        }

        if (tile.inflation < 1.0f && tile.direction) {
            tile.inflation += 0.025f;
        }
        if (tile.inflation >= 1.0f && tile.direction) {
            tile.direction = false;
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(),
                    SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS,
                    0.01f, 0.5f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2f,
                    false
            );
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileBellows tile) {
        if (!isEnabled(state)) return;

        tile.delay++;
        if (tile.delay >= 2) {
            tile.delay = 0;

            Direction facing = getFacing(state);
            if (facing != null) {
                BlockEntity targetTile = level.getBlockEntity(pos.relative(facing));

                if (targetTile instanceof AbstractFurnaceBlockEntity furnace) {
                    speedUpFurnace(furnace);
                }

                if (targetTile instanceof TileSmelter smelter) {
                    speedUpSmelter(smelter);
                }
            }
        }
    }

    private static void speedUpFurnace(AbstractFurnaceBlockEntity furnace) {
        // NEOFORGE_PORT: ObfuscationReflectionHelper removed in NeoForge (26.x is deobfuscated).
        // Plain reflection against official field names (cookingTimer/cookingTotalTime);
        // same fault-tolerant try/catch as before.
        try {
            var progressField = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingTimer");
            progressField.setAccessible(true);
            var totalField = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingTotalTime");
            totalField.setAccessible(true);
            int progress = (int) progressField.get(furnace);
            int total = (int) totalField.get(furnace);
            if (progress > 0 && progress < total - 1) {
                progressField.set(furnace, progress + 1);
            }
        } catch (Exception e) {

        }
    }

    private static void speedUpSmelter(TileSmelter smelter) {
        try {

            if (smelter.furnaceBurnTime > 0 && smelter.furnaceCookTime > 0 && smelter.furnaceCookTime < smelter.smeltTime) {
                smelter.furnaceCookTime++;
            }
        } catch (Exception e) {

        }
    }

    private static boolean isEnabled(BlockState state) {

        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
        }

        return true;
    }

    public static Direction getFacing(BlockState state) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    public static int getBellows(Level level, BlockPos pos, Direction[] directions) {
        int bellows = 0;
        for (Direction dir : directions) {
            BlockEntity tile = level.getBlockEntity(pos.relative(dir));
            if (tile instanceof TileBellows) {
                BlockState state = tile.getBlockState();
                Direction bellowsFacing = getFacing(state);

                if (bellowsFacing == dir.getOpposite() && isEnabled(state)) {
                    bellows++;
                }
            }
        }
        return bellows;
    }

    public AABB getCustomRenderBoundingBox() {
        return new AABB(
                worldPosition.getX() - 0.3, worldPosition.getY() - 0.3, worldPosition.getZ() - 0.3,
                worldPosition.getX() + 1.3, worldPosition.getY() + 1.3, worldPosition.getZ() + 1.3
        );
    }
}
