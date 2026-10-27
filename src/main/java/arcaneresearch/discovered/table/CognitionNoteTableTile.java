package arcaneresearch.discovered.table;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.note.DiscoveredNotes;
import org.jetbrains.annotations.NotNull;

public class CognitionNoteTableTile extends BlockEntity implements Container, MenuProvider {
    public static final int INPUT_COUNT = 5;
    public static final int NOTE_SLOT = 5;
    public static final int PAPER_SLOT = 6;
    public static final int SIZE = 7;
    public static final int SAFE_CHANCE = 60;
    public static final int SAFE_LOSS = 1;
    public static final int THOROUGH_CHANCE = 100;
    public static final int THOROUGH_LOSS = 3;
    public static final float MAX_TAG_BONUS = 5.0F;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean safe = true;
    private boolean recalc = true;
    private int baseChance = SAFE_CHANCE;
    private int baseLoss = SAFE_LOSS;
    private final Aspect[] tags = new Aspect[INPUT_COUNT];
    private final int[] tagAmounts = new int[INPUT_COUNT];
    private final float[] tagBonus = new float[INPUT_COUNT];
    private long lastRecalcAttempt;
    private boolean recalcFailed;

    public CognitionNoteTableTile(BlockPos pos, BlockState state) {
        super(DiscoveredTableSetup.getEntityType(), pos, state);
    }

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
    public @NotNull ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int count) {
        ItemStack out = ContainerHelper.removeItem(items, slot, count);
        if (!out.isEmpty()) {
            setChanged();
        }
        return out;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        recalc = true;
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public void dropContents() {
        if (level != null) {
            net.minecraft.world.Containers.dropContents(level, worldPosition, this);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putBoolean("Safe", safe);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, items);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            items.clear();
            ContainerHelper.loadAllItems(tag, items);
        }
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net,
                             net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet) {
        if (packet == null) {
            return;
        }
        handleUpdateTag(packet.getTag());
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        safe = !tag.contains("Safe") || tag.getBoolean("Safe");
        recalc = true;
    }

    @Override
    public @NotNull Component getDisplayName() {
        Block block = DiscoveredTableSetup.getBlock();
        return block == null ? Component.empty() : block.getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new CognitionNoteTableMenu(id, playerInventory, this);
    }

    public boolean isSafe() {
        return safe;
    }

    public void toggleSafe() {
        safe = !safe;
        recalcBaseChance();
        setChanged();
    }

    public int getBaseChance() {
        return baseChance;
    }

    public int getBaseLoss() {
        return baseLoss;
    }

    public Aspect getTag(int i) {
        if (i < 0 || i >= tags.length) {
            return null;
        }
        recalcIfNeeded();
        return tags[i];
    }

    public int getTagAmount(int i) {
        if (i < 0 || i >= tagAmounts.length) {
            return 0;
        }
        recalcIfNeeded();
        return tagAmounts[i];
    }

    public float getTagBonus(int i) {
        if (i < 0 || i >= tagBonus.length) {
            return 0.0F;
        }
        recalcIfNeeded();
        return tagBonus[i];
    }

    public void recalcIfNeeded() {
        if (!recalc) {
            return;
        }
        long now = System.currentTimeMillis();
        if (recalcFailed && now - lastRecalcAttempt < 500L) {
            return;
        }
        lastRecalcAttempt = now;
        try {
            recalculateTags();
            recalculateBonus();
            recalcBaseChance();
        } catch (Exception e) {
            recalcFailed = true;
            return;
        }
        recalc = false;
        recalcFailed = false;
    }

    private void recalcBaseChance() {
        if (safe) {
            baseChance = SAFE_CHANCE;
            baseLoss = SAFE_LOSS;
        } else {
            baseChance = THOROUGH_CHANCE;
            baseLoss = THOROUGH_LOSS;
        }
    }

    private void recalculateTags() {
        AspectList merged = new AspectList();
        for (int i = 0; i < INPUT_COUNT; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                AspectList list = AspectHelper.getObjectAspects(stack);
                if (list != null) {
                    merged.merge(list);
                }
            }
        }
        Aspect[] sorted = merged.getAspectsSortedByAmount();
        java.util.ArrayList<Aspect> ordered = new java.util.ArrayList<>();
        java.util.HashSet<Aspect> required = noteRequiredTags();
        for (Aspect aspect : sorted) {
            if (aspect != null && required.contains(aspect)) {
                ordered.add(aspect);
            }
        }
        for (Aspect aspect : sorted) {
            if (aspect != null && !ordered.contains(aspect)) {
                ordered.add(aspect);
            }
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            if (i < ordered.size()) {
                tags[i] = ordered.get(i);
                tagAmounts[i] = merged.getAmount(ordered.get(i));
            } else {
                tags[i] = null;
                tagAmounts[i] = 0;
            }
            tagBonus[i] = 0.0F;
        }
    }

    private java.util.HashSet<Aspect> noteRequiredTags() {
        java.util.HashSet<Aspect> out = new java.util.HashSet<>();
        try {
            ItemStack note = items.get(NOTE_SLOT);
            if (!note.isEmpty() && note.getItem() instanceof ItemDiscoveredResearchNote) {
                DiscoveredNoteData data = DiscoveredNoteData.read(note);
                if (data.tags != null) {
                    for (Aspect aspect : data.tags) {
                        if (aspect != null) {
                            out.add(aspect);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private void recalculateBonus() {
        if (level == null) {
            return;
        }
        BlockPos pos = worldPosition;
        addBonus("tenebrae", level.getMaxLocalRawBrightness(pos) < 9 ? 2 : 0);
        addBonus("lux", level.isDay() && level.canSeeSky(pos.above()) ? 2 : 0);
        if (pos.getY() > 100) {
            addBonus("aer", 3);
        }
        if (level.isRainingAt(pos.above())) {
            addBonus("aqua", 2);
            addBonus("potentia", 2);
            addBonus("motus", 2);
            if (level.isThundering()) {
                addBonus("potentia", 2);
            }
        }
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(15));
        int beasts = 0;
        int men = 0;
        for (LivingEntity e : entities) {
            if (e instanceof Player) {
                men++;
            } else {
                beasts++;
            }
        }
        addBonus("bestia", Math.min(5, beasts / 5));
        addBonus("humanus", Math.min(3, men));
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    if ((x + y + z) % 3 != 0) {
                        continue;
                    }
                    ItemStack asStack = new ItemStack(level.getBlockState(pos.offset(x, y, z)).getBlock());
                    AspectList list = AspectHelper.getObjectAspects(asStack);
                    if (list != null) {
                        for (Aspect aspect : list.getAspects()) {
                            addBonus(aspect.getTag(), 1);
                        }
                    }
                }
            }
        }
    }

    private void addBonus(String tag, int amount) {
        if (amount <= 0 || tag == null) {
            return;
        }
        for (int i = 0; i < tags.length; i++) {
            Aspect t = tags[i];
            if (t != null && tag.equals(t.getTag())) {
                tagBonus[i] = Math.min(MAX_TAG_BONUS, tagBonus[i] + amount);
            }
        }
    }

    public boolean canResearch() {
        recalcIfNeeded();
        if (tags[0] == null) {
            return false;
        }
        ItemStack note = items.get(NOTE_SLOT);
        if (note.isEmpty()) {
            return items.get(PAPER_SLOT).is(Items.PAPER);
        }
        return note.getItem() instanceof ItemDiscoveredResearchNote;
    }

    public void startResearch(Player player) {
        if (level == null || level.isClientSide) {
            return;
        }
        level.playSound(null, worldPosition, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.4F, 1.0F);
        boolean progressed = doResearch(player);
        if (progressed) {
            for (int i = 0; i < INPUT_COUNT; i++) {
                ItemStack stack = items.get(i);
                if (!stack.isEmpty() && level.getRandom().nextInt(100) < baseLoss) {
                    stack.shrink(1);
                }
            }
            setChanged();
        }
    }

    public boolean doResearch(Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }
        recalcIfNeeded();
        int[] amounts = new int[INPUT_COUNT];
        for (int i = 0; i < INPUT_COUNT; i++) {
            amounts[i] = tagAmounts[i] + Math.round(tagBonus[i]);
        }
        ItemStack note = items.get(NOTE_SLOT);
        boolean progressed = false;
        if (note.isEmpty()) {
            String key = DiscoveredTableResearch.findMatchingResearch(player, tags.clone(), amounts);
            AspectList required = key == null ? null : DiscoveredTableResearch.requiredAspects(key);
            if (key != null && required != null && createResearchNoteForTable(key, required)) {
                note = items.get(NOTE_SLOT);
                progressed = true;
                DiscoveredTableResearch.progressTableResearch(level, player, note,
                        baseChance, baseLoss, tags.clone(), amounts);
            }
        } else if (note.getItem() instanceof ItemDiscoveredResearchNote) {
            DiscoveredNoteData data = DiscoveredNoteData.read(note);
            if (!data.isComplete()) {
                progressed = DiscoveredTableResearch.progressTableResearch(level, player, note,
                        baseChance, baseLoss, tags.clone(), amounts);
            }
        }
        if (bulkFillFromInputs(note)) {
            progressed = true;
        }
        note = items.get(NOTE_SLOT);
        if (note.getItem() instanceof ItemDiscoveredResearchNote) {
            DiscoveredNoteData data = DiscoveredNoteData.read(note);
            if (data.isComplete()) {
                items.set(NOTE_SLOT, DiscoveredNotes.toDiscovery(note));
                progressed = true;
            }
        }
        setChanged();
        return progressed;
    }

    private boolean createResearchNoteForTable(String key, AspectList required) {
        if (!items.get(NOTE_SLOT).isEmpty() || !items.get(PAPER_SLOT).is(Items.PAPER)) {
            return false;
        }
        items.get(PAPER_SLOT).shrink(1);
        items.set(NOTE_SLOT, DiscoveredNotes.createNote(key, required));
        return true;
    }

    private boolean bulkFillFromInputs(ItemStack note) {
        if (note == null || note.isEmpty()
                || !(note.getItem() instanceof ItemDiscoveredResearchNote)) {
            return false;
        }
        boolean filled = false;
        try {
            DiscoveredNoteData data = DiscoveredNoteData.read(note);
            if (data.key == null || data.tags == null) {
                return false;
            }
            for (int t = 0; t < data.tags.length; t++) {
                Aspect tag = data.tags[t];
                if (tag == null) {
                    continue;
                }
                int want = data.need != null && t < data.need.length ? data.need[t] : 0;
                int got = data.have != null && t < data.have.length
                        ? Math.min(data.have[t], want) : 0;
                while (got < want) {
                    boolean consumed = false;
                    for (int i = 0; i < INPUT_COUNT; i++) {
                        ItemStack stack = items.get(i);
                        if (stack.isEmpty()) {
                            continue;
                        }
                        AspectList list = AspectHelper.getObjectAspects(stack);
                        int per = list == null ? 0 : list.getAmount(tag);
                        if (per <= 0) {
                            continue;
                        }
                        int add = Math.min(per, want - got);
                        data.have[t] = got + add;
                        got += add;
                        stack.shrink(1);
                        filled = true;
                        consumed = true;
                        break;
                    }
                    if (!consumed) {
                        break;
                    }
                }
            }
            if (filled) {
                if (data.getTotalProgress() >= 1.0F) {
                    data.stage1 = true;
                }
                DiscoveredNoteData.write(note, data);
                setChanged();
            }
        } catch (Exception ignored) {
        }
        return filled;
    }
}