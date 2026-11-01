package arcaneresearch.common.tiles.aura;

import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class AuraTotemPoleTile extends AuraTotemTile implements WorldlyContainer {

    private static final int[] SLOTS = {0};
    private static final ItemStack EMPTY = ItemStack.EMPTY;

    public AuraTotemPoleTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    private TileArcaneresearchInventory target() {
        Level level = getLevel();
        if (level == null) return null;
        BlockEntity tile = level.getBlockEntity(this.worldPosition.above());
        return tile instanceof AuraTotemTile totem ? totem : null;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        return EMPTY;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        TileArcaneresearchInventory target = target();
        return target != null ? target.getItem(slot) : EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        TileArcaneresearchInventory target = target();
        if (target != null) target.setItem(slot, stack);
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction side) {
        TileArcaneresearchInventory target = target();
        if (target instanceof WorldlyContainer worldly) {
            return worldly.canPlaceItemThroughFace(slot, stack, side);
        }
        return target != null && target.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NotNull ItemStack stack, @NotNull Direction side) {
        return false;
    }
}