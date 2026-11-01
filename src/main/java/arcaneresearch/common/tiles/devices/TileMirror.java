package arcaneresearch.common.tiles.devices;

import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.blocks.devices.BlockMirror;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileMirror extends TileArcaneresearch implements Container {

    private static final int INSTABILITY_THRESHOLD = 128;

    public boolean linked = false;
    public int linkX = 0;
    public int linkY = 0;
    public int linkZ = 0;
    public ResourceKey<Level> linkDimension = Level.OVERWORLD;
    public int instability = 0;

    private int count = 0;
    private int linkCheckInterval = 40;
    private final List<ItemStack> outputStacks = new ArrayList<>();

    @Nullable
    private BlockPos cachedLinkedPos;
    private int cachedLinkX = Integer.MIN_VALUE;
    private int cachedLinkY = Integer.MIN_VALUE;
    private int cachedLinkZ = Integer.MIN_VALUE;

    public TileMirror(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileMirror(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MIRROR_ITEM.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileMirror tile) {
        tile.count++;
        tile.ejectItems();
        tile.checkInstability();
        if (tile.count % tile.linkCheckInterval == 0) {
            if (!tile.isLinkValidSimple()) {
                if (tile.linkCheckInterval < 600) {
                    tile.linkCheckInterval += 20;
                }
                tile.restoreLink();
            } else {
                tile.linkCheckInterval = 40;
            }
        }
    }

    private BlockPos getLinkedPos() {
        BlockPos pos = cachedLinkedPos;
        if (pos != null && cachedLinkX == linkX && cachedLinkY == linkY && cachedLinkZ == linkZ) {
            return pos;
        }
        pos = new BlockPos(linkX, linkY, linkZ);
        cachedLinkedPos = pos;
        cachedLinkX = linkX;
        cachedLinkY = linkY;
        cachedLinkZ = linkZ;
        return pos;
    }

    static void updateLinkedState(Level world, BlockPos pos, boolean value) {
        if (world == null || world.isClientSide) {
            return;
        }
        try {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockMirror
                    && state.hasProperty(BlockMirror.LINKED)
                    && state.getValue(BlockMirror.LINKED) != value) {
                world.setBlock(pos, state.setValue(BlockMirror.LINKED, value), 3);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putBoolean("Linked", linked);
        tag.putInt("LinkX", linkX);
        tag.putInt("LinkY", linkY);
        tag.putInt("LinkZ", linkZ);
        tag.putString("LinkDim", linkDimension.location().toString());
        tag.putInt("Instability", instability);
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        linked = tag.contains("Linked") ? tag.getBoolean("Linked") : tag.getBoolean("linked");
        linkX = tag.contains("LinkX") ? tag.getInt("LinkX") : tag.getInt("linkX");
        linkY = tag.contains("LinkY") ? tag.getInt("LinkY") : tag.getInt("linkY");
        linkZ = tag.contains("LinkZ") ? tag.getInt("LinkZ") : tag.getInt("linkZ");
        if (tag.contains("LinkDim")) {
            ResourceLocation dimLoc = ResourceLocation.tryParse(tag.getString("LinkDim"));
            if (dimLoc != null) {
                linkDimension = ResourceKey.create(Registries.DIMENSION, dimLoc);
            }
        } else if (tag.contains("linkDim")) {
            readLegacyLinkDim(tag);
        }
        instability = tag.contains("Instability") ? tag.getInt("Instability") : tag.getInt("instability");
    }

    private void readLegacyLinkDim(CompoundTag tag) {
        int dim = tag.getInt("linkDim");
        if (dim == -1) {
            linkDimension = Level.NETHER;
        } else if (dim == 1) {
            linkDimension = Level.END;
        } else {
            linkDimension = Level.OVERWORLD;
        }
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag itemList = new ListTag();
        for (ItemStack stack : outputStacks) {
            if (!stack.isEmpty()) {
                itemList.add(stack.save(new CompoundTag()));
            }
        }
        tag.put("Items", itemList);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        outputStacks.clear();
        ListTag itemList = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0, n = itemList.size(); i < n; i++) {
            ItemStack stack = ItemStack.of(itemList.getCompound(i));
            if (!stack.isEmpty()) {
                outputStacks.add(stack);
            }
        }
    }

    public void restoreLink() {
        if (!isDestinationValid()) return;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (te instanceof TileMirror target) {
            target.linked = true;
            target.linkX = worldPosition.getX();
            target.linkY = worldPosition.getY();
            target.linkZ = worldPosition.getZ();
            assert level != null;
            target.linkDimension = level.dimension();
            target.syncTile(false);
            linked = true;
            syncTile(false);
            updateLinkedState(targetWorld, getLinkedPos(), true);
            updateLinkedState(level, worldPosition, true);
        }
    }

    public void invalidateLink() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return;
        if (!isChunkLoaded(targetWorld, linkX, linkZ)) return;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (te instanceof TileMirror target) {
            target.linked = false;
            target.syncTile(false);
            updateLinkedState(targetWorld, getLinkedPos(), false);
        }
    }

    public boolean isLinkValid() {
        if (!linked) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (!(te instanceof TileMirror target)) {
            setLinked();
            return false;
        }
        if (!target.linked) {
            setLinked();
            return false;
        }
        if (target.linkX != worldPosition.getX()
                || target.linkY != worldPosition.getY()
                || target.linkZ != worldPosition.getZ()
                || !target.linkDimension.equals(level != null ? level.dimension() : null)) {
            setLinked();
            return false;
        }
        return true;
    }

    private void setLinked() {
        linked = false;
        syncTile(false);
        if (level != null && !level.isClientSide) {
            updateLinkedState(level, worldPosition, false);
        }
    }

    public boolean isLinkValidSimple() {
        if (!linked) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (!(te instanceof TileMirror target)) return false;
        if (!target.linked
                || target.linkX != worldPosition.getX()
                || target.linkY != worldPosition.getY()
                || target.linkZ != worldPosition.getZ()) return false;
        assert level != null;
        return target.linkDimension.equals(level.dimension());
    }

    public boolean isDestinationValid() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (!(te instanceof TileMirror target)) {
            setLinked();
            return false;
        }
        return !target.isLinkValid();
    }

    public boolean transport(ItemEntity itemEntity) {
        if (!linked || !isLinkValid()) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (te instanceof TileMirror target) {
            ItemStack items = itemEntity.getItem();
            target.addStack(items.copy());
            addInstability(items.getCount());
            itemEntity.discard();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            }
            return true;
        }
        return false;
    }

    public boolean transportDirect(ItemStack items) {
        if (items.isEmpty()) return false;
        addStack(items.copy());
        return true;
    }

    public void addStack(ItemStack stack) {
        outputStacks.add(stack.copy());
        setChanged();
    }

    private void ejectItems() {
        if (outputStacks.isEmpty() || count <= 20) return;
        assert level != null;
        int index = level.random.nextInt(outputStacks.size());
        ItemStack stack = outputStacks.get(index);
        if (stack.isEmpty()) {
            outputStacks.remove(index);
            setChanged();
            return;
        }
        ItemStack outItem = stack.copy();
        outItem.setCount(1);
        if (spawnItem(outItem)) {
            stack.shrink(1);
            addInstability(1);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            }
            if (stack.isEmpty()) {
                outputStacks.remove(index);
            }
        }
    }

    private boolean spawnItem(ItemStack stack) {
        if (level == null || stack.isEmpty()) return false;
        try {
            Direction facing = getFacing();
            double x = worldPosition.getX() + 0.5 + facing.getStepX() * 0.6;
            double y = worldPosition.getY() + 0.25 + facing.getStepY() * 0.6;
            double z = worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.6;
            if (!level.getBlockState(BlockPos.containing(x, y, z)).isAir()) {
                x = worldPosition.getX() + 0.5;
                y = worldPosition.getY() + 0.25;
                z = worldPosition.getZ() + 0.5;
            }
            ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack);
            itemEntity.setDeltaMovement(
                    facing.getStepX() * 0.35 + (level.random.nextFloat() - 0.5f) * 0.12f,
                    facing.getStepY() * 0.35 + level.random.nextFloat() * 0.06f,
                    facing.getStepZ() * 0.35 + (level.random.nextFloat() - 0.5f) * 0.12f);
            itemEntity.setPortalCooldown(40);
            level.addFreshEntity(itemEntity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void addInstability(int amount) {
        instability += amount;
        setChanged();
    }

    private void checkInstability() {
        if (instability > INSTABILITY_THRESHOLD) {
            AuraHelper.polluteAura(level, worldPosition, 1.0f, true);
            instability -= INSTABILITY_THRESHOLD;
            setChanged();
        }
        if (instability > 0 && count % 100 == 0) {
            instability--;
            setChanged();
        }
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockMirror.FACING)) {
            return state.getValue(BlockMirror.FACING);
        }
        return Direction.NORTH;
    }

    private ServerLevel getTargetWorld() {
        if (level == null || level.isClientSide()) return null;
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        return server.getLevel(linkDimension);
    }

    private boolean isChunkLoaded(ServerLevel world, int x, int z) {
        return world.hasChunkAt(new BlockPos(x, 0, z));
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == 1) {
            if (level != null && level.isClientSide) {
                spawnWispyMotes();
            }
            return true;
        }
        return super.triggerEvent(id, data);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnWispyMotes() {
        Direction face = getFacing();
        assert level != null;
        double xx = worldPosition.getX() + 0.33 + level.random.nextFloat() * 0.33f - face.getStepX() / 2.0;
        double yy = worldPosition.getY() + 0.33 + level.random.nextFloat() * 0.33f - face.getStepY() / 2.0;
        double zz = worldPosition.getZ() + 0.33 + level.random.nextFloat() * 0.33f - face.getStepZ() / 2.0;
        FXDispatcher.INSTANCE.drawWispyMotes(xx, yy, zz,
                face.getStepX() / 50.0 + level.random.nextGaussian() * 0.01,
                face.getStepY() / 50.0 + level.random.nextGaussian() * 0.01,
                face.getStepZ() / 50.0 + level.random.nextGaussian() * 0.01,
                10 + level.random.nextInt(20),
                level.random.nextFloat() / 3.0f, 0.0f, level.random.nextFloat() / 2.0f,
                (float) (level.random.nextGaussian() * 0.01));
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (!linked || !isLinkValid()) {
            spawnItem(stack.copy());
            return;
        }
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) {
            spawnItem(stack.copy());
            return;
        }
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (te instanceof TileMirror target) {
            target.addStack(stack.copy());
            addInstability(stack.getCount());
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            }
        } else {
            spawnItem(stack.copy());
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        outputStacks.clear();
    }
}