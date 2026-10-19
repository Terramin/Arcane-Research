package arcaneresearch.discovered.table;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;

/**
 * Тайл hex-стола (MAIN-половина): 2 слота (0 — инструменты писца,
 * 1 — записка), зеркало слотов {@code CognitionNoteTableTile} в миниатюре.
 * Инвентарь персистентен (в отличие от меню-прототипа на SimpleContainer).
 *
 * NEOFORGE_PORT: saveAdditional/load(CompoundTag) -&gt; saveAdditional(ValueOutput) /
 * loadAdditional(ValueInput) (items + bonus через те же label); синк на клиент —
 * getUpdateTag(registries) через TagValueOutput (как TileBanner) + getUpdatePacket
 * (onDataPacket/handleUpdateTag удалены); isClientSide-поля -&gt; isClientSide();
 * Level.isDay удалён -&gt; окно LevelData.getGameTime (как CognitionNoteTableTile).
 */
public class HexNoteTableTile extends BlockEntity implements Container, MenuProvider {

    public static final int INK_SLOT = 0;
    public static final int NOTE_SLOT = 1;
    public static final int SIZE = 2;

    /** Пересчёт бонуса окружения каждые N тиков (паритет nextRecalc). */
    private static final int BONUS_RECALC_TICKS = 600;
    /** Кап бонусных очков на аспект. */
    private static final int BONUS_CAP = 5;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    /**
     * Бонусные очки окружения (паритет {@code TileResearchTable.bonusAspects}):
     * тратятся после кошелька игрока, персистентны, пересчитываются
     * периодически (см. {@link #recalculateBonus}).
     */
    private final AspectList bonusAspects = new AspectList();
    private int ticks;

    public HexNoteTableTile(BlockPos pos, BlockState state) {
        super(HexTableRegistry.TILE, pos, state);
    }

    // ---- Container ----

    @Override
    public int getContainerSize() {
        return SIZE;
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
        return slot >= 0 && slot < SIZE ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) {
            setChanged();
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SIZE) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        // BER читает клиентский тайл: без явного апдейта после перезахода
        // он пустой (предметы есть только на сервере) и перо/свиток/
        // таумометр не рисуются, пока что-то не дёрнет синк.
        Level level = getLevel();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(),
                    Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = getLevel();
        if (level == null) {
            return false;
        }
        return player.distanceToSqr(
                worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == INK_SLOT) {
            return HexResearchTableMenu.isInkStack(stack);
        }
        if (slot == NOTE_SLOT) {
            return HexResearchTableMenu.isNoteStack(stack);
        }
        return false;
    }

    public void dropContents() {
        Level level = getLevel();
        if (level != null && !level.isClientSide()) {
            Containers.dropContents(level, worldPosition, this);
        }
    }

    // ---- бонус окружения ----

    public AspectList getBonus() {
        return bonusAspects;
    }

    /** Списать бонусное очко (после кошелька). Возвращает снятое (0/1). */
    public int takeBonus(Aspect aspect) {
        if (aspect == null || bonusAspects.getAmount(aspect) <= 0) {
            return 0;
        }
        bonusAspects.remove(aspect, 1);
        setChanged();
        return 1;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        if (level.isClientSide() || !(be instanceof HexNoteTableTile tile)) {
            return;
        }
        if (++tile.ticks >= BONUS_RECALC_TICKS) {
            tile.ticks = 0;
            tile.recalculateBonus();
        }
    }

    /**
     * Пересчёт бонуса (подмножество recalculateBonus стола 1:
     * свет/небо/высота/погода/сущности + семпл блоков 21³ через
     * {@code AspectHelper.getObjectAspects}, кап на аспект).
     */
    public void recalculateBonus() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        bonusAspects.aspects.clear();
        BlockPos pos = worldPosition;
        addCapped(Aspect.getAspect("tenebrae"),
                level.getMaxLocalRawBrightness(pos) < 9 ? 2 : 0);
        addCapped(Aspect.getAspect("lux"),
                level.getLevelData().getGameTime() % 24000L < 12000L && level.canSeeSky(pos.above()) ? 2 : 0);
        if (pos.getY() > 100) {
            addCapped(Aspect.getAspect("aer"), 3);
        }
        if (level.isRainingAt(pos.above())) {
            addCapped(Aspect.getAspect("aqua"), 2);
            addCapped(Aspect.getAspect("potentia"), 2);
            addCapped(Aspect.getAspect("motus"), 2);
            if (level.isThundering()) {
                addCapped(Aspect.getAspect("potentia"), 2);
            }
        }
        int beasts = 0;
        int men = 0;
        try {
            for (net.minecraft.world.entity.LivingEntity e : level.getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class, new AABB(pos).inflate(15))) {
                if (e instanceof net.minecraft.world.entity.player.Player) {
                    men++;
                } else {
                    beasts++;
                }
            }
        } catch (Exception ignored) {
        }
        addCapped(Aspect.getAspect("bestia"), Math.min(5, beasts / 5));
        addCapped(Aspect.getAspect("humanus"), Math.min(3, men));
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    if ((x + y + z) % 3 != 0) {
                        continue;
                    }
                    try {
                        ItemStack asStack = new ItemStack(
                                level.getBlockState(pos.offset(x, y, z)).getBlock());
                        AspectList list = AspectHelper.getObjectAspects(asStack);
                        if (list != null) {
                            for (Aspect aspect : list.getAspects()) {
                                addCapped(aspect, 1);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        setChanged();
    }

    private void addCapped(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) {
            return;
        }
        int have = bonusAspects.getAmount(aspect);
        if (have < BONUS_CAP) {
            bonusAspects.add(aspect, Math.min(amount, BONUS_CAP - have));
        }
    }

    // ---- NBT ----

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        bonusAspects.writeToNBT(tag, "bonus");
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        ContainerHelper.loadAllItems(tag, items);
        bonusAspects.aspects.clear();
        try {
            bonusAspects.readFromNBT(tag, "bonus");
        } catch (Exception ignored) {
        }
    }

    // ---- синк на клиент (инвентарь + бонус для BER) ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        // NEOFORGE_PORT: saveAdditional(CompoundTag) удалён -> ValueOutput;
        // обёртка через TagValueOutput (как TileBanner.getUpdateTag).
        net.minecraft.world.level.storage.TagValueOutput out =
                net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                        net.minecraft.util.ProblemReporter.DISCARDING, registries);
        saveAdditional(out);
        tag.merge(out.buildResult());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ---- MenuProvider ----

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.hex_note_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new HexResearchTableMenu(HexTableRegistry.MENU, id, playerInventory, this);
    }
}
