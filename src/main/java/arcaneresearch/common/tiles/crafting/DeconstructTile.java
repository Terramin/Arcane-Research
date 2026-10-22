package arcaneresearch.common.tiles.crafting;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.discovered.scan.MsgWalletSync;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.LinkedHashMap;
import java.util.Map;

 


















 
public class DeconstructTile extends TileArcaneresearchInventory implements MenuProvider {

      
    public static final int BREAK_TICKS = 40;
      
    private static final int DROP_ROLL = 80;

    private int breaktime;
    private Aspect aspect;

    private final ContainerData dataAccess = new SimpleContainerData(1) {
        @Override
        public int get(int index) {
            return index == 0 ? breaktime : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                breaktime = value;
            }
        }
    };

    public DeconstructTile(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state, 1);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.deconstruction_table");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new arcaneresearch.common.menu.DeconstructMenu(containerId, playerInventory, this);
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    public Aspect currentAspect() {
        return aspect;
    }

    public int breaktime() {
        return breaktime;
    }

    public int breaktimeScaled(int scale) {
        return breaktime * scale / BREAK_TICKS;
    }

      
    public static AspectList tagsOf(ItemStack stack) {
        return ArcaneresearchCraftingManager.getObjectTags(stack);
    }

      
    public static boolean hasAspects(ItemStack stack) {
        return !stack.isEmpty() && tagsOf(stack).size() > 0;
    }

      
    public void serverTick() {
        if (level == null || level.isClientSide()) {
            return;
        }
        boolean dirty = false;
        if (breaktime == 0 && canBreak()) {
            breaktime = BREAK_TICKS;
            dirty = true;
        }
        if (breaktime > 0 && canBreak()) {
            breaktime--;
            if (breaktime == 0) {
                breaktime = 0;
                breakItem();
                dirty = true;
            }
        } else {
            breaktime = 0;
        }
        if (dirty) {
            setChanged();
        }
    }

    private boolean canBreak() {
        if (getItem(0).isEmpty() || aspect != null) {
            return false;
        }
        AspectList tags = ArcaneresearchCraftingManager.getObjectTags(getItem(0));
        return tags != null && tags.size() > 0;
    }

      
    private void breakItem() {
        if (!canBreak()) {
            return;
        }
        AspectList tags = ArcaneresearchCraftingManager.getObjectTags(getItem(0));
        AspectList primals = reduceToPrimals(tags);
        if (level != null && level.getRandom().nextInt(DROP_ROLL) < primals.visSize()) {
            Aspect[] options = primals.getAspects();
            if (options != null && options.length > 0) {
                aspect = options[level.getRandom().nextInt(options.length)];
            }
        }
        ItemStack stack = getItem(0);
        stack.shrink(1);
        if (stack.isEmpty()) {
            setItem(0, ItemStack.EMPTY);
        }
        setChanged();
        syncTile(false);
    }

     




 
    public Aspect claimAspect(Player player) {
        if (aspect == null || player == null) {
            return null;
        }
        Aspect taken = aspect;
        aspect = null;
        if (player instanceof ServerPlayer serverPlayer) {
            ScanWallet.add(serverPlayer, taken, 1);
            Map<String, Integer> wallet = ScanWallet.snapshot(serverPlayer);
            PacketHandler.sendToPlayer(new MsgWalletSync(wallet, new LinkedHashMap<>()), serverPlayer);
        }
        setChanged();
        syncTile(false);
        return taken;
    }

      
    public static AspectList reduceToPrimals(AspectList in) {
        AspectList out = new AspectList();
        if (in == null) {
            return out;
        }
        for (Aspect aspect : in.getAspects()) {
            if (aspect == null) {
                continue;
            }
            int amount = in.getAmount(aspect);
            if (aspect.isPrimal()) {
                out.add(aspect, amount);
            } else {
                Aspect[] components = aspect.getComponents();
                if (components != null) {
                    for (Aspect component : components) {
                        if (component != null) {
                            AspectList single = new AspectList().add(component, amount);
                            out.add(reduceToPrimals(single));
                        }
                    }
                }
            }
        }
        return out;
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putInt("Breaktime", breaktime);
        if (aspect != null) {
            tag.putString("Aspect", aspect.getTag());
        }
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);
        breaktime = tag.getIntOr("Breaktime", 0);
        String aspectTag = tag.getStringOr("Aspect", "");
        aspect = aspectTag.isEmpty() ? null : Aspect.getAspect(aspectTag);
    }
}
