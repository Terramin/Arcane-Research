package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TileVoidSiphon extends TileArcaneresearchInventory implements WorldlyContainer {
    public static final int PROGRESS_REQUIRED = 2000;
    private static final int[] SLOTS = {0};

    public int progress = 0;

    private int counter = 0;

    public TileVoidSiphon(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
    }

    public TileVoidSiphon(BlockPos pos, BlockState state) {
        this(ModBlockEntities.VOID_SIPHON.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileVoidSiphon tile) {
        tile.counter++;

        if (level == null || !tile.isEnabled(state)) return;
        if ((tile.counter % 20) != 0 || tile.progress >= PROGRESS_REQUIRED) return;

        ItemStack stack = tile.getItem(0);
        int maxStack = stack.isEmpty() ? 0 : stack.getMaxStackSize();

        if (!stack.isEmpty()
                && (!stack.is(ModItems.VOID_SEED.get()) || stack.getCount() >= maxStack)) {
            return;
        }

        List<EntityFluxRift> rifts = tile.getValidRifts();
        boolean didWork = false;

        for (EntityFluxRift rift : rifts) {
            int riftSize = rift.getRiftSize();
            double d = Math.sqrt(riftSize);

            tile.progress += (int) d;
            rift.setRiftStability(rift.getRiftStability() - (float) (d / 15.0));

            if (level.random.nextInt(33) == 0) {
                rift.setRiftSize(riftSize - 1);
            }

            if (d >= 1.0) {
                didWork = true;
            }
        }

        if (didWork && (tile.counter % 40) == 0) {
            level.blockEvent(pos, state.getBlock(), 5, tile.counter);
        }

        while (tile.progress >= PROGRESS_REQUIRED) {
            stack = tile.getItem(0);

            if (!stack.isEmpty()
                    && (!stack.is(ModItems.VOID_SEED.get()) || stack.getCount() >= stack.getMaxStackSize())) {
                break;
            }

            tile.progress -= PROGRESS_REQUIRED;

            if (stack.isEmpty()) {
                tile.setItem(0, new ItemStack(ModItems.VOID_SEED.get()));
            } else {
                stack.grow(1);
            }

            tile.syncTile(false);
            tile.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileVoidSiphon tile) {
    }

    private List<EntityFluxRift> getValidRifts() {
        if (level == null) return List.of();

        List<EntityFluxRift> rifts = EntityUtils.getEntitiesInRange(
                level, worldPosition, 8.0, EntityFluxRift.class);

        if (rifts.isEmpty()) return List.of();

        List<EntityFluxRift> result = new ArrayList<>(rifts.size());

        double sx = worldPosition.getX() + 0.5;
        double sy = worldPosition.getY() + 1.0;
        double sz = worldPosition.getZ() + 0.5;

        for (EntityFluxRift rift : rifts) {
            if (rift.isRemoved()) continue;
            if (rift.getRiftSize() < 2) continue;

            double dx = rift.getX() - sx;
            double dy = rift.getY() - sy;
            double dz = rift.getZ() - sz;

            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (len < 1.0e-6) {
                result.add(rift);
                continue;
            }

            double nx = sx + dx / len;
            double ny = sy + dy / len;
            double nz = sz + dz / len;

            if (EntityUtils.canEntityBeSeen(rift, nx, ny, nz)) {
                result.add(rift);
            }
        }

        return result;
    }

    private boolean isEnabled(BlockState state) {
        if (level == null) return false;

        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
        }

        return !level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 5) {
            if (level != null && level.isClientSide) {
                spawnVoidStreaks(param);
            }
            return true;
        }

        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnVoidStreaks(int param) {
        List<EntityFluxRift> rifts = getValidRifts();

        double px = worldPosition.getX() + 0.5;
        double py = worldPosition.getY() + 0.5625;
        double pz = worldPosition.getZ() + 0.5;

        for (EntityFluxRift rift : rifts) {
            FXDispatcher.INSTANCE.voidStreak(
                    rift.getX(), rift.getY(), rift.getZ(),
                    px, py, pz,
                    param, 0.04f
            );
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("progress", progress);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (tag.contains("progress")) {
            progress = tag.getInt("progress");

            if (progress < 0) progress = 0;
            if (progress > PROGRESS_REQUIRED * 4) progress = PROGRESS_REQUIRED * 4;
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ModItems.VOID_SEED.get());
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return SLOTS.clone();
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    public float getProgressPercent() {
        return (float) progress / PROGRESS_REQUIRED;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int value) {
        if (value < 0) value = 0;
        if (value > PROGRESS_REQUIRED * 4) value = PROGRESS_REQUIRED * 4;
        progress = value;
    }
}