package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.entities.EntitySpecialItem;
import arcaneresearch.common.lib.crafting.CrucibleMatch;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Block entity of the alchemy crucible. It holds water and dissolved aspects, heats
 * up above a heat source and turns matching items into crucible-craft outputs.
 *
 * <p>Heating, spill timing and the liquid level are pure rules in
 * {@link CrucibleRules}; NBT and colour handling live in {@link CrucibleNbtRules}
 * and {@link CrucibleRenderRules}.</p>
 */
public class TileCrucible extends TileArcaneresearch implements IAspectContainer {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.BLOCKS);
    private static final ArcaneLogger AURA_LOGGER = ArcaneLogger.forCategory(ProfileCategory.AURA);
    /** Maximum vis that can be stored before the crucible starts spilling. */
    public static final int MAX_ASPECTS = CrucibleRules.ASPECT_LIMIT;
    /** Maximum amount of water the internal tank can hold. */
    public static final int TANK_CAPACITY = CrucibleRules.TANK_CAPACITY;

    /** Tank that syncs the tile whenever fluid actually changes via a capability. */
    private final class SyncingFluidTank extends FluidTank {
        SyncingFluidTank() {
            super(TANK_CAPACITY);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == Fluids.WATER;
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            int filled = super.fill(resource, action);
            if (action.execute() && filled > 0) {
                markDirtyAndSync();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            FluidStack drained = super.drain(resource, action);
            if (action.execute() && !drained.isEmpty()) {
                markDirtyAndSync();
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            FluidStack drained = super.drain(maxDrain, action);
            if (action.execute() && !drained.isEmpty()) {
                markDirtyAndSync();
            }
            return drained;
        }
    }

    private final FluidTank tank = new SyncingFluidTank();

    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(() -> tank);

    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    /** Current heat, 0..200; boiling starts at 151. */
    public short heat = 0;
    /** Aspects dissolved in the water. */
    public AspectList aspects = new AspectList();

    private int bellows = -1;
    private long tickCounter = CrucibleRules.COUNTER_INITIAL;

    private AABB renderBox;

    /**
     * @param pos block position
     * @param state initial block state
     */
    public TileCrucible(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    /**
     * Server tick: updates heat from the block below, handles overflow and the
     * periodic aspect spill.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileCrucible tile) {
        if (level == null) return;

        tile.tickCounter++;
        int prevHeat = tile.heat;

        boolean hasWater = tile.tank.getFluidAmount() > 0;
        boolean heatSource = false;
        if (hasWater) {
            tile.mutablePos.set(pos.getX(), pos.getY() - 1, pos.getZ());
            heatSource = tile.isHeatSource(level.getBlockState(tile.mutablePos));
        }
        int delta = CrucibleRules.heatDelta(hasWater, heatSource, tile.heat);
        tile.heat += delta;

        if (CrucibleRules.shouldSyncHeat(prevHeat, tile.heat, hasWater, heatSource)) {
            if (delta > 0) {
                LOGGER.debug("Crucible {} reached boiling heat ({})", pos, tile.heat);
            } else {
                LOGGER.debug("Crucible {} cooled below boiling point", pos);
            }
            tile.markDirtyAndSync();
        }

        int vis = tile.aspects.visSize();
        if (CrucibleRules.shouldSpill(vis)) {
            AURA_LOGGER.once("crucible.overflow", LogLevel.DEBUG,
                    "Crucible {} overflowing: {} vis stored (limit {})", pos, vis, MAX_ASPECTS);
            // Overflow spills immediately but must not reset the periodic counter.
            tile.spillRandom();
        }
        if (tile.tickCounter >= CrucibleRules.SPILL_INTERVAL) {
            tile.spillRandom();
            tile.tickCounter = 0;
        }
    }

    /** Client tick: draws froth and aspect bubbles while the crucible has water. */
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileCrucible tile) {
        if (level == null) return;

        if (tile.tank.getFluidAmount() > 0) {
            tile.drawEffects();
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        CrucibleNbtRules.writeHeat(tag, heat);
        tank.writeToNBT(tag);
        aspects.writeToNBT(tag);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        heat = CrucibleNbtRules.readHeat(tag);
        tank.readFromNBT(tag);
        if (CrucibleNbtRules.hasEmptyFlag(tag)) {
            tank.drain(TANK_CAPACITY, IFluidHandler.FluidAction.EXECUTE);
        }
        aspects.readFromNBT(tag);
    }

    @OnlyIn(Dist.CLIENT)
    private void drawEffects() {
        if (level == null || !level.isClientSide) return;

        float fluidHeight = getFluidHeight();

        if (heat > 150) {
            FXDispatcher.INSTANCE.crucibleFroth(
                    worldPosition.getX() + 0.2f + level.random.nextFloat() * 0.6f,
                    worldPosition.getY() + fluidHeight,
                    worldPosition.getZ() + 0.2f + level.random.nextFloat() * 0.6f);

            if (aspects.visSize() > 500) {
                for (int a = 0; a < 2; ++a) {
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX(),
                            worldPosition.getY() + 1, worldPosition.getZ() + level.random.nextFloat());
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX() + 1,
                            worldPosition.getY() + 1, worldPosition.getZ() + level.random.nextFloat());
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX() + level.random.nextFloat(),
                            worldPosition.getY() + 1, worldPosition.getZ());
                    FXDispatcher.INSTANCE.crucibleFrothDown(worldPosition.getX() + level.random.nextFloat(),
                            worldPosition.getY() + 1, worldPosition.getZ() + 1);
                }
            }
        }

        if (level.random.nextInt(6) == 0 && aspects.size() > 0) {
            Aspect[] arr = aspects.getAspects();
            int color = arr[level.random.nextInt(arr.length)].getColor();
            int x = 5 + level.random.nextInt(22);
            int y = 5 + level.random.nextInt(22);

            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            FXDispatcher.INSTANCE.crucibleBubble(worldPosition.getX() + x / 32.0f + 0.015625f,
                    worldPosition.getY() + 0.05f + fluidHeight,
                    worldPosition.getZ() + y / 32.0f + 0.015625f, r, g, b);
        }
    }

    private boolean isHeatSource(BlockState state) {
        return state.is(Blocks.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.MAGMA_BLOCK)
                || state.getBlock() instanceof arcaneresearch.common.blocks.misc.BlockNitor;
    }

    /**
     * Smelts a dropped item, consuming it when the result is fully used.
     *
     * @param itemEntity item falling into the crucible
     */
    public void attemptSmelt(ItemEntity itemEntity) {
        if (level == null || level.isClientSide || itemEntity == null) return;
        if (heat < 151 || tank.getFluidAmount() <= 0) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;

        String thrower = itemEntity.getOwner() != null
                ? itemEntity.getOwner().getName().getString()
                : "";

        ItemStack result = attemptSmelt(stack, thrower);

        if (result == null || result.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(result);
        }
    }

    /**
     * Runs one item through the crucible, either crafting a recipe or dissolving
     * it into its aspects.
     *
     * @param item item stack to process
     * @param username name of the player who dropped it, may be empty
     * @return the remaining stack, or {@code null} when fully consumed
     */
    public ItemStack attemptSmelt(ItemStack item, String username) {
        if (level == null || item == null) return item;

        boolean bubble = false;
        boolean craftDone = false;
        int stacksize = item.getCount();

        Player player = null;

        if (!username.isEmpty()) {
            player = level.players().stream()
                    .filter(p -> p.getName().getString().equals(username))
                    .findFirst()
                    .orElse(null);
        }

        Block block = getBlockState().getBlock();

        for (int a = 0; a < stacksize; ++a) {
            CrucibleMatch recipe = ArcaneresearchCraftingManager.findMatchingCrucibleRecipe(
                    aspects, item, player, level);

            if (recipe != null && tank.getFluidAmount() > 0) {
                ItemStack out = recipe.output().get().copy();

                if (player != null) {
                    // Forge 1.20.1's event is void, so there is no replacement result
                    // to apply; it is fired purely for other mods' listeners.
                    net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(
                            player, out, new net.minecraft.world.SimpleContainer(item.copy()));
                }

                LOGGER.debug("Crucible {} crafted {} from {} (water={})",
                        worldPosition, out, item, tank.getFluidAmount());
                aspects = recipe.removeAspects(aspects);
                tank.drain(CrucibleRules.WATER_PER_CRAFT, IFluidHandler.FluidAction.EXECUTE);
                ejectItem(out);

                craftDone = true;
                --stacksize;
                tickCounter = CrucibleRules.COUNTER_AFTER_CRAFT;
            } else {
                AspectList itemAspects = getItemAspects(item);

                if (itemAspects.size() != 0) {
                    AURA_LOGGER.once("crucible.absorb", LogLevel.DEBUG,
                            "Crucible {} dissolved {} into {} aspect(s)", worldPosition, item, itemAspects.size());
                    for (Aspect tag : itemAspects.getAspects()) {
                        aspects.add(tag, itemAspects.getAmount(tag));
                    }

                    bubble = true;
                    --stacksize;
                    tickCounter = CrucibleRules.COUNTER_AFTER_ABSORB;
                }
            }
        }

        if (bubble) {
            level.playSound(null, worldPosition, ModSounds.BUBBLE.get(),
                    SoundSource.BLOCKS, 0.2f, 1.0f + level.random.nextFloat() * 0.4f);

            markDirtyAndSync();
            level.blockEvent(worldPosition, block, 2, 1);
        }

        if (craftDone) {
            markDirtyAndSync();
            level.blockEvent(worldPosition, block, 99, 0);
        }

        setChanged();

        if (stacksize <= 0) return null;

        item.setCount(stacksize);
        return item;
    }

    private AspectList getItemAspects(ItemStack item) {
        return ArcaneresearchCraftingManager.getObjectTags(item);
    }

    /** Ejects a craft result above the crucible, splitting it into legal stack sizes. */
    public void ejectItem(ItemStack items) {
        if (level == null || level.isClientSide || items == null || items.isEmpty()) return;

        int maxStack = items.getMaxStackSize();
        boolean first = true;

        while (items.getCount() > 0) {
            ItemStack spitout = items.copy();

            if (spitout.getCount() > maxStack) {
                spitout.setCount(maxStack);
            }

            items.shrink(spitout.getCount());

            EntitySpecialItem entityitem = new EntitySpecialItem(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.71, worldPosition.getZ() + 0.5,
                    spitout);

            entityitem.setDeltaMovement(
                    first ? 0.0 : (level.random.nextFloat() - level.random.nextFloat()) * 0.01f,
                    0.075,
                    first ? 0.0 : (level.random.nextFloat() - level.random.nextFloat()) * 0.01f);

            level.addFreshEntity(entityitem);
            first = false;
        }
    }

    /** Removes one random aspect and pollutes the aura, then syncs the tile. */
    public void spillRandom() {
        if (level == null) return;

        Aspect[] aspectArray = aspects.getAspects();
        if (aspectArray.length > 0) {
            Aspect toSpill = aspectArray[level.random.nextInt(aspectArray.length)];
            aspects.remove(toSpill, 1);

            float pollution = CrucibleRules.spillPollution(toSpill == Aspect.FLUX);
            AURA_LOGGER.once("crucible.spill", LogLevel.DEBUG,
                    "Crucible {} spilled {} (pollution={}, vis left={})",
                    worldPosition, toSpill, pollution, aspects.visSize());
            AuraHelper.polluteAura(level, worldPosition, pollution, true);
        }

        // The original syncs on every spill, including the periodic empty one.
        markDirtyAndSync();
    }

    /** Empties the tank and all aspects, polluting the aura with their vis. */
    public void spillAll() {
        if (level == null) return;

        int total = aspects.visSize();

        if (tank.getFluidAmount() > 0 || total > 0) {
            tank.drain(TANK_CAPACITY, IFluidHandler.FluidAction.EXECUTE);
            AURA_LOGGER.debug("Crucible {} fully spilled {} vis", worldPosition, total);
            AuraHelper.polluteAura(level, worldPosition, total * 0.25f, true);

            int flux = aspects.getAmount(Aspect.FLUX);

            if (flux > 0) {
                AuraHelper.polluteAura(level, worldPosition, flux * 0.75f, false);
            }

            aspects = new AspectList();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 2, 5);
            markDirtyAndSync();
        }
    }

    /** Alias of {@link #spillAll()} kept for API compatibility. */
    public void spillRemnants() {
        spillAll();
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (level != null && level.isClientSide && clientTriggerEvent(id, param)) {
            return true;
        }

        return super.triggerEvent(id, param);
    }

    @OnlyIn(Dist.CLIENT)
    private boolean clientTriggerEvent(int id, int param) {
        if (level == null) return false;

        if (id == 99) {
            FXDispatcher.INSTANCE.drawBamf(
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.25f,
                    worldPosition.getZ() + 0.5, true, true, Direction.UP);

            level.playLocalSound(
                    worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f,
                    ModSounds.SPILL.get(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
            return true;
        }

        if (id == 1) {
            FXDispatcher.INSTANCE.drawBamf(worldPosition.above(), true, true, Direction.UP);
            return true;
        }

        if (id == 2) {
            level.playLocalSound(
                    worldPosition.getX() + 0.5f, worldPosition.getY() + 0.5f, worldPosition.getZ() + 0.5f,
                    ModSounds.SPILL.get(), SoundSource.BLOCKS, 0.2f, 1.0f, false);
            for (int q = 0; q < 10; ++q) {
                FXDispatcher.INSTANCE.crucibleBoil(worldPosition, this, param);
            }
            return true;
        }

        return false;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public int addToContainer(Aspect tag, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList list) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList list) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
    }

    /** @return the internal water tank, wrapped to sync changes */
    public FluidTank getTank() {
        return tank;
    }

    /** @return height of the liquid surface, including the aspect-driven offset */
    public float getFluidHeight() {
        return CrucibleRules.fluidHeight(tank.getFluidAmount(), aspects.visSize());
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                    worldPosition.getX() + 1, worldPosition.getY() + 1, worldPosition.getZ() + 1
            );
        }

        return renderBox;
    }

    /** @return whether the crucible is hot enough to smelt items */
    public boolean isHeated() {
        return CrucibleRules.isHeated(heat);
    }
}