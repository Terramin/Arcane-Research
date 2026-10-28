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

 









 
public class HexNoteTableTile extends BlockEntity implements Container, MenuProvider {

    public static final int INK_SLOT = 0;
    public static final int NOTE_SLOT = 1;
    public static final int SIZE = 2;

      
    private static final int BONUS_RECALC_TICKS = 600;
      
    private static final int BONUS_CAP = 5;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);

     



 
    private final AspectList bonusAspects = new AspectList();
    private int ticks;

    public HexNoteTableTile(BlockPos pos, BlockState state) {
        super(HexTableRegistry.TILE, pos, state);
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
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SIZE ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SIZE) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack out = ContainerHelper.removeItem(items, slot, amount);
        if (!out.isEmpty()) {
            setChanged();
        }
        return out;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return slot >= 0 && slot < SIZE ? ContainerHelper.takeItem(items, slot) : ItemStack.EMPTY;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        
        
        
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

    

    public AspectList getBonus() {
        return bonusAspects;
    }

      
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

     



 
    public void recalculateBonus() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        bonusAspects.aspects.clear();
        BlockPos pos = worldPosition;
        BlockPos above = pos.above();

        addCapped(Aspect.getAspect("tenebrae"),
                level.getMaxLocalRawBrightness(pos) < 9 ? 2 : 0);
        addCapped(Aspect.getAspect("lux"),
                level.isBrightOutside() && level.canSeeSky(above) ? 2 : 0);
        if (pos.getY() > 100) {
            addCapped(Aspect.getAspect("aer"), 3);
        }
        if (level.isRainingAt(above)) {
            addCapped(Aspect.getAspect("aqua"), 2);
            Aspect potentia = Aspect.getAspect("potentia");
            addCapped(potentia, 2);
            addCapped(Aspect.getAspect("motus"), 2);
            if (level.isThundering()) {
                addCapped(potentia, 2);
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
                int zStart = -10 + Math.floorMod(10 - x - y, 3);
                for (int z = zStart; z <= 10; z += 3) {
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

    

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        bonusAspects.writeToNBT(tag, "bonus");
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        
        
        
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        bonusAspects.aspects.clear();
        try {
            bonusAspects.readFromNBT(tag, "bonus");
        } catch (Exception ignored) {
        }
    }

    

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        
        
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

    

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.hex_note_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        if (HexTableRegistry.MENU == null) {
            return null;
        }
        return new HexResearchTableMenu(HexTableRegistry.MENU, id, playerInventory, this);
    }
}
