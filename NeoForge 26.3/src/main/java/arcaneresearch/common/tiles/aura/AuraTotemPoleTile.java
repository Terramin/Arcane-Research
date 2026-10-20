package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;

/**
 * Totem pole extension, ported from TC5 TileAuraTotemPole.
 * Proxies its single slot to the totem tile above.
 */
public class AuraTotemPoleTile extends AuraTotemTile implements WorldlyContainer {
    public AuraTotemPoleTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    private TileArcaneresearchInventory target() {
        Level level = getLevel();
        if (level == null) {
            return null;
        }
        BlockEntity tile = level.getBlockEntity(this.worldPosition.above());
        if (tile instanceof AuraTotemTile || tile instanceof AuraTotemPoleTile) {
            return (TileArcaneresearchInventory) tile;
        }
        return null;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack getItem(int slot) {
        TileArcaneresearchInventory target = target();
        return target == null ? ItemStack.EMPTY : target.getItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        TileArcaneresearchInventory target = target();
        if (target != null) {
            target.setItem(slot, stack);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        TileArcaneresearchInventory target = target();
        if (target instanceof WorldlyContainer worldly) {
            return worldly.canPlaceItemThroughFace(slot, stack, side);
        }
        return target != null && target.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
