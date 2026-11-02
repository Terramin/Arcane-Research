package arcaneresearch.common.menu;

import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.items.casters.CasterManager;
import arcaneresearch.common.lib.crafting.ArcaneWorkbenchCraftingContainer;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.slot.ArcaneWorkbenchResultSlot;
import arcaneresearch.common.menu.slot.CrystalSlot;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArcaneWorkbenchMenu extends AbstractContainerMenu {

    public static final int OUTPUT_SLOT = 0;
    public static final int GRID_START = 1;
    public static final int GRID_END = 9;
    public static final int CRYSTAL_START = 10;
    public static final int CRYSTAL_END = 15;
    public static final int PLAYER_INV_START = 16;
    public static final int PLAYER_INV_END = 42;
    public static final int PLAYER_HOTBAR_START = 43;
    public static final int PLAYER_HOTBAR_END = 51;

    public static final int[] CRYSTAL_X = {63, 14, 110, 15, 110, 62};
    public static final int[] CRYSTAL_Y = {10, 35, 35, 94, 94, 117};
    public static final int GRID_X0 = 40;
    public static final int GRID_DX = 23;
    public static final int GRID_Y0 = 40;
    public static final int GRID_DY = 24;

    public static final Aspect[] PRIMAL_ASPECTS = {
            Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
    };

    private final TileArcaneWorkbench blockEntity;
    private final ContainerLevelAccess access;
    private final Player player;
    private final ArcaneWorkbenchCraftingContainer craftMatrix;
    private final ResultContainer craftResult;
    private final ContainerData data;
    private int lastVis = -1;
    private long lastCheck = 0L;

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData));
    }

    public ArcaneWorkbenchMenu(int containerId, Inventory playerInventory, TileArcaneWorkbench blockEntity) {
        super(ModMenuTypes.ARCANE_WORKBENCH.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;

        Level level = blockEntity.getLevel();
        this.access = level != null
                ? ContainerLevelAccess.create(level, blockEntity.getBlockPos())
                : ContainerLevelAccess.NULL;

        this.craftResult = new ResultContainer();
        this.craftMatrix = new ArcaneWorkbenchCraftingContainer(blockEntity, this);
        this.data = new SimpleContainerData(1);
        addDataSlots(this.data);

        addSlot(new ArcaneWorkbenchResultSlot(blockEntity, player, craftMatrix, craftResult, 0, 160, 64));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftMatrix, col + row * 3,
                        GRID_X0 + col * GRID_DX, GRID_Y0 + row * GRID_DY));
            }
        }
        for (int i = 0; i < 6; i++) {
            addSlot(new CrystalSlot(PRIMAL_ASPECTS[i], craftMatrix, 9 + i, CRYSTAL_X[i], CRYSTAL_Y[i]));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 16 + col * 18, 209));
        }
        slotsChanged(craftMatrix);
    }

    private static TileArcaneWorkbench getBlockEntity(Inventory playerInventory, FriendlyByteBuf extraData) {
        BlockEntity be = playerInventory.player.level().getBlockEntity(extraData.readBlockPos());
        if (be instanceof TileArcaneWorkbench tile) return tile;
        throw new IllegalStateException("Block entity is not a TileArcaneWorkbench: " + be);
    }

    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu,@NotNull Level level,
                                                  @NotNull Player player,
                                                  ArcaneWorkbenchCraftingContainer craftMatrix,
                                                  ResultContainer craftResult) {
        if (level.isClientSide) return;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        ItemStack result = ItemStack.EMPTY;
        IArcaneRecipe arcaneRecipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(craftMatrix, player);

        if (arcaneRecipe != null
                && ArcaneresearchCapabilities.isResearchKnown(player, arcaneRecipe.getResearch())) {
            TileArcaneWorkbench tile = craftMatrix.getTile();
            if (tile != null) {
                tile.updateAura();
                int visCost = getDiscountedVisCost(arcaneRecipe, player);
                boolean hasVis = tile.auraVisServer >= visCost;
                boolean hasCrystals = hasRequiredCrystals(craftMatrix, arcaneRecipe.getCrystals());
                if ((hasVis && hasCrystals) || player.getAbilities().instabuild) {
                    craftResult.setRecipeUsed(arcaneRecipe);
                    result = arcaneRecipe.assemble(craftMatrix, level.registryAccess());
                }
            }
        } else {
            var vanillaRecipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, gridOnlyCopy(menu, craftMatrix), level);
            if (vanillaRecipe.isPresent()) {
                boolean limited = level.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING);
                if (!limited || serverPlayer.getRecipeBook().contains(vanillaRecipe.get())) {
                    craftResult.setRecipeUsed(vanillaRecipe.get());
                    result = vanillaRecipe.get().assemble(craftMatrix, level.registryAccess());
                }
            }
        }

        craftResult.setItem(0, result);
        menu.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, menu.incrementStateId(), 0, result));
    }

    public static CraftingContainer gridOnlyCopy(AbstractContainerMenu menu, CraftingContainer craftMatrix) {
        return new GridCopy(craftMatrix);
    }

    public static int getDiscountedVisCost(IArcaneRecipe recipe, Player player) {
        int base = recipe.getVis();
        if (base <= 0) return 0;
        float discount = CasterManager.getTotalVisDiscount(player);
        if (discount < 0f) discount = 0f;
        if (discount > 1f) discount = 1f;
        return (int) (base * (1.0f - discount));
    }

    public static boolean hasRequiredCrystals(ArcaneWorkbenchCraftingContainer craftMatrix, AspectList crystals) {
        if (crystals == null || crystals.size() <= 0) return true;
        if (craftMatrix == null) return false;

        for (Aspect aspect : crystals.getAspects()) {
            if (aspect == null) continue;
            int required = crystals.getAmount(aspect);
            if (required <= 0) continue;

            ItemStack wanted = ArcaneresearchApiHelper.makeCrystal(aspect, required);
            int found = 0;
            for (int slot = 9; slot < 15; slot++) {
                @NotNull ItemStack stack = craftMatrix.getItem(slot);
                if (stack.isEmpty()) continue;
                if (ItemStack.isSameItemSameTags(wanted, stack)) {
                    found += stack.getCount();
                    if (found >= required) break;
                }
            }
            if (found < required) return false;
        }
        return true;
    }

    @Override
    public void slotsChanged(@NotNull Container container) {
        access.execute((level, pos) -> slotChangedCraftingGrid(this, level, player, craftMatrix, craftResult));
    }

    @Override
    public void broadcastChanges() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastCheck) {
            lastCheck = currentTime + 500L;
            blockEntity.updateAura();
        }
        if (lastVis != blockEntity.auraVisServer) {
            slotsChanged(craftMatrix);
            data.set(0, blockEntity.auraVisServer);
            lastVis = blockEntity.auraVisServer;
        }
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (blockEntity == null) return false;
        Level level = blockEntity.getLevel();
        if (level == null) return false;
        return stillValid(access, player, ModBlocks.ARCANE_WORKBENCH.get());
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        final int total = this.slots.size();
        if (index < 0 || index >= total) return ItemStack.EMPTY;

        @NotNull Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        if (index == OUTPUT_SLOT) {
            result = ItemStack.EMPTY;
            for (int n = 0; n < 64; n++) {
                ItemStack out = slot.getItem();
                if (out.isEmpty()) break;

                int before = out.getCount();
                ItemStack moved = out.copy();
                if (!moveItemStackTo(moved, PLAYER_INV_START, total, true)) break;

                int craftedNow = before - moved.getCount();
                if (craftedNow <= 0) break;

                if (result.isEmpty()) {
                    result = out.copy();
                    result.setCount(0);
                }
                result.grow(craftedNow);
                slot.onQuickCraft(out, result);
                slot.onTake(player, out);

                if (!moved.isEmpty()) break;
            }
            if (!result.isEmpty()) {
                if (slot.getItem().isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            }
            return result;
        } else if (index >= PLAYER_INV_START) {
            for (int i = 0; i < 6; i++) {
                if (CrystalSlot.isValidCrystal(stackInSlot, PRIMAL_ASPECTS[i])) {
                    moveItemStackTo(stackInSlot, CRYSTAL_START + i, CRYSTAL_START + i + 1, false);
                    if (stackInSlot.isEmpty()) break;
                }
            }
            if (!stackInSlot.isEmpty()) {
                moveItemStackTo(stackInSlot, GRID_START, GRID_END + 1, false);
            }
            if (!stackInSlot.isEmpty()) {
                if (index < PLAYER_HOTBAR_START) {
                    if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, total, false)) return ItemStack.EMPTY;
                } else {
                    if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) return ItemStack.EMPTY;
                }
            }
        } else {
            if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, total, false)) return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stackInSlot.getCount() == result.getCount()) return ItemStack.EMPTY;

        slot.onTake(player, stackInSlot);
        return result;
    }

    @Override
    public boolean canTakeItemForPickAll(@NotNull ItemStack stack, Slot slot) {
        return slot.container != craftResult && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
    }

    public TileArcaneWorkbench getBlockEntity() {
        return blockEntity;
    }

    public int getAuraVis() {
        return data.get(0);
    }

    public ArcaneWorkbenchCraftingContainer getCraftMatrix() {
        return craftMatrix;
    }

    private static class GridCopy implements CraftingContainer {

        private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

        GridCopy(CraftingContainer source) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = source.getItem(i);
                items.set(i, stack.copy());
            }
        }

        @Override
        public int getWidth() {
            return 3;
        }

        @Override
        public int getHeight() {
            return 3;
        }

        @Override
        public @NotNull List<ItemStack> getItems() {
            return items;
        }

        @Override
        public int getContainerSize() {
            return 9;
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (stack != null && !stack.isEmpty()) return false;
            }
            return true;
        }

        @Override
        public @NotNull ItemStack getItem(int slot) {
            if (slot < 0 || slot >= 9) return ItemStack.EMPTY;
            return items.get(slot);
        }

        @Override
        public @NotNull ItemStack removeItem(int slot, int amount) {
            if (slot < 0 || slot >= 9 || amount <= 0) return ItemStack.EMPTY;
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) return ItemStack.EMPTY;

            if (stack.getCount() <= amount) {
                items.set(slot, ItemStack.EMPTY);
                return stack;
            }
            ItemStack result = stack.copy();
            result.setCount(amount);
            stack.shrink(amount);
            return result;
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int slot) {
            if (slot < 0 || slot >= 9) return ItemStack.EMPTY;
            ItemStack stack = items.get(slot);
            items.set(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack) {
            if (slot >= 0 && slot < 9) {
                items.set(slot, stack);
            }
        }

        @Override
        public void setChanged() {
        }

        @Override
        public boolean stillValid(@NotNull Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            items.clear();
        }

        @Override
        public void fillStackedContents(@NotNull StackedContents contents) {
            for (ItemStack stack : items) {
                if (stack != null) contents.accountStack(stack);
            }
        }
    }
}