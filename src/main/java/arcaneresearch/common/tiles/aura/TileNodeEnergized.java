package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IAspectContainer;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.lib.aura.VisNetHandler;
import org.jetbrains.annotations.NotNull;

public class TileNodeEnergized extends TileVisNode implements IAspectContainer {
    AspectList visBase = new AspectList();
    AspectList vis = new AspectList();
    String id = "blank";

    private AspectList auraBase = new AspectList()
            .add(Aspect.AIR, 20).add(Aspect.FIRE, 20).add(Aspect.EARTH, 20)
            .add(Aspect.WATER, 20).add(Aspect.ORDER, 20).add(Aspect.ENTROPY, 20);

    private NodeType nodeType = NodeType.NORMAL;
    private NodeModifier nodeModifier = null;

    public TileNodeEnergized(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileNodeEnergized tile) {
        if (level == null) return;

        if (tile.getNodeType() == NodeType.UNSTABLE && level.random.nextInt(500) == 1) {
            tile.visBase = new AspectList();
        }

        if (tile.visBase.size() == 0 && tile.getAuraBase().size() > 0) {
            tile.setupNode();
            if (tile.visBase.size() > 0) {
                tile.syncAspectsToClients();
            }
        }

        tile.vis = tile.visBase.copy();

        if (level.getGameTime() % 40 == 0) {
            VisNetHandler.addSource(level, tile);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, TileNodeEnergized tile) {
    }

    private static AspectList reduceToPrimals(AspectList source) {
        AspectList out = new AspectList();

        if (source == null) return out;

        for (Aspect aspect : source.getAspects()) {
            if (aspect == null) continue;
            reduceToPrimals(aspect, source.getAmount(aspect), out);
        }

        return out;
    }

    private static void reduceToPrimals(Aspect aspect, int amount, AspectList out) {
        if (aspect.isPrimal()) {
            out.merge(aspect, amount);
            return;
        }

        Aspect[] components = aspect.getComponents();
        if (components == null) return;

        for (Aspect component : components) {
            if (component == null) continue;
            reduceToPrimals(component, amount, out);
        }
    }

    /**
     * Derives the visible aspect list from the node's aura. Kept separate from
     * {@link #setupNode()} so the client can rebuild it from the always-synced
     * aura when the custom {@code AEB} update key was not delivered.
     */
    private AspectList computeVis(AspectList aura, net.minecraft.util.RandomSource random) {
        AspectList out = new AspectList();

        AspectList temp = reduceToPrimals(aura);

        NodeModifier mod = getNodeModifier();

        for (Aspect aspect : temp.getAspects()) {
            if (aspect == null) continue;

            int amt = temp.getAmount(aspect);

            if (mod == NodeModifier.BRIGHT) {
                amt = (int) (amt * 1.2F);
            } else if (mod == NodeModifier.PALE) {
                amt = (int) (amt * 0.8F);
            } else if (mod == NodeModifier.FADING) {
                amt = (int) (amt * 0.5F);
            }

            amt = Math.max(0, Math.round((float) Math.sqrt(amt)));

            if (random != null) {
                amt += random.nextInt(5) - 2;
            }

            if (amt >= 1) {
                out.merge(aspect, amt);
            }
        }

        return out;
    }

    public void setupNode() {
        Level lvl = level;
        boolean unstable = getNodeType() == NodeType.UNSTABLE;

        this.visBase = computeVis(getAuraBase(), unstable && lvl != null ? lvl.random : null);

        setChanged();

        if (lvl != null) {
            lvl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    public int consumeVis(Aspect aspect, int amount) {
        int drain = Math.min(this.vis.getAmount(aspect), amount);

        if (drain > 0) {
            this.vis.reduce(aspect, drain);
        }

        return drain;
    }

    public AspectList getAuraBase() {
        return this.auraBase;
    }

    @Override
    public AspectList getAspects() {
        return this.visBase;
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects != null) {
            this.auraBase = aspects.copy();
        }
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return false;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContain(AspectList ot) {
        return false;
    }

    @Override
    public int containerContains(Aspect tag) {
        return 0;
    }

    public NodeType getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        if (nodeType != null) {
            this.nodeType = nodeType;
        }
    }

    public NodeModifier getNodeModifier() {
        return this.nodeModifier;
    }

    public void setNodeModifier(NodeModifier nodeModifier) {
        this.nodeModifier = nodeModifier;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        if (id != null) {
            this.id = id;
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        // TileArcaneresearch.saveAdditional() delegates to writeSyncNBT(), so
        // the energized node data is written by the override below as well.
        super.saveAdditional(tag);
    }

    @Override
    protected void writeSyncNBT(@NotNull CompoundTag tag) {
        super.writeSyncNBT(tag);

        // getUpdateTag() uses writeSyncNBT() only. Without this the client
        // never receives the aspect list when the transducer creates the
        // energized node at runtime, so no aspect icons are drawn above it.
        tag.putString("nodeId", this.id);
        tag.putByte("type", (byte) getNodeType().ordinal());

        NodeModifier mod = getNodeModifier();
        tag.putByte("modifier", mod == null ? (byte) -1 : (byte) mod.ordinal());

        ListTag tlist = new ListTag();

        for (Aspect aspect : this.visBase.getAspects()) {
            if (aspect == null) continue;

            CompoundTag f = new CompoundTag();
            f.putString("key", aspect.getTag());
            f.putInt("amount", this.visBase.getAmount(aspect));
            tlist.add(f);
        }

        tag.put("AEB", tlist);

        this.auraBase.writeToNBT(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        try {
            loadEnergized(tag);
        } catch (Exception ignored) {
        }
    }

    /**
     * Forces the aspect data to clients with our own channel. The converter and
     * transducer call this right after {@link #setupNode()} so a stale or
     * coalesced block-entity update cannot leave the goggles popup empty.
     */
    public void syncAspectsToClients() {
        if (level == null || level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        writeSyncNBT(tag);
        sendMessageToClients(tag);
    }

    @Override
    public void messageFromServer(CompoundTag nbt) {
        if (level != null && level.isClientSide && nbt != null) {
            loadEnergized(nbt);
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        try {
            loadEnergized(tag);
        } catch (Exception ignored) {
        }
    }

    private void loadEnergized(CompoundTag tag) {
        this.id = tag.getString("nodeId");

        NodeType[] types = NodeType.values();
        int typeOrd = tag.getByte("type");

        if (typeOrd < 0 || typeOrd >= types.length) {
            typeOrd = 0;
        }

        setNodeType(types[typeOrd]);

        NodeModifier[] modifiers = NodeModifier.values();
        int modOrd = tag.getByte("modifier");

        setNodeModifier(modOrd >= 0 && modOrd < modifiers.length ? modifiers[modOrd] : null);

        this.visBase = new AspectList();

        if (tag.contains("AEB", Tag.TAG_LIST)) {
            ListTag tlist = tag.getList("AEB", Tag.TAG_COMPOUND);

            for (int j = 0; j < tlist.size(); j++) {
                CompoundTag rs = tlist.getCompound(j);

                if (rs.contains("key")) {
                    Aspect a = Aspect.getAspect(rs.getString("key"));

                    if (a != null) {
                        this.visBase.add(a, rs.getInt("amount"));
                    }
                }
            }
        }

        this.auraBase = new AspectList();
        this.auraBase.readFromNBT(tag);

        // The derived vis list is written under the custom "AEB" key. A block
        // entity update emitted before setupNode() ran (or an older world) can
        // omit it, which left the client with no aspects and made the goggles
        // popup disappear on a freshly converted/stabilized node. The aura is
        // always synced under the standard "Aspects" key, so rebuild the
        // display list from it as a fallback.
        if (this.visBase.size() == 0 && this.auraBase.size() > 0) {
            this.visBase = computeVis(this.auraBase, null);
        }
    }
}
