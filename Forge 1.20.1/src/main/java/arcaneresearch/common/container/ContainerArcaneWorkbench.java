package arcaneresearch.common.container;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.blocks.world.ore.ShardType;
import arcaneresearch.common.container.slot.SlotCraftingArcaneWorkbench;
import arcaneresearch.common.container.slot.SlotCrystal;
import arcaneresearch.common.items.casters.CasterManager;
import arcaneresearch.common.lib.crafting.ArcaneWorkbenchCraftingContainer;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModMenuTypes;
@Deprecated
public class ContainerArcaneWorkbench extends AbstractContainerMenu {
    public static final int OUTPUT_SLOT = 0;
    public static final int GRID_START = 1;
    public static final int GRID_END = 9;
    public static final int CRYSTAL_START = 10;
    public static final int CRYSTAL_END = 15;
    public static final int PLAYER_INV_START = 16;
    public static final int PLAYER_INV_END = 42;
    public static final int PLAYER_HOTBAR_START = 43;
    public static final int PLAYER_HOTBAR_END = 51;
    public static final Aspect[] PRIMAL_ASPECTS = {
            Aspect.AIR, Aspect.FIRE, Aspect.WATER, Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
    };
    public static int[] xx = new int[]{63, 14, 110, 15, 110, 62};
    public static int[] yy = new int[]{10, 35, 35, 94, 94, 117};
    private final ContainerLevelAccess access;
    private final ArcaneWorkbenchCraftingContainer craftMatrix;
    private final ContainerData data;
    public ResultContainer craftResult;
    private TileArcaneWorkbench tileEntity;
    private Inventory ip;
    private int lastVis = -1;
    private long lastCheck = 0L;
    public ContainerArcaneWorkbench(Inventory playerInventory, TileArcaneWorkbench tile) {
        super(ModMenuTypes.ARCANE_WORKBENCH.get(), containerIdOf(playerInventory));
        tileEntity = tile;
        ip = playerInventory;
        this.access = ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos());
        this.craftResult = new ResultContainer();
        this.craftMatrix = new ArcaneWorkbenchCraftingContainer(tile, this);
        this.data = new SimpleContainerData(1);
        addDataSlots(this.data);
        addSlot(new SlotCraftingArcaneWorkbench(tileEntity, ip.player, craftMatrix, craftResult, 0, 160, 64));
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 3; ++col) {
                addSlot(new Slot(craftMatrix, col + row * 3, 40 + col * 23, 40 + row * 24));
            }
        }
        for (ShardType st : ShardType.values()) {
            if (st.getMetadata() < 6) {
                addSlot(new SlotCrystal(st.getAspect(), craftMatrix, st.getMetadata() + 9, xx[st.getMetadata()], yy[st.getMetadata()]));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 16 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, 16 + col * 18, 209));
        }
        slotsChanged(craftMatrix);
    }
    private static int containerIdOf(Inventory inv) {
        return inv.player.containerMenu.containerId;
    }
    protected static void slotChangedCraftingGrid(AbstractContainerMenu menu, Level level,
                                                  Player player, ArcaneWorkbenchCraftingContainer craftMatrix, ResultContainer craftResult) {
        if (level.isClientSide) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ItemStack result = ItemStack.EMPTY;
        IArcaneRecipe arcaneRecipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(craftMatrix, player);
        if (arcaneRecipe != null
                && ArcaneresearchCapabilities.isResearchKnown(player, arcaneRecipe.getResearch())) {
            TileArcaneWorkbench tile = craftMatrix.getTile();
            tile.updateAura();
            int visCost = getDiscountedVisCost(arcaneRecipe, player);
            boolean hasVis = tile.auraVisServer >= visCost;
            boolean hasCrystals = hasRequiredCrystals(craftMatrix, arcaneRecipe.getCrystals());
            if (hasVis && hasCrystals) {
                craftResult.setRecipeUsed(arcaneRecipe);
                result = arcaneRecipe.assemble(craftMatrix, level.registryAccess());
            }
        } else {
            var vanillaRecipe = level.getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, gridOnlyCopy(menu, craftMatrix), level);
            if (vanillaRecipe.isPresent()) {
                boolean limited = level.getGameRules().getBoolean(
                        net.minecraft.world.level.GameRules.RULE_LIMITED_CRAFTING);
                if (!limited || serverPlayer.getRecipeBook().contains(vanillaRecipe.get())) {
                    craftResult.setRecipeUsed(vanillaRecipe.get());
                    result = vanillaRecipe.get().assemble(craftMatrix, level.registryAccess());
                }
            }
        }
        craftResult.setItem(0, result);
        menu.setRemoteSlot(0, result);
        serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId,
                menu.incrementStateId(), 0, result));
    }
    public static net.minecraft.world.inventory.CraftingContainer gridOnlyCopy(
            AbstractContainerMenu menu, net.minecraft.world.inventory.CraftingContainer craftMatrix) {
        return new GridCopy(craftMatrix);
    }
    public static int getDiscountedVisCost(IArcaneRecipe recipe, Player player) {
        int base = recipe.getVis();
        if (base <= 0) {
            return 0;
        }
        float discount = CasterManager.getTotalVisDiscount(player);
        return (int) (base * (1.0f - discount));
    }
    public static boolean hasRequiredCrystals(ArcaneWorkbenchCraftingContainer craftMatrix, AspectList crystals) {
        if (crystals == null || crystals.size() <= 0) {
            return true;
        }
        for (Aspect aspect : crystals.getAspects()) {
            int required = crystals.getAmount(aspect);
            ItemStack wanted = ArcaneresearchApiHelper.makeCrystal(aspect, required);
            int found = 0;
            for (int slot = 9; slot < 15; slot++) {
                ItemStack stack = craftMatrix.getItem(slot);
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(wanted, stack)) {
                    found += stack.getCount();
                    if (found >= required) {
                        break;
                    }
                }
            }
            if (found < required) {
                return false;
            }
        }
        return true;
    }
    @Override
    public void slotsChanged(Container container) {
        access.execute((level, pos) -> slotChangedCraftingGrid(this, level, ip.player, craftMatrix, craftResult));
    }
    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        long currentTime = System.currentTimeMillis();
        if (currentTime > lastCheck) {
            lastCheck = currentTime + 500L;
            tileEntity.updateAura();
        }
        if (lastVis != tileEntity.auraVisServer) {
            slotsChanged(craftMatrix);
            data.set(0, tileEntity.auraVisServer);
            lastVis = tileEntity.auraVisServer;
        }
    }
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ARCANE_WORKBENCH.get());
    }
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            if (index == OUTPUT_SLOT) {
                if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_END + 1, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, result);
            } else if (index >= PLAYER_INV_START && index <= PLAYER_HOTBAR_END) {
                for (int i = 0; i < 6; i++) {
                    if (SlotCrystal.isValidCrystal(stackInSlot, PRIMAL_ASPECTS[i])) {
                        if (!moveItemStackTo(stackInSlot, CRYSTAL_START + i, CRYSTAL_START + i + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                        if (stackInSlot.isEmpty()) {
                            break;
                        }
                    }
                }
                if (!stackInSlot.isEmpty()) {
                    if (index < PLAYER_HOTBAR_START) {
                        if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            } else if (!moveItemStackTo(stackInSlot, PLAYER_INV_START, PLAYER_HOTBAR_END + 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stackInSlot);
        }
        return result;
    }
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftResult && super.canTakeItemForPickAll(stack, slot);
    }
    @Override
    public void removed(Player player) {
        super.removed(player);
    }
    public TileArcaneWorkbench getBlockEntity() {
        return tileEntity;
    }
    public int getAuraVis() {
        return data.get(0);
    }
    public ArcaneWorkbenchCraftingContainer getCraftMatrix() {
        return craftMatrix;
    }
    private static class GridCopy implements net.minecraft.world.inventory.CraftingContainer {
        private final net.minecraft.core.NonNullList<ItemStack> items =
                net.minecraft.core.NonNullList.withSize(9, ItemStack.EMPTY);
        GridCopy(net.minecraft.world.inventory.CraftingContainer source) {
            for (int i = 0; i < 9; i++) {
                items.set(i, source.getItem(i).copy());
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
        public java.util.List<ItemStack> getItems() {
            return items;
        }
        @Override
        public int getContainerSize() {
            return 9;
        }
        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public ItemStack getItem(int slot) {
            return (slot >= 0 && slot < 9) ? items.get(slot) : ItemStack.EMPTY;
        }
        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot < 0 || slot >= 9) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
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
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot < 0 || slot >= 9) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = items.get(slot);
            items.set(slot, ItemStack.EMPTY);
            return stack;
        }
        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot >= 0 && slot < 9) {
                items.set(slot, stack);
            }
        }
        @Override
        public void setChanged() {
        }
        @Override
        public boolean stillValid(Player player) {
            return true;
        }
        @Override
        public void clearContent() {
            items.clear();
        }
        @Override
        public void fillStackedContents(net.minecraft.world.entity.player.StackedContents contents) {
            for (ItemStack stack : items) {
                contents.accountStack(stack);
            }
        }
    }
}
