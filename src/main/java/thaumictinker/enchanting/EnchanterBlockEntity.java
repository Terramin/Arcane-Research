package thaumictinker.enchanting;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.misc.BlockNitor;
import arcaneresearch.init.ModSounds;

 





 
public class EnchanterBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int TOOL = 0;
    public static final int WAND = 1;
    public static final int PILLAR_COUNT = 6;
    public static final int PILLAR_RANGE = 4;
    public static final int MIN_PILLAR_TOTEMS = 2;
    public static final int MAX_PILLAR_TOTEMS = 12;
    public static final int VIS_PER_UNIT = 1;
    public static final int EVENT_RUNES = 0;

    private static final class Pillar {
        private final BlockPos base;
        private int top;

        private Pillar(BlockPos base, int top) {
            this.base = base;
            this.top = top;
        }
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private final List<Enchantment> enchantments = new ArrayList<>();
    private final List<Integer> levels = new ArrayList<>();
    private final List<Pillar> pillars = new ArrayList<>();
    private AspectList totalAspects = new AspectList();
    private AspectList currentAspects = new AspectList();
    private boolean working;

    public EnchanterBlockEntity(BlockPos pos, BlockState state) {
        super(EnchantingFeature.ENCHANTER_ENTITY, pos, state);
    }

    public List<Enchantment> getEnchantments() {
        return enchantments;
    }

    public List<Integer> getLevels() {
        return levels;
    }

    public AspectList getTotalAspects() {
        return totalAspects;
    }

    public AspectList getCurrentAspects() {
        return currentAspects;
    }

    public boolean isWorking() {
        return working;
    }

    public ItemStack getTool() {
        return items.get(TOOL);
    }

    public ItemStack getWand() {
        return items.get(WAND);
    }

    public static boolean isUsableWand(ItemStack stack) {
        return stack.getItem() instanceof IWand wand && !wand.isStaff(stack);
    }

    public static boolean isAcceptedWand(ItemStack stack) {
        
        
        
        return isUsableWand(stack);
    }

    private void pushUpdate() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void setChanged() {
        if (level != null && !level.isClientSide() && !working) {
            clearSelection();
        }
        pushUpdate();
    }

    private void clearSelection() {
        enchantments.clear();
        levels.clear();
        totalAspects = new AspectList();
    }

    public void updateAspectList() {
        totalAspects = new AspectList();
        for (int index = 0; index < enchantments.size() && index < levels.size(); index++) {
            EnchantmentManager.Data data = EnchantmentManager.data(enchantments.get(index));
            if (data != null) {
                totalAspects.add(data.aspects(levels.get(index)));
            }
        }
    }

    public void removeEnchantment(Enchantment enchantment) {
        int index = enchantments.indexOf(enchantment);
        if (index != -1) {
            levels.remove(index);
            enchantments.remove(index);
        }
    }

    public boolean addEnchantment(Player player, Enchantment enchantment) {
        ItemStack tool = getTool();
        if (working || enchantments.contains(enchantment) || !EnchantmentManager.canEnchantStack(tool)) {
            return false;
        }
        if (!EnchantmentManager.canApply(tool, enchantment, enchantments)
                || !EnchantmentManager.canEnchantmentBeUsed(player, enchantment)) {
            return false;
        }
        enchantments.add(enchantment);
        levels.add(1);
        return true;
    }

    public void setLevel(Enchantment enchantment, int enchantmentLevel) {
        int index = enchantments.indexOf(enchantment);
        if (index == -1 || enchantmentLevel < 1 || enchantmentLevel > maxLevel(enchantment)) {
            return;
        }
        levels.set(index, enchantmentLevel);
    }

    public int maxLevel(Enchantment enchantment) {
        return enchantment.getMaxLevel();
    }

    public void applyEdit(Player player, Enchantment enchantment, int requestedLevel) {
        if (working || level == null || level.isClientSide()) {
            return;
        }
        if (requestedLevel == -1) {
            removeEnchantment(enchantment);
        } else if (!enchantments.contains(enchantment)) {
            addEnchantment(player, enchantment);
        } else {
            setLevel(enchantment, Math.max(1, Math.min(maxLevel(enchantment), requestedLevel)));
        }
        updateAspectList();
        pushUpdate();
    }

    public boolean startWorking() {
        if (level == null || level.isClientSide() || working || enchantments.isEmpty() || levels.isEmpty()
                || enchantments.size() != levels.size()) {
            return false;
        }
        if (!EnchantmentManager.canEnchantStack(getTool())) {
            return false;
        }
        working = true;
        pushUpdate();
        return true;
    }

    public static void serverTick(Level tickLevel, BlockPos pos, BlockState state, EnchanterBlockEntity enchanter) {
        enchanter.tick(tickLevel, pos);
    }

    private void tick(Level tickLevel, BlockPos pos) {
        ItemStack tool = getTool();
        if (tool.isEmpty() && !(enchantments.isEmpty() && levels.isEmpty())) {
            clearSelection();
            pushUpdate();
        }
        if (!working) {
            return;
        }
        if (tool.isEmpty()) {
            working = false;
            pushUpdate();
            return;
        }
        if (!checkPillars()) {
            return;
        }
        boolean complete = true;
        for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
            if (currentAspects.getAmount(aspect) < totalAspects.getAmount(aspect)) {
                complete = false;
                break;
            }
        }
        if (complete) {
            finish(tickLevel, pos, tool);
            return;
        }
        ItemStack wand = getWand();
        if (isUsableWand(wand)) {
            drainWand(tickLevel, wand, (IWand) wand.getItem());
        }
    }

    private void finish(Level finishLevel, BlockPos pos, ItemStack tool) {
        working = false;
        currentAspects = new AspectList();
        totalAspects = new AspectList();
        for (int index = 0; index < enchantments.size() && index < levels.size(); index++) {
            tool.enchant(enchantments.get(index), levels.get(index));
        }
        enchantments.clear();
        levels.clear();
        finishLevel.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        pushUpdate();
    }

    private void drainWand(Level drainLevel, ItemStack wand, IWand wandItem) {
        AspectList wandAspects = wandItem.getAllVis(wand);
        List<Aspect> candidates = new ArrayList<>();
        for (Aspect aspect : EnchantmentManager.PRIMAL_ASPECTS) {
            int missing = totalAspects.getAmount(aspect) - currentAspects.getAmount(aspect);
            if (missing > 0 && wandAspects.getAmount(aspect) >= VIS_PER_UNIT) {
                candidates.add(aspect);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        RandomSource random = drainLevel.getRandom();
        int index = random.nextInt(candidates.size());
        Aspect aspect = candidates.get(index);
        if (!wandItem.consumeVis(wand, null, aspect, VIS_PER_UNIT, false)) {
            return;
        }
        currentAspects.add(aspect, VIS_PER_UNIT);
        super.setChanged();
        if (index < pillars.size() && random.nextBoolean()) {
            drainLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_RUNES, index);
            if (random.nextInt(5) == 0) {
                BlockPos base = pillars.get(index).base;
                drainLevel.playSound(null, base.getX() + 0.5, pillars.get(index).top + 0.5, base.getZ() + 0.5,
                        ModSounds.BRAIN.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id != EVENT_RUNES || level == null) {
            return super.triggerEvent(id, type);
        }
        if (!level.isClientSide()) {
            return true;
        }
        if (pillars.size() != PILLAR_COUNT) {
            pillars.clear();
            assignPillars();
        }
        RandomSource random = level.getRandom();
        if (type >= 0 && type < pillars.size()) {
            Pillar pillar = pillars.get(type);
            FXDispatcher.INSTANCE.blockRunes(pillar.base.getX(), pillar.top - 0.75, pillar.base.getZ(),
                    0.3F + random.nextFloat() * 0.7F, 0.0F, 0.3F + random.nextFloat() * 0.7F,
                    15, random.nextFloat() / 8.0F);
        }
        FXDispatcher.INSTANCE.blockRunes(worldPosition.getX(), worldPosition.getY() + 0.25, worldPosition.getZ(),
                0.3F + random.nextFloat() * 0.7F, 0.0F, 0.3F + random.nextFloat() * 0.7F,
                15, random.nextFloat() / 8.0F);
        return true;
    }

    public boolean checkPillars() {
        if (pillars.isEmpty()) {
            if (assignPillars()) {
                working = false;
                currentAspects = new AspectList();
                pushUpdate();
                return false;
            }
            return true;
        }
        for (Pillar pillar : pillars) {
            int height = findPillar(pillar.base);
            if (height == -1) {
                pillars.clear();
                return checkPillars();
            }
            pillar.top = height;
        }
        return true;
    }

    public boolean assignPillars() {
        int y = worldPosition.getY();
        for (int x = worldPosition.getX() - PILLAR_RANGE; x <= worldPosition.getX() + PILLAR_RANGE; x++) {
            for (int z = worldPosition.getZ() - PILLAR_RANGE; z <= worldPosition.getZ() + PILLAR_RANGE; z++) {
                BlockPos base = new BlockPos(x, y, z);
                int height = findPillar(base);
                if (height != -1) {
                    pillars.add(new Pillar(base, height));
                }
                if (pillars.size() == PILLAR_COUNT) {
                    return false;
                }
            }
        }
        pillars.clear();
        return true;
    }

    public int findPillar(BlockPos base) {
        int totems = 0;
        for (int offset = 0; ; offset++) {
            BlockPos pos = base.above(offset);
            if (level == null || pos.getY() > level.getMaxBuildHeight()) {
                return -1;
            }
            BlockState state = level.getBlockState(pos);
            if (state.is(EnchantingFeature.OBSIDIAN_TOTEM)) {
                totems++;
                continue;
            }
            if (state.getBlock() instanceof BlockNitor && totems >= MIN_PILLAR_TOTEMS && totems < MAX_PILLAR_TOTEMS + 1) {
                return pos.getY();
            }
            return -1;
        }
    }

    public int pillarCount() {
        return pillars.size();
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EnchanterMenu(containerId, inventory, worldPosition);
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return items.get(TOOL).isEmpty() && items.get(WAND).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                        worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items, true);
        tag.putBoolean("working", working);
        totalAspects.writeToNBT(tag, "totalAspects");
        currentAspects.writeToNBT(tag, "currentAspects");
        ListTag enchantTag = new ListTag();
        for (Enchantment enchantment : enchantments) {
            ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
            enchantTag.add(StringTag.valueOf(id == null ? "thaumictinker:unknown" : id.toString()));
        }
        tag.put("enchantments", enchantTag);
        ListTag levelTag = new ListTag();
        for (int enchantmentLevel : levels) {
            levelTag.add(net.minecraft.nbt.IntTag.valueOf(enchantmentLevel));
        }
        tag.put("levels", levelTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        working = tag.getBoolean("working");
        totalAspects = new AspectList();
        totalAspects.readFromNBT(tag, "totalAspects");
        currentAspects = new AspectList();
        currentAspects.readFromNBT(tag, "currentAspects");
        enchantments.clear();
        ListTag enchantTag = tag.getList("enchantments", Tag.TAG_STRING);
        for (int index = 0; index < enchantTag.size(); index++) {
            Enchantment enchantment = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS
                    .getValue(ResourceLocation.tryParse(enchantTag.getString(index)));
            if (enchantment != null) {
                enchantments.add(enchantment);
            }
        }
        levels.clear();
        ListTag levelTag = tag.getList("levels", Tag.TAG_INT);
        for (int index = 0; index < levelTag.size(); index++) {
            levels.add(levelTag.getInt(index));
        }
    }
}
