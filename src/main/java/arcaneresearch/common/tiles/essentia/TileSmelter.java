package arcaneresearch.common.tiles.essentia;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.SmelterMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.common.tiles.devices.TileBellows;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModItems;
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
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileSmelter extends TileArcaneresearchInventory implements Container, MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int MAX_VIS = 256;

    private static final int[] SLOTS_BOTTOM = {SLOT_FUEL};
    private static final int[] SLOTS_TOP = {};
    private static final int[] SLOTS_SIDES = {SLOT_INPUT};

    private static final Direction[] HORIZONTALS =
            Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
    private static final Direction[] CHECK_NORTH = {Direction.SOUTH, Direction.EAST, Direction.WEST};
    private static final Direction[] CHECK_SOUTH = {Direction.NORTH, Direction.EAST, Direction.WEST};
    private static final Direction[] CHECK_EAST = {Direction.SOUTH, Direction.NORTH, Direction.WEST};
    private static final Direction[] CHECK_WEST = {Direction.SOUTH, Direction.EAST, Direction.NORTH};

    public AspectList aspects = new AspectList();
    public int vis = 0;
    public int smeltTime = 100;
    public int furnaceBurnTime = 0;
    public int currentItemBurnTime = 0;
    public int furnaceCookTime = 0;

    private final int maxVis = MAX_VIS;
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
            Aspect[] currentAspects = tile.aspects.getAspects();
            for (Aspect aspect : currentAspects) {
                if (aspect != null && tile.aspects.getAmount(aspect) > 0
                        && TileAlembic.processAlembics(level, pos, aspect)) {
                    tile.takeFromContainer(aspect, 1);
                    break;
                }
            }
            Direction smelterFacing = tile.getFacing();
            for (Direction face : HORIZONTALS) {
                if (smelterFacing == face) {
                    continue;
                }
                BlockPos auxPos = pos.relative(face);
                BlockState auxState = level.getBlockState(auxPos);
                if (auxState.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockSmelterAux
                        && auxState.getValue(BlockStateProperties.HORIZONTAL_FACING) == face.getOpposite()) {
                    for (Aspect aspect : currentAspects) {
                        if (aspect != null && tile.aspects.getAmount(aspect) > 0
                                && TileAlembic.processAlembics(level, auxPos, aspect)) {
                            tile.takeFromContainer(aspect, 1);
                            break;
                        }
                    }
                }
            }
        }

        boolean canSmeltNow = tile.canSmelt();

        if (tile.furnaceBurnTime == 0) {
            if (canSmeltNow) {
                ItemStack fuel = tile.getItem(SLOT_FUEL);
                int burnTime = ForgeHooks.getBurnTime(fuel, null);
                if (burnTime > 0) {
                    tile.furnaceBurnTime = burnTime;
                    tile.currentItemBurnTime = burnTime;
                    tile.speedBoost = fuel.is(ModItems.ALUMENTUM.get());
                    ItemStack containerItem = fuel.getCraftingRemainingItem();
                    fuel.shrink(1);
                    if (fuel.isEmpty()) {
                        tile.setItem(SLOT_FUEL, containerItem);
                    }
                    setFurnaceState(level, pos, state, true);
                    changed = true;
                } else {
                    setFurnaceState(level, pos, state, false);
                }
            } else {
                setFurnaceState(level, pos, state, false);
            }
        }

        if (isEnabled(state) && tile.furnaceBurnTime > 0 && canSmeltNow) {
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
        return ForgeHooks.getBurnTime(stack, null) > 0;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("BurnTime", furnaceBurnTime);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        furnaceBurnTime = tag.contains("BurnTime") ? tag.getInt("BurnTime") : tag.getShort("BurnTime");
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("speedBoost", speedBoost);
        tag.putInt("CookTime", furnaceCookTime);
        aspects.writeToNBT(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        speedBoost = tag.contains("speedBoost") ? tag.getBoolean("speedBoost") : tag.getBoolean("SpeedBoost");
        furnaceCookTime = tag.contains("CookTime") ? tag.getInt("CookTime") : tag.getShort("CookTime");
        aspects.readFromNBT(tag);
        vis = aspects.visSize();
        ItemStack fuel = getItem(SLOT_FUEL);
        if (!fuel.isEmpty()) {
            currentItemBurnTime = ForgeHooks.getBurnTime(fuel, null);
        }
    }

    private boolean canSmelt() {
        ItemStack input = getItem(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        AspectList itemAspects = getItemAspects(input);
        int itemVis = itemAspects.visSize();
        if (itemVis <= 0) {
            return false;
        }
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
        float efficiency = getEfficiency();
        float fluxEfficiency = efficiency * 0.66f;
        int flux = 0;
        for (Aspect aspect : itemAspects.getAspects()) {
            if (aspect == null) continue;
            int amount = itemAspects.getAmount(aspect);
            float eff = (aspect == Aspect.FLUX) ? fluxEfficiency : efficiency;
            for (int i = 0; i < amount; i++) {
                assert level != null;
                if (level.random.nextFloat() > eff) {
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
                for (Direction face : HORIZONTALS) {
                    if (smelterFacing == face) {
                        continue;
                    }
                    BlockPos ventPos = worldPosition.relative(face);
                    BlockState ventState = level.getBlockState(ventPos);
                    if (ventState.getBlock() instanceof arcaneresearch.common.blocks.essentia.BlockSmelterVent
                            && ventState.getValue(BlockStateProperties.HORIZONTAL_FACING) == face.getOpposite()
                            && level.random.nextFloat() < 0.333f) {
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
        if (tag != null && aspects != null && aspects.getAmount(tag) >= amount) {
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
        Direction[] checkDirs = switch (facing) {
            case NORTH -> CHECK_NORTH;
            case SOUTH -> CHECK_SOUTH;
            case EAST -> CHECK_EAST;
            case WEST -> CHECK_WEST;
            default -> HORIZONTALS;
        };
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
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        if (slot == SLOT_INPUT) {
            return getItemAspects(stack).size() > 0;
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

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.smelter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new SmelterMenu(containerId, playerInventory, this);
    }
}