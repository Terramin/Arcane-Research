package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.blocks.devices.BlockInlay;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TilePedestal extends TileArcaneresearchInventory {
    private AABB renderBox;

    public TilePedestal(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL.get(), pos, state, 1);
        this.syncedSlots = new int[]{0};
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return getItem(slot).isEmpty();
    }

    public void setItemFromInfusion(ItemStack stack) {
        if (stack == null) return;

        setItem(0, stack);

        if (level != null && !level.isClientSide) {
            syncTile(false);
        }
    }

    public ItemStack getDisplayedItem() {
        return getItem(0);
    }

    public boolean tryInsertItem(Player player, ItemStack heldItem) {
        if (heldItem == null || heldItem.isEmpty()) return false;

        ItemStack current = getItem(0);
        if (!current.isEmpty()) return false;

        ItemStack toPlace = heldItem.copy();
        toPlace.setCount(1);
        setItem(0, toPlace);
        heldItem.shrink(1);

        playPickupSound(1.6f);
        syncTile(false);
        return true;
    }

    public ItemStack tryTakeItem(Player player) {
        ItemStack current = getItem(0);
        if (current.isEmpty()) return ItemStack.EMPTY;

        ItemStack taken = current.copy();
        setItem(0, ItemStack.EMPTY);

        playPickupSound(1.5f);
        syncTile(false);
        return taken;
    }

    private void playPickupSound(float pitchScale) {
        if (level == null) return;

        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS,
                0.2f, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7f + 1.0f) * pitchScale);
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (level != null && level.isClientSide && clientTriggerEvent(id)) {
            return true;
        }

        return super.triggerEvent(id, data);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean clientTriggerEvent(int id) {
        if (id == 11) {
            FXDispatcher.INSTANCE.drawBamf(worldPosition.above(), 0.75f, 0.0f, 0.5f, true, true, null);
            return true;
        }

        if (id == 12) {
            FXDispatcher.INSTANCE.drawBamf(worldPosition.above(), true, true, null);
            return true;
        }

        if (id == 5) {
            FXDispatcher.INSTANCE.drawPedestalShield(worldPosition);
            return true;
        }

        return false;
    }

    @Nullable
    public BlockPos findInstabilityMitigator() {
        if (level == null) return null;

        BlockState ownState = getBlockState();

        if (!ownState.hasProperty(BlockInlay.CHARGE)) return null;

        int charge = ownState.getValue(BlockInlay.CHARGE);

        if (charge <= 0) return null;

        return seekSourceRecursive(worldPosition, charge);
    }

    private BlockPos seekSourceRecursive(BlockPos pos, int lastCharge) {
        if (level == null) return null;

        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos pp = pos.relative(face);

            int strength = BlockInlay.getSourceStrengthAt(level, pp);

            if (strength >= 5) {
                return pp;
            }

            BlockState bs = level.getBlockState(pp);

            if (!bs.hasProperty(BlockInlay.CHARGE)) continue;

            int charge = bs.getValue(BlockInlay.CHARGE);

            if (charge > lastCharge) {
                BlockPos found = seekSourceRecursive(pp, charge);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    worldPosition.getX() + 1, worldPosition.getY() + 2, worldPosition.getZ() + 1
            );
        }

        return renderBox;
    }
}