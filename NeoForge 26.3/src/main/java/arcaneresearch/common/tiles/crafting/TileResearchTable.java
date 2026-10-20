package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.theorycraft.ITheorycraftAid;
import arcaneresearch.api.research.theorycraft.ResearchTableData;
import arcaneresearch.api.research.theorycraft.TheorycraftManager;
import arcaneresearch.common.menu.ResearchTableMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileResearchTable extends TileArcaneresearchInventory implements MenuProvider {

    public static final int SLOT_SCRIBING_TOOLS = 0;
    public static final int SLOT_PAPER = 1;

    public ResearchTableData data;

    public TileResearchTable(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESEARCH_TABLE.get(), pos, state, 2);
        this.data = null;
        this.syncedSlots = new int[]{0, 1};
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.research_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ResearchTableMenu(containerId, playerInventory, this);
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        // NEOFORGE_PORT: ValueOutput has no put/remove/contains(String) — codec store;
        // absent key reads back empty (same semantics as Forge remove).
        if (data != null) {
            tag.store("note", net.minecraft.nbt.CompoundTag.CODEC, data.serialize());
        }
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        var note = tag.read("note", net.minecraft.nbt.CompoundTag.CODEC);
        if (note.isPresent()) {
            data = new ResearchTableData(this);
            data.deserialize(note.get());
        } else {
            data = null;
        }
    }

    public void startNewTheory(Player player, Set<String> aids) {
        data = new ResearchTableData(player, this);
        data.initialize(player, aids);
        syncTile(false);
        setChanged();
    }

    public void finishTheory(Player player) {
        if (data == null) return;

        Map<String, Integer> sortedMap = data.categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        int i = 0;
        for (String cat : sortedMap.keySet()) {

            int tot = Math.round(sortedMap.get(cat) / 100.0f * IPlayerKnowledge.EnumKnowledgeType.THEORY.getProgression());

            if (i > data.penaltyStart) {
                tot = (int) Math.max(1.0, tot * 0.666666667);
            }

            ResearchCategory rc = ResearchCategories.getResearchCategory(cat);
            if (rc != null) {

                ArcaneresearchApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.THEORY, rc, tot);
            }
            i++;
        }

        data = null;

        if (level != null) {
            level.playSound(null, worldPosition, ModSounds.LEARN.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        }

        syncTile(false);
        setChanged();
    }

    public boolean hasActiveTheory() {
        return data != null;
    }

    public ResearchTableData getResearchData() {
        return data;
    }

    public Set<String> checkSurroundingAids() {
        Map<String, ITheorycraftAid> foundAids = new HashMap<>();

        if (level == null) return foundAids.keySet();

        for (int y = -1; y <= 1; y++) {
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos checkPos = worldPosition.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);

                    for (Map.Entry<String, ITheorycraftAid> entry : TheorycraftManager.aids.entrySet()) {
                        String aidKey = entry.getKey();
                        ITheorycraftAid aid = entry.getValue();
                        Object trigger = aid.getAidObject();

                        if (trigger instanceof Block) {
                            if (state.getBlock() == trigger) {
                                foundAids.put(aidKey, aid);
                            }
                        } else if (trigger instanceof ItemStack triggerStack) {

                            ItemStack blockItem = pickBlockEquivalent(state, checkPos);
                            if (!blockItem.isEmpty()
                                    && ItemStack.isSameItemSameComponents(blockItem, triggerStack)) {
                                foundAids.put(aidKey, aid);
                            }
                        }
                    }
                }
            }
        }

        AABB searchBox = new AABB(worldPosition).inflate(5.0);
        List<Entity> entities = level.getEntities((Entity) null, searchBox, e -> true);

        for (Entity entity : entities) {
            for (Map.Entry<String, ITheorycraftAid> entry : TheorycraftManager.aids.entrySet()) {
                String aidKey = entry.getKey();
                ITheorycraftAid aid = entry.getValue();
                Object trigger = aid.getAidObject();

                if (trigger instanceof Class<?> entityClass) {
                    if (entityClass.isAssignableFrom(entity.getClass())) {
                        foundAids.put(aidKey, aid);
                    }
                }
            }
        }

        return foundAids.keySet();
    }

    private ItemStack pickBlockEquivalent(BlockState state, BlockPos pos) {
        try {
            // NEOFORGE_PORT: Block.getCloneItemStack(Level, BlockPos, BlockState, boolean)
            // removed in 26.x -> BlockState.getCloneItemStack(LevelReader, BlockPos, boolean)
            // (verified in 26.3 jar); data-carrying pick preserved via includeData=true.
            ItemStack picked = state.getCloneItemStack(level, pos, true);
            if (picked != null && !picked.isEmpty()) {
                return picked;
            }
        } catch (Exception ignored) {
        }
        return new ItemStack(state.getBlock());
    }

    public boolean consumeInkFromTable() {

        ItemStack tools = getItem(SLOT_SCRIBING_TOOLS);

        if (tools.getItem() instanceof IScribeTools) {
            if (tools.getDamageValue() < tools.getMaxDamage()) {
                tools.setDamageValue(tools.getDamageValue() + 1);
                syncTile(false);
                setChanged();
                return true;
            }
        }

        return false;
    }

    public boolean consumePaperFromTable() {
        ItemStack paper = getItem(SLOT_PAPER);
        if (!paper.isEmpty() && paper.is(Items.PAPER) && paper.getCount() > 0) {
            paper.shrink(1);
            if (paper.isEmpty()) {
                setItem(SLOT_PAPER, ItemStack.EMPTY);
            }
            syncTile(false);
            setChanged();
            return true;
        }
        return false;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_SCRIBING_TOOLS) {

            return stack.getItem() instanceof IScribeTools;
        }
        if (slot == SLOT_PAPER) {
            return stack.is(Items.PAPER);
        }
        return false;
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1) {

            if (level != null && level.isClientSide()) {
                level.playLocalSound(
                        worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        ModSounds.LEARN.get(), SoundSource.BLOCKS,
                        1.0f, 1.0f, false
                );
            }
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public boolean hasResearchData() {
        return data != null;
    }

    public boolean hasScribeTools() {
        ItemStack tools = getItem(SLOT_SCRIBING_TOOLS);
        return !tools.isEmpty() && tools.getItem() instanceof IScribeTools;
    }
}
