package arcaneresearch.common.tiles.devices;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectSource;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.blocks.devices.BlockMirror;
import arcaneresearch.common.lib.events.EssentiaHandler;
import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TileMirrorEssentia extends TileArcaneresearch implements IAspectSource {

    private static final int INSTABILITY_THRESHOLD = 64;

    public boolean linked = false;
    public int linkX = 0;
    public int linkY = 0;
    public int linkZ = 0;
    public ResourceKey<Level> linkDimension = Level.OVERWORLD;
    public Direction linkedFacing = null;
    public int instability = 0;

    private int count = 0;
    private int linkCheckInterval = 40;

    @Nullable
    private BlockPos cachedLinkedPos;
    private int cachedLinkX = Integer.MIN_VALUE;
    private int cachedLinkY = Integer.MIN_VALUE;
    private int cachedLinkZ = Integer.MIN_VALUE;

    public TileMirrorEssentia(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileMirrorEssentia(BlockPos pos, BlockState state) {
        this(ModBlockEntities.MIRROR_ESSENTIA.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileMirrorEssentia tile) {
        tile.count++;
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

    public void restoreLink() {
        if (!isDestinationValid()) return;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (te instanceof TileMirrorEssentia target) {
            target.linked = true;
            target.linkX = worldPosition.getX();
            target.linkY = worldPosition.getY();
            target.linkZ = worldPosition.getZ();
            assert level != null;
            target.linkDimension = level.dimension();
            target.syncTile(false);
            linkedFacing = getFacingAt(targetWorld, getLinkedPos());
            linked = true;
            syncTile(false);
            TileMirror.updateLinkedState(targetWorld, getLinkedPos(), true);
            TileMirror.updateLinkedState(level, worldPosition, true);
        }
    }

    public void invalidateLink() {
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return;
        if (!isChunkLoaded(targetWorld, linkX, linkZ)) return;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (te instanceof TileMirrorEssentia target) {
            target.linked = false;
            target.linkedFacing = null;
            setChanged();
            target.syncTile(false);
            TileMirror.updateLinkedState(targetWorld, getLinkedPos(), false);
        }
    }

    public boolean isLinkValid() {
        if (!linked) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (!(te instanceof TileMirrorEssentia target)) {
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
            TileMirror.updateLinkedState(level, worldPosition, false);
        }
    }

    public boolean isLinkValidSimple() {
        if (!linked) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockEntity te = targetWorld.getBlockEntity(getLinkedPos());
        if (!(te instanceof TileMirrorEssentia target)) return false;
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
        if (!(te instanceof TileMirrorEssentia target)) {
            setLinked();
            return false;
        }
        return !target.isLinkValid();
    }

    protected void addInstability(int amount) {
        instability += amount;
        setChanged();
        if (null != null) {
            BlockEntity te = ((Level) null).getBlockEntity(getLinkedPos());
            if (te instanceof TileMirrorEssentia target) {
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

    @Override
    public AspectList getAspects() {
        return null;
    }

    @Override
    public void setAspects(AspectList aspects) {
    }

    @Override
    public boolean doesContainerAccept(Aspect aspect) {
        if (!isLinkValid()) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockPos linkedPos = getLinkedPos();
        if (linkedFacing == null) {
            linkedFacing = getFacingAt(targetWorld, linkedPos);
        }
        BlockEntity te = targetWorld.getBlockEntity(linkedPos);
        return te instanceof TileMirrorEssentia
                && EssentiaHandler.canAcceptEssentia(te, aspect, linkedFacing, 8, true);
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (amount > 1 || !isLinkValid()) return amount;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return amount;
        BlockPos linkedPos = getLinkedPos();
        if (linkedFacing == null) {
            linkedFacing = getFacingAt(targetWorld, linkedPos);
        }
        BlockEntity te = targetWorld.getBlockEntity(linkedPos);
        if (te instanceof TileMirrorEssentia) {
            boolean b = EssentiaHandler.addEssentia(te, aspect, linkedFacing, 8, true, 5);
            if (b) {
                addInstability(amount);
            }
            return b ? 0 : amount;
        }
        return amount;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (amount > 1 || !isLinkValid()) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockPos linkedPos = getLinkedPos();
        if (linkedFacing == null) {
            linkedFacing = getFacingAt(targetWorld, linkedPos);
        }
        BlockEntity te = targetWorld.getBlockEntity(linkedPos);
        if (te instanceof TileMirrorEssentia) {
            boolean b = EssentiaHandler.drainEssentia(te, aspect, linkedFacing, 8, true, 5);
            if (b) {
                addInstability(amount);
            }
            return b;
        }
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList aspects) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        if (amount > 1 || !isLinkValid()) return false;
        ServerLevel targetWorld = getTargetWorld();
        if (targetWorld == null) return false;
        BlockPos linkedPos = getLinkedPos();
        if (linkedFacing == null) {
            linkedFacing = getFacingAt(targetWorld, linkedPos);
        }
        BlockEntity te = targetWorld.getBlockEntity(linkedPos);
        return te instanceof TileMirrorEssentia
                && EssentiaHandler.findEssentia(te, aspect, linkedFacing, 8, true);
    }

    @Override
    public boolean doesContainerContain(AspectList aspects) {
        return false;
    }

    @Override
    public int containerContains(Aspect aspect) {
        return 0;
    }

    @Override
    public boolean isBlocked() {
        return false;
    }

    private Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockMirror.FACING)) {
            return state.getValue(BlockMirror.FACING);
        }
        return Direction.NORTH;
    }

    private Direction getFacingAt(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.hasProperty(BlockMirror.FACING)) {
            return state.getValue(BlockMirror.FACING);
        }
        return Direction.DOWN;
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
}