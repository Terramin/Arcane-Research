package arcaneresearch.common.tiles.essentia;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.SmelterMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.common.tiles.devices.TileBellows;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModItems;

import javax.annotation.Nullable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileSmelter extends TileArcaneresearchInventory implements Container, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int MAX_VIS = 256;
    private static final int[] SLOTS_BOTTOM = {SLOT_FUEL};
    private static final int[] SLOTS_TOP = {};
    private static final int[] SLOTS_SIDES = {SLOT_INPUT};
    public AspectList aspects = new AspectList();
    public int vis = 0;
    public int smeltTime = 100;
    public int furnaceBurnTime = 0;
    public int currentItemBurnTime = 0;
    public int furnaceCookTime = 0;
    private int maxVis = MAX_VIS;
    private boolean speedBoost = false;

    private int bellows = -1;
    private int count = 0;

    private int smelterType = 0;

    public TileSmelter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 2);
    }

    public TileSmelter(BlockPos pos, BlockState state) {
        this(ModBlockEntities.SMELTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileSmelter tile) {
        // NEOFORGE_PORT: Forge body; ForgeHooks.getBurnTime -> CraftingEvents helpers;
        // ItemStack.getCraftingRemainingItem() -> same helper (same semantics).
        boolean wasBurning = tile.furnaceBurnTime > 0;
        boolean changed = false;

        tile.count++;

        if (tile.furnaceBurnTime > 0) {
            tile.furnaceBurnTime--;
        }

        if (tile.bellows < 0) {
            tile.checkNeighbours();
        }

        int speed = tile.getSpeed();
        if (tile.speedBoost) {
            speed = (int) (speed * 0.8);
        }

        if (tile.count % speed == 0 && tile.aspects.size() > 0) {
            for (Aspect aspect : tile.aspects.getAspects()) {
                if (tile.aspects.getAmount(aspect) > 0) {
                    if (TileAlembic.processAlembics(level, pos, aspect)) {
                        tile.takeFromContainer(aspect, 1);
                        break;
                    }
                }
            }
            Direction smelterFacing = tile.getFacing();
            for (Direction face : Direction.Plane.HORIZONTAL) {
                if (smelterFacing == face) {
                    continue;
                }
                BlockPos auxPos = pos.relative(face);
                BlockState auxState = level.getBlockState(auxPos);
                if (auxState.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockSmelterAux
                        && auxState.getValue(BlockStateProperties.HORIZONTAL_FACING) == face.getOpposite()) {
                    for (Aspect aspect : tile.aspects.getAspects()) {
                        if (tile.aspects.getAmount(aspect) > 0) {
                            if (TileAlembic.processAlembics(level, auxPos, aspect)) {
                                tile.takeFromContainer(aspect, 1);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (tile.furnaceBurnTime == 0 && tile.canSmelt()) {
            ItemStack fuel = tile.getItem(SLOT_FUEL);
            int burnTime = arcaneresearch.common.lib.events.CraftingEvents.getBurnTimeTotal(level, fuel);

            if (burnTime > 0) {
                tile.furnaceBurnTime = burnTime;
                tile.currentItemBurnTime = burnTime;
                changed = true;
                tile.speedBoost = false;

                if (fuel.is(ModItems.ALUMENTUM.get())) {
                    tile.speedBoost = true;
                }

                if (!fuel.isEmpty()) {
                    ItemStack containerItem = arcaneresearch.common.lib.events.CraftingEvents
                            .getCraftingRemainingItem(fuel);
                    fuel.shrink(1);
                    if (fuel.isEmpty()) {
                        tile.setItem(SLOT_FUEL, containerItem);
                    }
                }

                setFurnaceState(level, pos, state, true);
            } else {
                setFurnaceState(level, pos, state, false);
            }
        } else if (tile.furnaceBurnTime == 0) {
            setFurnaceState(level, pos, state, false);
        }

        if (isEnabled(state) && tile.furnaceBurnTime > 0 && tile.canSmelt()) {
            tile.furnaceCookTime++;
            if (tile.furnaceCookTime >= tile.smeltTime) {
                tile.furnaceCookTime = 0;
                tile.smeltItem();
                changed = true;
            }
        } else {
            tile.furnaceCookTime = 0;
        }

        if (wasBurning != (tile.furnaceBurnTime > 0)) {
            changed = true;
        }

        if (changed) {
            tile.setChanged();
        }
    }

    private static void setFurnaceState(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.hasProperty(BlockStateProperties.LIT)) {
            boolean currentlyLit = state.getValue(BlockStateProperties.LIT);
            if (currentlyLit != lit) {
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), 3);
            }
        }
    }

    private static boolean isEnabled(BlockState state) {
        if (state.hasProperty(BlockStateProperties.ENABLED)) {
            return state.getValue(BlockStateProperties.ENABLED);
        }

        if (state.hasProperty(BlockStateProperties.LIT)) {
            return state.getValue(BlockStateProperties.LIT);
        }
        return true;
    }

    public static boolean isItemFuel(ItemStack stack) {
        return arcaneresearch.common.lib.events.CraftingEvents.getBurnTime(stack) > 0 || stack.has(net.minecraft.core.component.DataComponents.COOKING_FUEL);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putShort("BurnTime", (short) furnaceBurnTime);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        furnaceBurnTime = tag.getShortOr("BurnTime", (short) 0);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);

        tag.putBoolean("speedBoost", speedBoost);
        tag.putShort("CookTime", (short) furnaceCookTime);
        aspects.writeToNBT(tag);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        speedBoost = hasBoolean(tag, "speedBoost") ? tag.getBooleanOr("speedBoost", false) : tag.getBooleanOr("SpeedBoost", false);
        furnaceCookTime = tag.getShortOr("CookTime", (short) 0);
        aspects.readFromNBT(tag);
        vis = aspects.visSize();

        ItemStack fuel = getItem(SLOT_FUEL);
        if (!fuel.isEmpty()) {
            currentItemBurnTime = arcaneresearch.common.lib.events.CraftingEvents.getBurnTimeTotal(level, fuel);
        }
    }

    private boolean canSmelt() {
        ItemStack input = getItem(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }

        AspectList itemAspects = getItemAspects(input);
        if (itemAspects == null || itemAspects.size() == 0) {
            return false;
        }

        int itemVis = itemAspects.visSize();
        if (itemVis > maxVis - vis) {
            return false;
        }

        smeltTime = (int) (itemVis * 2 * (1.0f - 0.125f * bellows));
        return true;
    }

    private void smeltItem() {
        if (!canSmelt()) return;

        ItemStack input = getItem(SLOT_INPUT);
        AspectList itemAspects = getItemAspects(input);

        int flux = 0;
        float efficiency = getEfficiency();

        for (Aspect aspect : itemAspects.getAspects()) {
            int amount = itemAspects.getAmount(aspect);
            for (int i = 0; i < amount; i++) {

                float effectiveEfficiency = (aspect == Aspect.FLUX) ? efficiency * 0.66f : efficiency;
                if (level.getRandom().nextFloat() > effectiveEfficiency) {
                    flux++;
                } else {
                    aspects.add(aspect, 1);
                }
            }
        }

        if (flux > 0) {
            int pollution = 0;
            Direction smelterFacing = getFacing();
            for (int c = 0; c < flux; c++) {
                boolean vented = false;
                for (Direction face : Direction.Plane.HORIZONTAL) {
                    if (smelterFacing == face) {
                        continue;
                    }
                    BlockPos ventPos = worldPosition.relative(face);
                    BlockState ventState = level.getBlockState(ventPos);
                    if (ventState.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockSmelterVent
                            && ventState.getValue(BlockStateProperties.HORIZONTAL_FACING) == face.getOpposite()
                            && level.getRandom().nextFloat() < 0.333f) {
                        level.blockEvent(worldPosition, getBlockState().getBlock(), 1,
                                face.getOpposite().ordinal());
                        vented = true;
                        break;
                    }
                }
                if (!vented) {
                    pollution++;
                }
            }
            if (pollution > 0) {
                AuraHelper.polluteAura(level, worldPosition, pollution, true);
            }
        }

        vis = aspects.visSize();

        input.shrink(1);
        if (input.isEmpty()) {
            setItem(SLOT_INPUT, ItemStack.EMPTY);
        }
    }

    private AspectList getItemAspects(ItemStack stack) {
        if (stack.isEmpty()) {
            return new AspectList();
        }
        return ArcaneresearchCraftingManager.getObjectTags(stack);
    }

    public boolean takeFromContainer(Aspect tag, int amount) {
        if (aspects != null && aspects.getAmount(tag) >= amount) {
            aspects.remove(tag, amount);
            vis = aspects.visSize();
            setChanged();
            return true;
        }
        return false;
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    public void checkNeighbours() {
        Direction facing = getFacing();
        Direction[] checkDirs;

        switch (facing) {
            case NORTH -> checkDirs = new Direction[]{Direction.SOUTH, Direction.EAST, Direction.WEST};
            case SOUTH -> checkDirs = new Direction[]{Direction.NORTH, Direction.EAST, Direction.WEST};
            case EAST -> checkDirs = new Direction[]{Direction.SOUTH, Direction.NORTH, Direction.WEST};
            case WEST -> checkDirs = new Direction[]{Direction.SOUTH, Direction.EAST, Direction.NORTH};
            default -> checkDirs = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        }

        bellows = TileBellows.getBellows(level, worldPosition, checkDirs);
    }

    private float getEfficiency() {
        return switch (smelterType) {
            case 1 -> 0.9f;
            case 2 -> 0.95f;
            default -> 0.8f;
        };
    }

    private int getSpeed() {

        return switch (smelterType) {
            case 1 -> 10;
            case 2 -> 15;
            default -> 15;
        };
    }

    public void setSmelterType(int type) {
        this.smelterType = Math.max(0, Math.min(2, type));
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {

            AspectList aspects = getItemAspects(stack);
            return aspects != null && aspects.size() > 0;
        }
        return slot == SLOT_FUEL && isItemFuel(stack);
    }

    public int[] getSlotsForFace(Direction face) {
        return switch (face) {
            case DOWN -> SLOTS_BOTTOM;
            case UP -> SLOTS_TOP;
            default -> SLOTS_SIDES;
        };
    }

    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction face) {
        return face != Direction.UP && canPlaceItem(slot, stack);
    }

    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction face) {

        return face != Direction.UP || slot != SLOT_FUEL || stack.getItem() == Items.BUCKET;
    }

    public int getCookProgressScaled(int scale) {
        if (smeltTime <= 0) smeltTime = 1;
        return furnaceCookTime * scale / smeltTime;
    }

    public int getVisScaled(int scale) {
        return vis * scale / maxVis;
    }

    public int getMaxVis() {
        return maxVis;
    }

    public int getBurnTimeRemainingScaled(int scale) {
        if (currentItemBurnTime == 0) currentItemBurnTime = 200;
        return furnaceBurnTime * scale / currentItemBurnTime;
    }

    public boolean isBurning() {
        return furnaceBurnTime > 0;
    }

    // NEOFORGE_PORT (26.3): reference BlockSmelter.onRemove dropped the slots and then
    // polluted the aura with the stored vis. The port's affectNeighborsAfterRemoval runs after
    // the tile is gone, so the pollution never happened. super drops the slots (Container).
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (level != null && vis > 0) {
            AuraHelper.polluteAura(level, pos, vis, true);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SmelterMenu(containerId, playerInventory, this);
    }
}
