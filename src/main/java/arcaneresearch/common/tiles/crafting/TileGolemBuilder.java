package arcaneresearch.common.tiles.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaTransport;
import arcaneresearch.api.golems.IGolemProperties;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.golems.GolemProperties;
import arcaneresearch.common.golems.ItemGolemPlacer;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.common.menu.GolemBuilderMenu;
import arcaneresearch.common.tiles.TileArcaneresearchInventory;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileGolemBuilder extends TileArcaneresearchInventory implements MenuProvider, IEssentiaTransport {
    private static final Direction[] DIRECTIONS = Direction.values();

    public int cost = 0;
    public int maxCost = 0;
    public boolean[] hasStuff = null;
    public int pressAnimation = 0;

    boolean bufferedEssentia = false;
    IGolemProperties props = null;
    ItemStack[] components = null;

    private long golemProps = -1L;
    private int tickCounter = 0;
    private AABB renderBox;

    public TileGolemBuilder(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLEM_BUILDER.get(), pos, state, 1);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileGolemBuilder tile) {
        if (level == null) return;

        tile.tickCounter++;
        boolean complete = false;

        if (tile.tickCounter % 5 == 0 && tile.cost > 0 && tile.golemProps >= 0L) {
            if (tile.bufferedEssentia || tile.drawEssentia()) {
                tile.bufferedEssentia = false;
                tile.cost--;
                tile.setChanged();
            }

            if (tile.cost <= 0) {
                IGolemProperties craftProps = tile.props != null
                        ? tile.props
                        : GolemProperties.fromLong(tile.golemProps);

                ItemStack placer = tile.createGolemStack(craftProps);
                ItemStack output = tile.getItem(0);

                if (output.isEmpty()) {
                    tile.setItem(0, placer.copy());
                    complete = true;
                } else if (output.getCount() < output.getMaxStackSize()
                        && ItemStack.isSameItemSameTags(output, placer)) {
                    output.grow(1);
                    complete = true;
                }

                if (complete) {
                    level.playSound(null, pos, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }

        if (complete) {
            tile.cost = 0;
            tile.golemProps = -1L;
            tile.markDirtyAndSync();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(Level level, BlockPos pos, BlockState state, TileGolemBuilder tile) {
        if (level == null) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        double topY = pos.getY() + 1;

        if (tile.pressAnimation < 90 && tile.cost > 0 && tile.golemProps > 0L) {
            tile.pressAnimation += 6;

            if (tile.pressAnimation >= 60) {
                level.playLocalSound(cx, cy, cz,
                        SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.66f,
                        1.0f + level.random.nextFloat() * 0.1f, false);

                for (int a = 0; a < 16; ++a) {
                    FXDispatcher.INSTANCE.drawVentParticles(cx, topY, cz,
                            level.random.nextGaussian() * 0.1, 0.0, level.random.nextGaussian() * 0.1, 11184810);
                }
            }
        }

        if (tile.pressAnimation >= 90 && level.random.nextInt(8) == 0) {
            FXDispatcher.INSTANCE.drawVentParticles(cx, topY, cz,
                    level.random.nextGaussian() * 0.1, 0.0, level.random.nextGaussian() * 0.1, 11184810);

            level.playLocalSound(cx, cy, cz,
                    SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1f,
                    1.0f + level.random.nextFloat() * 0.1f, false);
        }

        if (tile.pressAnimation > 0 && (tile.cost <= 0 || tile.golemProps < 0)) {
            if (tile.pressAnimation >= 90) {
                for (int a = 0; a < 10; ++a) {
                    FXDispatcher.INSTANCE.drawVentParticles(cx, topY, cz,
                            level.random.nextGaussian() * 0.1, 0.0, level.random.nextGaussian() * 0.1, 11184810);
                }
            }

            tile.pressAnimation -= 3;
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.arcaneresearch.golem_builder");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new GolemBuilderMenu(containerId, playerInventory, this);
    }

    public boolean startCraft(long golemId, Player player) {
        if (level == null || level.isClientSide || player == null) return false;

        ItemStack placer = new ItemStack(ModItems.GOLEM_PLACER.get());
        placer.getOrCreateTag().putLong("props", golemId);

        ItemStack output = getItem(0);

        if (!output.isEmpty()
                && (output.getCount() >= output.getMaxStackSize()
                || !ItemStack.isSameItemSameTags(output, placer))) {
            resetCraftState();
            return false;
        }

        if (player.isCreative()) {
            if (output.isEmpty()) {
                setItem(0, placer.copy());
            } else {
                output.grow(1);
            }

            setChanged();
            level.playSound(null, worldPosition, ModSounds.WAND.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            return true;
        }

        golemProps = golemId;
        props = GolemProperties.fromLong(golemProps);
        components = props.generateComponents();

        if (!InventoryUtils.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, player,
                true, components)) {
            resetCraftState();
            return false;
        }

        cost = props.getTraits().size() * 2;

        for (ItemStack stack : components) {
            cost += stack.getCount();
        }

        InventoryUtils.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, player,
                false, components);

        maxCost = cost;
        setChanged();
        syncTile(false);

        level.playSound(null, worldPosition, ModSounds.WAND.get(), SoundSource.BLOCKS, 0.25f, 1.0f);
        return true;
    }

    private void resetCraftState() {
        cost = 0;
        props = null;
        components = null;
        golemProps = -1L;
    }

    private ItemStack createGolemStack(IGolemProperties craftProps) {
        ItemStack stack = new ItemStack(ModItems.GOLEM_PLACER.get());
        stack.getOrCreateTag().putLong("props", craftProps.toLong());
        return stack;
    }

    public long getGolemProps() {
        return golemProps;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof ItemGolemPlacer;
    }

    @Override
    public void messageFromClient(CompoundTag nbt, net.minecraft.server.level.ServerPlayer player) {
        super.messageFromClient(nbt, player);

        if (nbt.contains("check")) {
            this.hasStuff = checkCraft(nbt.getLong("golem"));

            byte[] ba = new byte[this.hasStuff.length];

            for (int a = 0; a < ba.length; a++) {
                ba[a] = (byte) (this.hasStuff[a] ? 1 : 0);
            }

            CompoundTag reply = new CompoundTag();
            reply.putByteArray("stuff", ba);

            arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                    new arcaneresearch.common.lib.network.tiles.PacketTileToClient(worldPosition, reply), player);
        } else if (nbt.contains("golem")) {
            startCraft(nbt.getLong("golem"), player);
        }
    }

    @Override
    public void messageFromServer(CompoundTag nbt) {
        super.messageFromServer(nbt);

        if (nbt.contains("stuff")) {
            this.hasStuff = null;

            byte[] ba = nbt.getByteArray("stuff");

            if (ba.length > 0) {
                this.hasStuff = new boolean[ba.length];

                for (int a = 0; a < ba.length; a++) {
                    this.hasStuff[a] = ba[a] == 1;
                }
            }
        }
    }

    public boolean[] checkCraft(long id) {
        if (level == null) return new boolean[0];

        IGolemProperties props = GolemProperties.fromLong(id);
        ItemStack[] cc = props.generateComponents();

        boolean[] ret = new boolean[cc.length];

        for (int a = 0; a < cc.length; a++) {
            ret[a] = InventoryUtils.checkAdjacentChests(level, worldPosition, cc[a]);
        }

        return ret;
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putLong("golem", golemProps);
        tag.putInt("cost", cost);
        tag.putInt("mcost", maxCost);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        golemProps = tag.getLong("golem");
        cost = tag.getInt("cost");
        maxCost = tag.getInt("mcost");

        if (golemProps >= 0L) {
            try {
                props = GolemProperties.fromLong(golemProps);
                components = props.generateComponents();
            } catch (Exception e) {
                props = null;
                components = null;
                cost = 0;
                golemProps = -1L;
            }
        }
    }

    boolean drawEssentia() {
        if (level == null) return false;

        for (Direction face : DIRECTIONS) {
            BlockEntity te = ArcaneresearchApiHelper.getConnectableTile(level, worldPosition, face);

            if (!(te instanceof IEssentiaTransport ic)) continue;

            Direction opposite = face.getOpposite();

            if (!ic.canOutputTo(opposite)) return false;

            if (ic.getSuctionAmount(opposite) < getSuctionAmount(face)
                    && ic.takeEssentia(Aspect.MECHANISM, 1, opposite) == 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isConnectable(Direction direction) {
        return direction.getAxis().isHorizontal() || direction == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction direction) {
        return isConnectable(direction);
    }

    @Override
    public boolean canOutputTo(Direction direction) {
        return false;
    }

    @Override
    public void setSuction(Aspect aspect, int amount) {
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public Aspect getSuctionType(Direction direction) {
        return Aspect.MECHANISM;
    }

    @Override
    public int getSuctionAmount(Direction direction) {
        return (cost > 0 && golemProps >= 0L) ? 128 : 0;
    }

    @Override
    public Aspect getEssentiaType(Direction direction) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction direction) {
        return 0;
    }

    @Override
    public int takeEssentia(Aspect aspect, int amount, Direction direction) {
        return 0;
    }

    @Override
    public int addEssentia(Aspect aspect, int amount, Direction direction) {
        if (!bufferedEssentia && cost > 0 && golemProps >= 0L && aspect == Aspect.MECHANISM) {
            bufferedEssentia = true;
            return 1;
        }

        return 0;
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    worldPosition.offset(-1, 0, -1),
                    worldPosition.offset(2, 2, 2)
            );
        }

        return renderBox;
    }
}