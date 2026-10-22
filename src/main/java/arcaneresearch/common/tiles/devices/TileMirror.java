package arcaneresearch.common.tiles.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    private List<ItemStack> outputStacks = new ArrayList<>();

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

    static void updateLinkedState(Level world, BlockPos pos, boolean value) {
        if (world == null || world.isClientSide()) {
            return;
        }
        try {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof arcaneresearch.common.blocks.devices.BlockMirror
                    && state.hasProperty(arcaneresearch.common.blocks.devices.BlockMirror.LINKED)
                    && state.getValue(arcaneresearch.common.blocks.devices.BlockMirror.LINKED) != value) {
                world.setBlock(pos, state.setValue(
                        arcaneresearch.common.blocks.devices.BlockMirror.LINKED, value), 3);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        tag.putBoolean("Linked", linked);
        tag.putInt("LinkX", linkX);
        tag.putInt("LinkY", linkY);
        tag.putInt("LinkZ", linkZ);
        tag.putString("LinkDim", linkDimension.identifier().toString());
        tag.putInt("Instability", instability);
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        super.readSyncNBT(tag);

        linked = hasBoolean(tag, "Linked") ? tag.getBooleanOr("Linked", false) : tag.getBooleanOr("linked", false);
        linkX = tag.getIntOr("LinkX", tag.getIntOr("linkX", 0));
        linkY = tag.getIntOr("LinkY", tag.getIntOr("linkY", 0));
        linkZ = tag.getIntOr("LinkZ", tag.getIntOr("linkZ", 0));
        if (!tag.getStringOr("LinkDim", "").isEmpty()) {
            linkDimension = ResourceKey.create(Registries.DIMENSION,
                    Identifier.parse(tag.getStringOr("LinkDim", "")));
        } else if (hasLegacyLinkDim(tag)) {
            readLegacyLinkDim(tag);
        }
        instability = tag.getIntOr("Instability", tag.getIntOr("instability", 0));
    }

    
    
    private static boolean hasLegacyLinkDim(net.minecraft.world.level.storage.ValueInput tag) {
        if (tag.read("linkDim", com.mojang.serialization.Codec.INT).isPresent()) return true;
        return !tag.getStringOr("linkDim", "").isEmpty();
    }

    private void readLegacyLinkDim(net.minecraft.world.level.storage.ValueInput tag) {
        java.util.Optional<Integer> dimId = tag.read("linkDim", com.mojang.serialization.Codec.INT);
        if (dimId.isPresent()) {
            int dim = dimId.get();
            if (dim == -1) {
                linkDimension = Level.NETHER;
            } else if (dim == 1) {
                linkDimension = Level.END;
            } else {
                linkDimension = Level.OVERWORLD;
            }
            return;
        }
        try {
            linkDimension = ResourceKey.create(Registries.DIMENSION,
                    Identifier.parse(tag.getStringOr("linkDim", "")));
        } catch (Exception ignored) {
        }
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);

        
        
        java.util.List<ItemStack> stacks = new java.util.ArrayList<>();
        for (int i = 0; i < outputStacks.size(); i++) {
            ItemStack stack = outputStacks.get(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        tag.store("Items", ItemStack.CODEC.listOf(), stacks);
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);

        outputStacks.clear();
        for (ItemStack stack : tag.read("Items", ItemStack.CODEC.listOf()).orElse(java.util.List.of())) {
            if (!stack.isEmpty()) {
                outputStacks.add(stack);
            }
        }
    }

    public void restoreLink() {
        if (!isDestinationValid()) return;

        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return;

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (te instanceof TileMirror target) {

            target.linked = true;
            target.linkX = worldPosition.getX();
            target.linkY = worldPosition.getY();
            target.linkZ = worldPosition.getZ();
            target.linkDimension = level.dimension();
            target.syncTile(false);

            linked = true;
            setChanged();
            target.setChanged();
            syncTile(false);
            updateLinkedState(targetWorld, new BlockPos(linkX, linkY, linkZ), true);
            updateLinkedState(level, worldPosition, true);
        }
    }

    public void invalidateLink() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return;

        if (!isChunkLoaded(targetWorld, linkX, linkZ)) return;

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (te instanceof TileMirror target) {
            target.linked = false;
            setChanged();
            target.setChanged();
            target.syncTile(false);
            updateLinkedState(targetWorld, new BlockPos(linkX, linkY, linkZ), false);
        }
    }

    public boolean isLinkValid() {
        if (!linked) return false;

        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (!(te instanceof TileMirror target)) {
            setLinked(false);
            return false;
        }

        if (!target.linked) {
            setLinked(false);
            return false;
        }

        if (target.linkX != worldPosition.getX() ||
                target.linkY != worldPosition.getY() ||
                target.linkZ != worldPosition.getZ() ||
                !target.linkDimension.equals(level.dimension())) {
            setLinked(false);
            return false;
        }

        return true;
    }

    private void setLinked(boolean value) {
        linked = value;
        setChanged();
        syncTile(false);
        if (level != null && !level.isClientSide()) {
            updateLinkedState(level, worldPosition, value);
        }
    }

    public boolean isLinkValidSimple() {
        if (!linked) return false;

        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (!(te instanceof TileMirror target)) return false;

        return target.linked &&
                target.linkX == worldPosition.getX() &&
                target.linkY == worldPosition.getY() &&
                target.linkZ == worldPosition.getZ() &&
                target.linkDimension.equals(level.dimension());
    }

    public boolean isDestinationValid() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (!(te instanceof TileMirror target)) {
            setLinked(false);
            return false;
        }

        return !target.isLinkValid();
    }

    public boolean transport(ItemEntity itemEntity) {
        ItemStack items = itemEntity.getItem();
        if (!linked || !isLinkValid()) return false;

        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (te instanceof TileMirror target) {
            target.addStack(items.copy());
            addInstability(null, items.getCount());
            itemEntity.discard();
            setChanged();
            target.setChanged();

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
        setChanged();
        return true;
    }

    public void addStack(ItemStack stack) {
        outputStacks.add(stack.copy());
        setChanged();
    }

    private void ejectItems() {
        if (outputStacks.isEmpty() || count <= 20) return;

        int index = level.getRandom().nextInt(outputStacks.size());
        ItemStack stack = outputStacks.get(index);

        if (stack.isEmpty()) {
            outputStacks.remove(index);
            return;
        }

        ItemStack outItem = stack.copy();
        outItem.setCount(1);

        if (spawnItem(outItem)) {
            stack.shrink(1);
            addInstability(null, 1);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            }

            if (stack.isEmpty()) {
                outputStacks.remove(index);
            }
        }
        setChanged();
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
                    facing.getStepX() * 0.35 + (level.getRandom().nextFloat() - 0.5f) * 0.12f,
                    facing.getStepY() * 0.35 + level.getRandom().nextFloat() * 0.06f,
                    facing.getStepZ() * 0.35 + (level.getRandom().nextFloat() - 0.5f) * 0.12f);
            itemEntity.setPortalCooldown(40);

            level.addFreshEntity(itemEntity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void addInstability(Level targetWorld, int amount) {
        instability += amount;
        setChanged();

        if (targetWorld != null) {
            BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
            if (te instanceof TileMirror target) {
                target.instability += amount;
                if (target.instability < 0) target.instability = 0;
                target.setChanged();
            }
        }
    }

    private void checkInstability() {
        if (instability > INSTABILITY_THRESHOLD) {
            AuraHelper.polluteAura(level, worldPosition, 1.0f, true);
            instability -= INSTABILITY_THRESHOLD;
            setChanged();
        }

        if (instability > 0 && count % 100 == 0) {
            instability--;
        }
    }

    private Direction getFacing() {

        BlockState state = getBlockState();
        if (state.hasProperty(arcaneresearch.common.blocks.devices.BlockMirror.FACING)) {
            return state.getValue(arcaneresearch.common.blocks.devices.BlockMirror.FACING);
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

            if (level != null && level.isClientSide()) {
                spawnWispyMotes();
            }
            return true;
        }
        return super.triggerEvent(id, data);
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnWispyMotes() {
        Direction face = getFacing();
        double xx = worldPosition.getX() + 0.33 + level.getRandom().nextFloat() * 0.33f - face.getStepX() / 2.0;
        double yy = worldPosition.getY() + 0.33 + level.getRandom().nextFloat() * 0.33f - face.getStepY() / 2.0;
        double zz = worldPosition.getZ() + 0.33 + level.getRandom().nextFloat() * 0.33f - face.getStepZ() / 2.0;
        FXDispatcher.INSTANCE.drawWispyMotes(xx, yy, zz,
                face.getStepX() / 50.0 + level.getRandom().nextGaussian() * 0.01,
                face.getStepY() / 50.0 + level.getRandom().nextGaussian() * 0.01,
                face.getStepZ() / 50.0 + level.getRandom().nextGaussian() * 0.01,
                10 + level.getRandom().nextInt(20),
                level.getRandom().nextFloat() / 3.0f, 0.0f, level.getRandom().nextFloat() / 2.0f,
                (float) (level.getRandom().nextGaussian() * 0.01));
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
    public ItemStack getItem(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

        if (!linked || !isLinkValid()) {
            spawnItem(stack.copy());
            return;
        }

        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) {
            spawnItem(stack.copy());
            return;
        }

        BlockEntity te = targetWorld.getBlockEntity(new BlockPos(linkX, linkY, linkZ));
        if (te instanceof TileMirror target) {
            target.addStack(stack.copy());
            addInstability(null, stack.getCount());

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            }
        } else {
            spawnItem(stack.copy());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        outputStacks.clear();
    }
}
