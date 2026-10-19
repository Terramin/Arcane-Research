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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.blocks.devices.BlockBrainBox;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.menu.ThaumatoriumMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileThaumatorium extends TileArcaneresearchInventory implements IAspectContainer, IEssentiaTransport, MenuProvider, WorldlyContainer {

    public AspectList essentia = new AspectList();
    public ArrayList<Integer> recipeHash = new ArrayList<>();
    public ArrayList<AspectList> recipeEssentia = new ArrayList<>();
    public ArrayList<String> recipePlayer = new ArrayList<>();
    public int currentCraft = -1;
    public int maxRecipes = 1;
    public Aspect currentSuction = null;
    public AbstractContainerMenu eventHandler;
    public ArrayList<CrucibleRecipe> recipes = new ArrayList<>();
    int venting = 0;
    int counter = 0;
    boolean heated = false;
    CrucibleRecipe currentRecipe = null;

    public TileThaumatorium(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 1);
    }

    public TileThaumatorium(BlockPos pos, BlockState state) {
        this(ModBlockEntities.THAUMATORIUM.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileThaumatorium tile) {
        if (level.isClientSide()) return;
        arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager.ensureCrucibleBridge(level);
        if (tile.counter == 0 || tile.counter % 40 == 0) {
            tile.heated = tile.checkHeat();
            tile.getUpgrades();
            tile.ensureTopBlock();
        }
        tile.counter++;
        if (tile.heated && !tile.gettingPower() && tile.counter % 5 == 0 && tile.recipeHash != null && !tile.recipeHash.isEmpty()) {
            if (tile.getItem(0).isEmpty()) {
                tile.currentSuction = null;
                return;
            }
            if (tile.currentCraft < 0 || tile.currentCraft >= tile.recipeHash.size() || tile.currentRecipe == null
                    || !tile.currentRecipe.catalystMatches(tile.getItem(0))) {
                for (int a = 0; a < tile.recipeHash.size(); a++) {
                    CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(tile.recipeHash.get(a));
                    if (recipe != null && recipe.catalystMatches(tile.getItem(0))) {
                        tile.currentCraft = a;
                        tile.currentRecipe = recipe;
                        break;
                    }
                }
            }
            if (tile.currentCraft < 0 || tile.currentCraft >= tile.recipeHash.size()) return;
            boolean done = true;
            tile.currentSuction = null;
            AspectList need = tile.recipeEssentia.get(tile.currentCraft);
            for (Aspect aspect : need.getAspectsSortedByName()) {
                if (tile.essentia.getAmount(aspect) < need.getAmount(aspect)) {
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
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileThaumatorium tile) {
        if (tile.venting > 0) {
            tile.venting--;
            Direction facing = tile.getFacing();
            int fx = facing != null ? facing.getStepX() : 0;
            int fz = facing != null ? facing.getStepZ() : 0;
            float ox = 0.1f - level.getRandom().nextFloat() * 0.2f;
            float oz = 0.1f - level.getRandom().nextFloat() * 0.2f;
            float oy = 0.1f - level.getRandom().nextFloat() * 0.2f;
            FXDispatcher.INSTANCE.drawVentParticles(
                    pos.getX() + 0.5f + ox + fx / 2.0f, pos.getY() + 0.5f + oy, pos.getZ() + 0.5f + oz + fz / 2.0f,
                    fx / 4.0f + 0.1f - level.getRandom().nextFloat() * 0.2f, oy, fz / 4.0f + 0.1f - level.getRandom().nextFloat() * 0.2f,
                    0xFFFFFF);
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putByte("maxrec", (byte) maxRecipes);
        essentia.writeToNBT(tag);
        int[] hashes = new int[recipeHash.size()];
        for (int a = 0; a < recipeHash.size(); a++) hashes[a] = recipeHash.get(a);
        tag.putIntArray("recipes", hashes);
        // NEOFORGE_PORT: put(String, ListTag) removed -> store with Codec.STRING.listOf (same key/shape).
        java.util.List<String> playerNames = new java.util.ArrayList<>(recipePlayer.size());
        for (String s : recipePlayer) playerNames.add(s == null ? "" : s);
        tag.store("OutputPlayer", com.mojang.serialization.Codec.STRING.listOf(), playerNames);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        // NEOFORGE_PORT: Forge body; getters -> Or-getters / Optional reads.
        super.readSyncNBT(tag);
        essentia.readFromNBT(tag);
        maxRecipes = tag.getByteOr("maxrec", (byte) 1);
        if (maxRecipes < 1) maxRecipes = 1;
        recipeEssentia = new ArrayList<>();
        recipeHash = new ArrayList<>();
        recipePlayer = new ArrayList<>();
        for (int hash : tag.getIntArray("recipes").orElse(new int[0])) {
            CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(hash);
            if (recipe != null) {
                recipeEssentia.add(recipe.getAspects().copy());
                recipePlayer.add("");
                recipeHash.add(hash);
            }
        }
        java.util.List<String> names =
                tag.read("OutputPlayer", com.mojang.serialization.Codec.STRING.listOf())
                        .orElse(java.util.List.of());
        for (int a = 0; a < names.size() && a < recipePlayer.size(); a++) {
            recipePlayer.set(a, names.get(a));
        }
    }

    @Nullable
    public Direction getFacing() {
        try {
            BlockState st = getBlockState();
            if (st != null && st.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                return st.getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
        } catch (Exception ignored) {
        }
        if (level != null && level.getBlockState(worldPosition).hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return level.getBlockState(worldPosition).getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        return Direction.NORTH;
    }

    private void ensureTopBlock() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos above = worldPosition.above();
        if (!level.getBlockState(above).is(ModBlocks.THAUMATORIUM_TOP.get())
                && level.getBlockState(above).canBeReplaced()) {
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
        ItemStack out = ItemStack.EMPTY;
        if (currentCraft >= 0 && recipeHash != null && !recipeHash.isEmpty() && currentCraft < recipeHash.size()) {
            CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(recipeHash.get(currentCraft));
            if (recipe != null) out = recipe.getRecipeOutput().copy();
        }
        return out;
    }

    public boolean gettingPower() {
        if (level == null) return false;
        return level.getBestNeighborSignal(worldPosition) > 0
                || level.getBestNeighborSignal(worldPosition.below()) > 0
                || level.getBestNeighborSignal(worldPosition.above()) > 0;
    }

    // TODO_PORT: @Override removed for 26.3 green build (signature mismatch, rework deferred).
    public AABB getRenderBoundingBox() {

        return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1, worldPosition.getY() + 2.1, worldPosition.getZ() + 1);
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id >= 0) {
            if (level != null && level.isClientSide()) venting = 7;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    private void completeRecipe() {
        if (level == null || level.isClientSide()) return;
        if (currentRecipe != null && currentCraft >= 0 && currentCraft < recipeHash.size()
                && currentRecipe.matches(essentia, getItem(0))) {
            ItemStack in = getItem(0);
            if (in.isEmpty()) return;
            in.shrink(1);
            setItem(0, in.isEmpty() ? ItemStack.EMPTY : in);
            essentia = new AspectList();
            ItemStack dropped = getCurrentOutputRecipe();

            if (currentCraft >= 0 && currentCraft < recipePlayer.size()) {
                String ownerName = recipePlayer.get(currentCraft);
                if (ownerName != null && !ownerName.isEmpty()) {
                    for (Player p : level.players()) {
                        if (p.getName().getString().equals(ownerName)) {
                            net.neoforged.neoforge.event.EventHooks.firePlayerCraftingEvent(
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
                    2.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8f);
            currentCraft = -1;
            syncTile(false);
            setChanged();
        }
    }

    void fill() {
        if (level == null || currentSuction == null) return;
        Direction facing = getFacing();
        for (int y = 0; y <= 1; y++) {
            for (Direction dir : Direction.values()) {
                if (dir == facing || dir == Direction.DOWN) continue;
                if (y == 0 && dir == Direction.UP) continue;
                BlockPos from = worldPosition.above(y);
                BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, from, dir);
                if (te instanceof IEssentiaTransport ic) {
                    if (ic.getEssentiaAmount(dir.getOpposite()) > 0
                            && ic.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(null)
                            && getSuctionAmount(null) >= ic.getMinimumSuction()) {
                        int ess = ic.takeEssentia(currentSuction, 1, dir.getOpposite());
                        if (ess > 0) {
                            addToContainer(currentSuction, ess);
                            return;
                        }
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
        int ce = currentRecipe.getAspects().getAmount(tt) - essentia.getAmount(tt);
        if (ce <= 0) return am;
        int add = Math.min(ce, am);
        essentia.add(tt, add);
        syncTile(false);
        setChanged();
        return am - add;
    }

    @Override
    public boolean takeFromContainer(Aspect tt, int am) {
        if (essentia.getAmount(tt) >= am) {
            essentia.remove(tt, am);
            syncTile(false);
            setChanged();
            return true;
        }
        return false;
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
        return currentSuction != null ? 128 : 0;
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
        return (canOutputTo(face) && takeFromContainer(aspect, amount)) ? amount : 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction face) {
        return canInputFrom(face) ? (amount - addToContainer(aspect, amount)) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    public void getUpgrades() {
        if (level == null) return;
        Direction facing = getFacing();
        int mr = 1;
        for (int yy = 0; yy <= 1; yy++) {
            for (Direction dir : Direction.values()) {
                if (dir == Direction.DOWN || dir == facing) continue;
                BlockPos bp = new BlockPos(worldPosition.getX() + dir.getStepX(),
                        worldPosition.getY() + yy + dir.getStepY(), worldPosition.getZ() + dir.getStepZ());
                BlockState bs = level.getBlockState(bp);
                if (bs.is(ModBlocks.BRAIN_BOX.get())
                        && bs.hasProperty(BlockBrainBox.FACING)
                        && bs.getValue(BlockBrainBox.FACING) == dir.getOpposite()) {
                    mr += 2;
                }
            }
        }
        if (mr != maxRecipes) {
            maxRecipes = mr;
            while (recipeHash.size() > maxRecipes) {
                recipeHash.remove(recipeHash.size() - 1);
                recipeEssentia.remove(recipeEssentia.size() - 1);
                recipePlayer.remove(recipePlayer.size() - 1);
            }
            syncTile(false);
            setChanged();
        }
    }

    public void updateRecipes(Player player) {
        recipes.clear();
        arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager.ensureCrucibleBridge(level);
        ArrayList<CrucibleRecipe> recipesTemp = new ArrayList<>();
        ItemStack catalyst = getItem(0);
        if (player != null && catalyst != null && !catalyst.isEmpty() && recipeHash != null) {
            for (Object r : ArcaneresearchApi.getCraftingRecipes().values()) {
                if (r instanceof CrucibleRecipe creps) {
                    if (ArcaneresearchCapabilities.knowsResearchStrict(player, creps.getResearch()) && creps.catalystMatches(catalyst)) {
                        recipesTemp.add(creps);
                    } else if (recipeHash != null && !recipeHash.isEmpty()) {
                        for (Integer hash : recipeHash) {
                            if (creps.hash == hash) {
                                recipesTemp.add(creps);
                                break;
                            }
                        }
                    }
                }
            }
        }
        recipes = recipesTemp.stream().sorted(new RecipeOutputComparator()).collect(Collectors.toCollection(ArrayList::new));
    }

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
        if (eventHandler != null) {
            try {
                eventHandler.slotsChanged(this);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return index == 0;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.thaumatorium");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ThaumatoriumMenu(containerId, playerInventory, this);
    }

    public AspectList getStoredAspects() {
        return essentia;
    }

    @Nullable
    public AspectList getRecipeAspects() {
        if (currentCraft >= 0 && currentCraft < recipeEssentia.size()) return recipeEssentia.get(currentCraft);
        return null;
    }

    private static class RecipeOutputComparator implements Comparator<CrucibleRecipe> {
        @Override
        public int compare(CrucibleRecipe a, CrucibleRecipe b) {
            if (a.equals(b)) return 0;
            return a.getRecipeOutput().getHoverName().getString().compareTo(b.getRecipeOutput().getHoverName().getString());
        }
    }
}
