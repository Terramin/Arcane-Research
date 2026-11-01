package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.lib.aura.VisNetHandler;
import arcaneresearch.common.tiles.TileArcaneresearch;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class TileVisNode extends TileArcaneresearch {
    public boolean nodeRefresh = false;

    protected int nodeCounter = 0;
    protected boolean nodeRegged = false;

    WeakReference<TileVisNode> parent = null;
    ArrayList<WeakReference<TileVisNode>> children = new ArrayList<>();

    public TileVisNode(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public AuraPos getLocation() {
        Level level = getLevel();
        ResourceKey<Level> dim = level != null ? level.dimension() : Level.OVERWORLD;
        return new AuraPos(dim, getBlockPos());
    }

    public abstract int getRange();

    public abstract boolean isSource();

    public int consumeVis(Aspect aspect, int vis) {
        WeakReference<TileVisNode> parentRef = getParent();

        if (!VisNetHandler.isNodeValid(parentRef)) return 0;

        TileVisNode parentNode = parentRef.get();

        if (parentNode == null) return 0;

        int out = parentNode.consumeVis(aspect, vis);

        if (out > 0) {
            triggerConsumeEffect(aspect);
        }

        return out;
    }

    public void removeThisNode() {
        ArrayList<WeakReference<TileVisNode>> snapshot = new ArrayList<>(children);

        for (WeakReference<TileVisNode> ref : snapshot) {
            TileVisNode child = ref != null ? ref.get() : null;

            if (child != null) {
                child.removeThisNode();
            }
        }

        children.clear();

        WeakReference<TileVisNode> parentRef = getParent();

        if (VisNetHandler.isNodeValid(parentRef)) {
            TileVisNode parentNode = parentRef.get();

            if (parentNode != null) {
                parentNode.nodeRefresh = true;
            }
        }

        setParent(null);
        parentChanged();

        if (isSource()) {
            ResourceKey<Level> dim = getDimKey();

            HashMap<AuraPos, WeakReference<TileVisNode>> sourcelist = VisNetHandler.sources.get(dim);

            if (sourcelist == null) {
                sourcelist = new HashMap<>();
            }

            sourcelist.remove(getLocation());
            VisNetHandler.sources.put(dim, sourcelist);
        }

        Level level = getLevel();

        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    protected ResourceKey<Level> getDimKey() {
        Level level = getLevel();
        return level != null ? level.dimension() : Level.OVERWORLD;
    }

    public void onChunkUnload() {
        removeThisNode();
    }

    public void triggerConsumeEffect(Aspect aspect) {
    }

    public WeakReference<TileVisNode> getParent() {
        return this.parent;
    }

    public void setParent(WeakReference<TileVisNode> parent) {
        this.parent = parent;
    }

    public WeakReference<TileVisNode> getRootSource() {
        WeakReference<TileVisNode> parentRef = getParent();

        if (VisNetHandler.isNodeValid(parentRef)) {
            TileVisNode parentNode = parentRef.get();

            if (parentNode != null) {
                return parentNode.getRootSource();
            }
        }

        return isSource() ? new WeakReference<>(this) : null;
    }

    public ArrayList<WeakReference<TileVisNode>> getChildren() {
        return this.children;
    }

    public void parentChanged() {
    }

    public byte getAttunement() {
        return -1;
    }

    public record AuraPos(ResourceKey<Level> dim, BlockPos pos) {
    }
}