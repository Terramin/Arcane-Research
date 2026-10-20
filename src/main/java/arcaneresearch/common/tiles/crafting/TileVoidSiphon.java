package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModItems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

        if (!tile.isEnabled(state)) return;

        if (tile.counter % 20 == 0 && tile.progress < PROGRESS_REQUIRED) {

            ItemStack stack = tile.getItem(0);
            if (stack.isEmpty() || (stack.is(ModItems.VOID_SEED.get()) && stack.getCount() < stack.getMaxStackSize())) {

                List<EntityFluxRift> rifts = tile.getValidRifts();
                boolean didWork = false;

                for (EntityFluxRift rift : rifts) {
                    double d = Math.sqrt(rift.getRiftSize());
                    tile.progress += (int) d;

                    rift.setRiftStability(rift.getRiftStability() - (float) (d / 15.0));

                    if (level.getRandom().nextInt(33) == 0) {
                        rift.setRiftSize(rift.getRiftSize() - 1);
                    }

                    if (d >= 1.0) {
                        didWork = true;
                    }
                }

                if (didWork && tile.counter % 40 == 0) {
                    level.blockEvent(pos, state.getBlock(), 5, tile.counter);
                }

                while (tile.progress >= PROGRESS_REQUIRED) {
                    stack = tile.getItem(0);
                    if (stack.isEmpty() || (stack.is(ModItems.VOID_SEED.get()) && stack.getCount() < stack.getMaxStackSize())) {
                        tile.progress -= PROGRESS_REQUIRED;

                        if (stack.isEmpty()) {
                            tile.setItem(0, new ItemStack(ModItems.VOID_SEED.get()));
                        } else {
                            stack.grow(1);
                        }
                        tile.syncTile(false);
                        tile.setChanged();
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileVoidSiphon tile) {

    }

    private List<EntityFluxRift> getValidRifts() {
        List<EntityFluxRift> result = new ArrayList<>();
        List<EntityFluxRift> rifts = EntityUtils.getEntitiesInRange(level, worldPosition, 8.0, EntityFluxRift.class);

        for (EntityFluxRift rift : rifts) {
            if (rift.isRemoved()) continue;
            if (rift.getRiftSize() < 2) continue;

            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 1;
            double z = worldPosition.getZ() + 0.5;

            Vec3 siphonPos = new Vec3(x, y, z);
            Vec3 riftPos = new Vec3(rift.getX(), rift.getY(), rift.getZ());
            Vec3 checkPos = siphonPos.add(riftPos.subtract(siphonPos).normalize());

            if (EntityUtils.canEntityBeSeen(rift, checkPos.x, checkPos.y, checkPos.z)) {
                result.add(rift);
            }
        }

        return result;
    }

    private boolean isEnabled(BlockState state) {
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED)) {
            return state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED);
        }
        return !level.hasNeighborSignal(worldPosition);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 5) {
            if (level != null && level.isClientSide()) {
                spawnVoidStreaks(param);
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnVoidStreaks(int param) {
        List<EntityFluxRift> rifts = getValidRifts();
        for (EntityFluxRift rift : rifts) {
            FXDispatcher.INSTANCE.voidStreak(
                    rift.getX(), rift.getY(), rift.getZ(),
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5625, worldPosition.getZ() + 0.5,
                    param, 0.04f);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putShort("progress", (short) progress);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        progress = tag.getShortOr("progress", (short) 0);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(ModItems.VOID_SEED.get());
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
    }

    public float getProgressPercent() {
        return (float) progress / PROGRESS_REQUIRED;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int value) {
        progress = value;
    }
}
