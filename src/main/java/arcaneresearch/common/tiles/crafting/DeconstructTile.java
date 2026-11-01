package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.DeconstructMenu;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.discovered.scan.MsgWalletSync;
import arcaneresearch.discovered.scan.ScanNetwork;
import arcaneresearch.discovered.scan.ScanWallet;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeconstructTile extends TileArcaneresearchInventory implements MenuProvider {
    public static final int BREAK_TICKS = 40;
    private static final int DROP_ROLL = 80;

    private int breaktime;

    @Nullable
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
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.deconstruction_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new DeconstructMenu(containerId, playerInventory, this);
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    @Nullable
    public Aspect currentAspect() {
        return aspect;
    }

    public int breaktime() {
        return breaktime;
    }

    public int breaktimeScaled(int scale) {
        if (BREAK_TICKS <= 0) return 0;
        return breaktime * scale / BREAK_TICKS;
    }

    public static AspectList tagsOf(ItemStack stack) {
        return ArcaneresearchCraftingManager.getObjectTags(stack);
    }

    public static boolean hasAspects(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        AspectList tags = tagsOf(stack);
        return tags.size() > 0;
    }

    public void serverTick() {
        if (level == null || level.isClientSide) return;

        boolean dirty = false;
        boolean breakable = canBreak();

        if (breaktime == 0 && breakable) {
            breaktime = BREAK_TICKS;
            dirty = true;
        } else if (breaktime > 0 && breakable) {
            breaktime--;

            if (breaktime == 0) {
                breakItem();
                dirty = true;
            }
        } else if (breaktime != 0) {
            breaktime = 0;
            dirty = true;
        }

        if (dirty) {
            setChanged();
        }
    }

    private boolean canBreak() {
        if (aspect != null) return false;

        ItemStack stack = getItem(0);
        if (stack.isEmpty()) return false;

        AspectList tags = ArcaneresearchCraftingManager.getObjectTags(stack);
        return tags.size() > 0;
    }

    private void breakItem() {
        ItemStack stack = getItem(0);

        if (stack.isEmpty()) return;

        AspectList tags = ArcaneresearchCraftingManager.getObjectTags(stack);

        AspectList primals = reduceToPrimals(tags);

        if (level != null && level.random.nextInt(DROP_ROLL) < primals.visSize()) {
            Aspect[] options = primals.getAspects();

            if (options != null && options.length > 0) {
                aspect = options[level.random.nextInt(options.length)];
            }
        }

        stack.shrink(1);

        if (stack.isEmpty()) {
            setItem(0, ItemStack.EMPTY);
        }

        setChanged();

        if (level != null) {
            syncTile(false);
        }
    }

    @Nullable
    public Aspect claimAspect(Player player) {
        if (aspect == null || player == null) return null;

        Aspect taken = aspect;
        aspect = null;

        if (player instanceof ServerPlayer serverPlayer) {
            ScanWallet.add(serverPlayer, taken, 1);

            Map<String, Integer> wallet = ScanWallet.snapshot(serverPlayer);

            ScanNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new MsgWalletSync(wallet, new LinkedHashMap<>())
            );
        }

        setChanged();

        if (level != null) {
            syncTile(false);
        }

        return taken;
    }

    public static AspectList reduceToPrimals(AspectList in) {
        AspectList out = new AspectList();

        if (in == null) return out;

        for (Aspect aspect : in.getAspects()) {
            if (aspect == null) continue;
            reduceToPrimals(aspect, in.getAmount(aspect), out);
        }

        return out;
    }

    private static void reduceToPrimals(Aspect aspect, int amount, AspectList out) {
        if (aspect.isPrimal()) {
            out.add(aspect, amount);
            return;
        }

        Aspect[] components = aspect.getComponents();
        if (components == null) return;

        for (Aspect component : components) {
            if (component == null) continue;
            reduceToPrimals(component, amount, out);
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putInt("Breaktime", breaktime);

        if (aspect != null) {
            tag.putString("Aspect", aspect.getTag());
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        breaktime = tag.getInt("Breaktime");

        aspect = tag.contains("Aspect")
                ? Aspect.getAspect(tag.getString("Aspect"))
                : null;
    }
}