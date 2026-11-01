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
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.common.tiles.essentia.TileSmelter;
import arcaneresearch.init.ModBlockEntities;

import java.lang.reflect.Field;

public class TileBellows extends TileArcaneresearch {
    private static final String FURNACE_COOK_TIME_FIELD = "f_58318_";
    private static final String FURNACE_COOK_TIME_TOTAL_FIELD = "f_58319_";

    private static Field furnaceCookTimeField;
    private static Field furnaceCookTimeTotalField;
    private static boolean reflectionFailed = false;

    private AABB renderBox;

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
        if (level == null || !isEnabled(state)) return;

        if (tile.firstRun) {
            tile.inflation = 0.35f + level.random.nextFloat() * 0.55f;
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
                    0.01f, 0.5f + (level.random.nextFloat() - level.random.nextFloat()) * 0.2f,
                    false
            );
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileBellows tile) {
        if (level == null || !isEnabled(state)) return;

        if (++tile.delay < 2) return;
        tile.delay = 0;

        Direction facing = getFacing(state);
        BlockEntity targetTile = level.getBlockEntity(pos.relative(facing));

        if (targetTile instanceof AbstractFurnaceBlockEntity furnace) {
            speedUpFurnace(furnace);
        }

        if (targetTile instanceof TileSmelter smelter) {
            speedUpSmelter(smelter);
        }
    }

    private static void speedUpFurnace(AbstractFurnaceBlockEntity furnace) {
        if (reflectionFailed) return;

        if (furnaceCookTimeField == null || furnaceCookTimeTotalField == null) {
            try {
                furnaceCookTimeField = ObfuscationReflectionHelper.findField(
                        AbstractFurnaceBlockEntity.class, FURNACE_COOK_TIME_FIELD);
                furnaceCookTimeTotalField = ObfuscationReflectionHelper.findField(
                        AbstractFurnaceBlockEntity.class, FURNACE_COOK_TIME_TOTAL_FIELD);
                furnaceCookTimeField.setAccessible(true);
                furnaceCookTimeTotalField.setAccessible(true);
            } catch (Throwable t) {
                reflectionFailed = true;
                return;
            }
        }

        try {
            int progress = furnaceCookTimeField.getInt(furnace);
            int total = furnaceCookTimeTotalField.getInt(furnace);

            if (progress > 0 && progress < total - 1) {
                furnaceCookTimeField.setInt(furnace, progress + 1);
            }
        } catch (Throwable t) {
            reflectionFailed = true;
        }
    }

    private static void speedUpSmelter(TileSmelter smelter) {
        if (smelter.furnaceBurnTime > 0
                && smelter.furnaceCookTime > 0
                && smelter.furnaceCookTime < smelter.smeltTime) {
            smelter.furnaceCookTime++;
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
        if (level == null || directions == null) return 0;

        int bellows = 0;

        for (Direction dir : directions) {
            BlockPos bellowsPos = pos.relative(dir);

            if (!(level.getBlockEntity(bellowsPos) instanceof TileBellows)) continue;

            BlockState state = level.getBlockState(bellowsPos);

            if (!isEnabled(state)) continue;
            if (getFacing(state) != dir.getOpposite()) continue;

            bellows++;
        }

        return bellows;
    }

    public AABB getCustomRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX() - 0.3, worldPosition.getY() - 0.3, worldPosition.getZ() - 0.3,
                    worldPosition.getX() + 1.3, worldPosition.getY() + 1.3, worldPosition.getZ() + 1.3
            );
        }

        return renderBox;
    }
}