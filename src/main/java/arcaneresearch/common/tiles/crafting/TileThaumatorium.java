package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.blocks.devices.BlockBrainBox;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.menu.ThaumatoriumMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Block entity of the alchemy furnace. The server selects a recipe by catalyst,
 * pulls essentia from neighbouring transports and crafts one output per catalyst;
 * the client builds the recipe catalogue when needed, displays the synchronized
 * recipes and runs the vent particles.
 *
 * <p>The three recipe lists are parallel: {@link #recipeHash},
 * {@link #recipeEssentia} and {@link #recipePlayer}. Unknown hashes are kept so a
 * sync packet received before the recipe catalogue is ready cannot lose a recipe.</p>
 */
public class TileThaumatorium extends TileArcaneresearchInventory implements IAspectContainer, IEssentiaTransport, MenuProvider, WorldlyContainer {
    private static final int[] SLOTS = {0};
    private static final Direction[] DIRECTIONS = Direction.values();

    /** Essentia currently stored inside the furnace. */
    public AspectList essentia = new AspectList();
    /** Hashes of the selected crucible recipes, in slot order. */
    public ArrayList<Integer> recipeHash = new ArrayList<>();
    /** Aspects required by each selected recipe, parallel to {@link #recipeHash}. */
    public ArrayList<AspectList> recipeEssentia = new ArrayList<>();
    /** Player names that selected each recipe, parallel to {@link #recipeHash}. */
    public ArrayList<String> recipePlayer = new ArrayList<>();

    /** Index of the recipe currently being crafted, or -1 when none is selected. */
    public int currentCraft = -1;
    /** Number of recipe slots, increased by adjacent mnemonic matrices. */
    public int maxRecipes = 1;
    /** Aspect the furnace is currently requesting from its neighbours. */
    public Aspect currentSuction = null;
    /** Open container menu, notified whenever the inventory changes. */
    public AbstractContainerMenu eventHandler;
    /** Recipes available for the current catalyst, used by the GUI. */
    public ArrayList<CrucibleRecipe> recipes = new ArrayList<>();

    int venting = 0;
    int counter = 0;
    boolean heated = false;
    CrucibleRecipe currentRecipe = null;

    private AABB renderBox;
    /** False until every stored recipe hash has been resolved against the catalogue. */
    private boolean recipesResolved = false;
    /** Catalogue version used for the last successful resolution. */
    private int resolvedCatalogVersion = -1;

    /**
     * @param type registered block entity type
     * @param pos block position
     * @param state initial block state
     */
    public TileThaumatorium(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
    }

    /** Convenience constructor using the registered furnace block entity type. */
    public TileThaumatorium(BlockPos pos, BlockState state) {
        this(ModBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    /**
     * Server tick: refreshes heat and upgrades, selects a recipe for the catalyst and
     * pulls the missing essentia or completes the craft.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileThaumatorium tile) {
        if (level == null || level.isClientSide) return;

        arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager.ensureCrucibleBridge(level);
        tile.resolveRecipes();

        if (ThaumatoriumRules.isRefreshTick(tile.counter)) {
            tile.heated = tile.checkHeat();
            tile.getUpgrades();
            tile.ensureTopBlock();
        }

        tile.counter++;

        if (!tile.heated || tile.gettingPower() || !ThaumatoriumRules.isCraftTick(tile.counter)) return;
        if (tile.recipeHash == null || tile.recipeHash.isEmpty()) return;

        ItemStack catalyst = tile.getItem(0);

        if (catalyst.isEmpty()) {
            tile.currentSuction = null;
            return;
        }

        if (ThaumatoriumRules.shouldReselectRecipe(tile.currentCraft, tile.recipeHash.size(),
                tile.currentRecipe == null,
                tile.currentRecipe != null && tile.currentRecipe.catalystMatches(catalyst))) {

            tile.clearCurrentRecipe();

            for (int a = 0; a < tile.recipeHash.size(); a++) {
                CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(tile.recipeHash.get(a));

                if (recipe != null && recipe.catalystMatches(catalyst)) {
                    tile.currentCraft = a;
                    tile.currentRecipe = recipe;
                    break;
                }
            }
        }

        if (!ThaumatoriumRules.isRecipeSlotValid(tile.currentCraft, tile.recipeHash.size())) return;

        boolean done = true;
        tile.currentSuction = null;

        AspectList need = tile.recipeEssentia.get(tile.currentCraft);

        for (Aspect aspect : need.getAspectsSortedByName()) {
            if (!ThaumatoriumRules.isSatisfied(
                    need.getAmount(aspect), tile.essentia.getAmount(aspect))) {
                tile.currentSuction = aspect;
                done = false;
                break;
            }
        }

        if (done) {
            tile.completeRecipe();
        } else if (tile.currentSuction != null) {
            tile.fill();
        }
    }

    /**
     * Client tick: builds the recipe bridge when needed, resolves stored hashes and
     * emits vent particles for the remaining duration after a trigger event.
     */
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileThaumatorium tile) {
        if (level == null) return;

        // The recipe catalogue normally only exists on the server; build it before
        // resolving the sync tag so a client that never opened the menu still shows recipes.
        if (!tile.areRecipesResolved()) {
            arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager.ensureCrucibleBridge(level);
            tile.resolveRecipes();
        }

        if (tile.venting <= 0) return;

        tile.venting--;

        Direction facing = tile.getFacing();
        int fx = facing != null ? facing.getStepX() : 0;
        int fz = facing != null ? facing.getStepZ() : 0;

        float ox = ThaumatoriumRules.ventSpeed(level.random.nextFloat());
        float oz = ThaumatoriumRules.ventSpeed(level.random.nextFloat());
        float oy = ThaumatoriumRules.ventSpeed(level.random.nextFloat());
        float vx = ThaumatoriumRules.ventSpeed(level.random.nextFloat());
        float vy = ThaumatoriumRules.ventSpeed(level.random.nextFloat());
        float vz = ThaumatoriumRules.ventSpeed(level.random.nextFloat());

        FXDispatcher.INSTANCE.drawVentParticles(
                pos.getX() + 0.5f + ox + fx / 2.0f, pos.getY() + 0.5f + oy, pos.getZ() + 0.5f + oz + fz / 2.0f,
                fx / 4.0f + vx, vy, fz / 4.0f + vz,
                0xFFFFFF);
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putByte("maxrec", (byte) maxRecipes);
        essentia.writeToNBT(tag);

        int[] hashes = new int[recipeHash.size()];
        for (int a = 0; a < recipeHash.size(); a++) hashes[a] = recipeHash.get(a);
        tag.putIntArray("recipes", hashes);

        ListTag players = new ListTag();
        for (String s : recipePlayer) players.add(StringTag.valueOf(ThaumatoriumRules.normalizeOwner(s)));
        tag.put("OutputPlayer", players);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        essentia.readFromNBT(tag);

        maxRecipes = ThaumatoriumRules.normalizeMaxRecipes(tag.getByte("maxrec"));

        List<Integer> previousHashes = new ArrayList<>(recipeHash);
        boolean wasResolved = recipesResolved;

        recipeEssentia = new ArrayList<>();
        recipeHash = new ArrayList<>();
        recipePlayer = new ArrayList<>();
        clearCurrentRecipe();

        if (tag.contains("recipes")) {
            int[] hashes = tag.getIntArray("recipes");
            for (int hash : hashes) {
                // Never drop a hash just because the recipe catalogue is not ready yet:
                // it is resolved again once the bridge exists (client tick / server tick).
                CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(hash);
                recipeHash.add(hash);
                recipeEssentia.add(recipe != null ? recipe.getAspects().copy() : new AspectList());
                recipePlayer.add("");
            }
        }

        if (tag.contains("OutputPlayer")) {
            ListTag list = tag.getList("OutputPlayer", 8);
            for (int a = 0; a < list.size() && a < recipePlayer.size(); a++) {
                recipePlayer.set(a, list.getString(a));
            }
        }

        // An essence-only sync must not force a catalogue rebuild on every tick.
        recipesResolved = wasResolved && RecipeCatalogResolver.sameHashes(recipeHash, previousHashes);
    }

    @Nullable
    public Direction getFacing() {
        if (level == null) return Direction.NORTH;

        BlockState st = getBlockState();

        if (st.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return st.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        return Direction.NORTH;
    }

    /**
     * @return true once all stored hashes have a matching recipe definition from the
     * current catalogue version; a datapack reload invalidates this flag
     */
    public boolean areRecipesResolved() {
        return recipesResolved
                && resolvedCatalogVersion == arcaneresearch.common.lib.crafting
                        .ArcaneresearchCraftingManager.catalogVersion();
    }

    /**
     * Rebuilds recipe aspects from the catalogue. Called on both sides after the
     * crucible bridge is created, so hashes received before it existed are not lost.
     */
    public void resolveRecipes() {
        if (areRecipesResolved()) return;

        recipesResolved = RecipeCatalogResolver.resolve(recipeHash, recipeEssentia, recipePlayer,
                hash -> {
                    CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(hash);
                    return recipe != null ? recipe.getAspects().copy() : null;
                }, new AspectList());
        if (recipesResolved) {
            resolvedCatalogVersion = arcaneresearch.common.lib.crafting
                    .ArcaneresearchCraftingManager.catalogVersion();
        }
    }

    /** Clears all state tied to a recipe that is no longer available. */
    public void clearCurrentRecipe() {
        currentCraft = -1;
        currentRecipe = null;
        currentSuction = null;
    }

    private void ensureTopBlock() {
        if (level == null || level.isClientSide) return;

        BlockPos above = worldPosition.above();
        BlockState aboveState = level.getBlockState(above);

        if (!aboveState.is(ModBlocks.THAUMATORIUM_TOP.get()) && aboveState.isAir()) {
            level.setBlock(above, ModBlocks.THAUMATORIUM_TOP.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    boolean checkHeat() {
        if (level == null) return false;

        BlockState bs = level.getBlockState(worldPosition.below(2));

        return bs.is(Blocks.LAVA) || bs.is(Blocks.FIRE) || bs.is(Blocks.SOUL_FIRE)
                || bs.is(Blocks.MAGMA_BLOCK)
                || bs.getBlock() instanceof arcaneresearch.common.blocks.misc.BlockNitor;
    }

    public ItemStack getCurrentOutputRecipe() {
        if (recipeHash == null || !ThaumatoriumRules.isRecipeSlotValid(currentCraft, recipeHash.size())) {
            return ItemStack.EMPTY;
        }

        CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(recipeHash.get(currentCraft));
        return recipe != null ? recipe.getRecipeOutput().copy() : ItemStack.EMPTY;
    }

    public boolean gettingPower() {
        if (level == null) return false;

        return level.getBestNeighborSignal(worldPosition) > 0
                || level.getBestNeighborSignal(worldPosition.below()) > 0
                || level.getBestNeighborSignal(worldPosition.above()) > 0;
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.getX() - 0.1, worldPosition.getY() - 0.1, worldPosition.getZ() - 0.1,
                    worldPosition.getX() + 1.1, worldPosition.getY() + 2.1, worldPosition.getZ() + 1.1
            );
        }
        return renderBox;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id >= 0) {
            if (level != null && level.isClientSide) venting = ThaumatoriumRules.VENT_DURATION;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void completeRecipe() {
        if (level == null || level.isClientSide) return;
        if (currentRecipe == null || !ThaumatoriumRules.isRecipeSlotValid(currentCraft, recipeHash.size())) return;
        if (!currentRecipe.matches(essentia, getItem(0))) return;

        ItemStack in = getItem(0);
        if (in.isEmpty()) return;

        in.shrink(1);
        setItem(0, in.isEmpty() ? ItemStack.EMPTY : in);
        essentia = new AspectList();

        ItemStack dropped = getCurrentOutputRecipe();

        if (currentCraft < recipePlayer.size()) {
            String ownerName = recipePlayer.get(currentCraft);
            if (ownerName != null && !ownerName.isEmpty()) {
                for (Player p : level.players()) {
                    if (p.getName().getString().equals(ownerName)) {
                        net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(
                                p, dropped.copy(), new SimpleContainer(in.copy()));
                        break;
                    }
                }
            }
        }

        Direction facing = getFacing();
        if (facing == null) facing = Direction.NORTH;

        if (!dropped.isEmpty()) {
            InventoryUtils.ejectStackAt(level, worldPosition, facing, dropped);
        }

        level.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25f,
                2.6f + (level.random.nextFloat() - level.random.nextFloat()) * 0.8f);

        clearCurrentRecipe();
        syncTile(false);
        setChanged();
    }

    void fill() {
        if (level == null || currentSuction == null) return;

        Direction facing = getFacing();
        int suction = ThaumatoriumRules.SUCTION;

        for (int y = 0; y <= 1; y++) {
            for (Direction dir : DIRECTIONS) {
                if (!ThaumatoriumRules.canPullFrom(y, dir == facing,
                        dir == Direction.DOWN, dir == Direction.UP)) continue;

                BlockPos from = worldPosition.above(y);
                BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, from, dir);

                if (!(te instanceof IEssentiaTransport ic)) continue;

                Direction opposite = dir.getOpposite();

                if (ic.getEssentiaAmount(opposite) > 0
                        && ic.getSuctionAmount(opposite) < suction
                        && suction >= ic.getMinimumSuction()) {
                    int ess = ic.takeEssentia(currentSuction, 1, opposite);

                    if (ess > 0) {
                        addToContainer(currentSuction, ess);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public AspectList getAspects() {
        return essentia;
    }

    @Override
    public void setAspects(AspectList aspects) {
        essentia = aspects;
    }

    @Override
    public int addToContainer(Aspect tt, int am) {
        if (currentRecipe == null) return am;

        int required = currentRecipe.getAspects().getAmount(tt);
        if (!ThaumatoriumRules.canAcceptEssence(essentia.getAmount(tt), required)) return am;
        int ce = required - essentia.getAmount(tt);

        int add = Math.min(ce, am);
        essentia.add(tt, add);
        syncTile(false);
        setChanged();

        return am - add;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (essentia.getAmount(tt) < am) return false;

        essentia.remove(tt, am);
        syncTile(false);
        setChanged();
        return true;
    }

    @Override
    @Deprecated
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    @Deprecated
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tt, int am) {
        return essentia.getAmount(tt) >= am;
    }

    @Override
    public int containerContains(Aspect tt) {
        return essentia.getAmount(tt);
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != getFacing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
        currentSuction = aspect;
    }

    @Override
    public Aspect getSuctionType(Direction loc) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(Direction loc) {
        return currentSuction != null ? ThaumatoriumRules.SUCTION : 0;
    }

    @Override
    public Aspect getEssentiaType(Direction loc) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction loc) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction face) {
        int allowed = ThaumatoriumRules.takeEssentia(containerContains(aspect), amount, canOutputTo(face));
        return allowed > 0 && takeFromContainer(aspect, allowed) ? allowed : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        int accepted = ThaumatoriumRules.acceptEssentia(amount, canInputFrom(face));
        return accepted > 0 ? accepted - addToContainer(aspect, accepted) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean isBlocked() {
        return IEssentiaTransport.super.isBlocked();
    }

    /** Recounts adjacent mnemonic matrices and trims the recipe lists to the new capacity. */
    public void getUpgrades() {
        if (level == null) return;

        Direction facing = getFacing();
        int brainBoxes = 0;

        for (int yy = 0; yy <= 1; yy++) {
            for (Direction dir : DIRECTIONS) {
                if (dir == Direction.DOWN || dir == facing) continue;

                BlockPos bp = worldPosition.offset(
                        dir.getStepX(),
                        yy + dir.getStepY(),
                        dir.getStepZ()
                );

                BlockState bs = level.getBlockState(bp);

                if (bs.is(ModBlocks.BRAIN_BOX.get())
                        && bs.hasProperty(BlockBrainBox.FACING)
                        && bs.getValue(BlockBrainBox.FACING) == dir.getOpposite()) {
                    brainBoxes++;
                }
            }
        }

        int mr = ThaumatoriumRules.recipeCapacity(brainBoxes);
        if (mr == maxRecipes) return;

        maxRecipes = mr;

        int toRemove = ThaumatoriumRules.trimCount(recipeHash.size(), maxRecipes);
        for (int i = 0; i < toRemove; i++) {
            int last = recipeHash.size() - 1;
            recipeHash.remove(last);
            recipeEssentia.remove(last);
            recipePlayer.remove(last);
        }

        if (!ThaumatoriumRules.isRecipeSlotValid(currentCraft, recipeHash.size())) {
            clearCurrentRecipe();
        }

        syncTile(false);
        setChanged();
    }

    /**
     * Rebuilds the recipe list shown in the GUI for the current catalyst.
     *
     * @param player player whose research progress is checked
     */
    public void updateRecipes(Player player) {
        recipes.clear();

        if (level == null) {
            recipes = new ArrayList<>();
            return;
        }

        arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager.ensureCrucibleBridge(level);
        resolveRecipes();

        ItemStack catalyst = getItem(0);

        if (player == null || catalyst.isEmpty() || recipeHash == null) {
            return;
        }

        ArrayList<CrucibleRecipe> recipesTemp = new ArrayList<>();

        for (Object r : ArcaneresearchApi.getCraftingRecipes().values()) {
            if (!(r instanceof CrucibleRecipe creps)) continue;

            if (ArcaneresearchCapabilities.knowsResearchStrict(player, creps.getResearch())
                    && creps.catalystMatches(catalyst)) {
                recipesTemp.add(creps);
                continue;
            }

            for (Integer hash : recipeHash) {
                if (creps.hash == hash) {
                    recipesTemp.add(creps);
                }
            }
        }

        recipesTemp.sort(new RecipeOutputComparator());
        recipes = recipesTemp;
    }

    /** @return stored hashes not in the GUI list, followed by the GUI recipes' hashes */
    public ArrayList<Integer> generateRecipeHashlist() {
        ArrayList<Integer> hashList = new ArrayList<>();

        outer:
        for (int hash : recipeHash) {
            for (CrucibleRecipe cr : recipes) {
                if (cr.hash == hash) continue outer;
            }
            hashList.add(hash);
        }

        for (CrucibleRecipe cr2 : recipes) hashList.add(cr2.hash);

        return hashList;
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (eventHandler == null) return;

        try {
            eventHandler.slotsChanged(this);
        } catch (Exception ignored) {
        }
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        return index == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.thaumatorium");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new ThaumatoriumMenu(containerId, playerInventory, this);
    }

    public AspectList getStoredAspects() {
        return essentia;
    }

    @Nullable
    public AspectList getRecipeAspects() {
        if (ThaumatoriumRules.isRecipeSlotValid(currentCraft, recipeEssentia.size())) {
            return recipeEssentia.get(currentCraft);
        }
        return null;
    }

    private static class RecipeOutputComparator implements Comparator<CrucibleRecipe> {
        @Override
        public int compare(CrucibleRecipe a, CrucibleRecipe b) {
            if (a.equals(b)) return 0;
            return a.getRecipeOutput().getHoverName().getString()
                    .compareTo(b.getRecipeOutput().getHoverName().getString());
        }
    }
}