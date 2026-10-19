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
import java.util.ArrayList;
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
        if (tile.getNodeType() == NodeType.UNSTABLE && level.random.nextInt(500) == 1) {
            tile.visBase = new AspectList();
        }
        if (tile.visBase.size() == 0 && tile.getAuraBase().size() > 0) {
            tile.setupNode();
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
        if (source == null) {
            return out;
        }
        ArrayList<Aspect> stack = new ArrayList<>();
        for (Aspect aspect : source.getAspects()) {
            if (aspect == null) {
                continue;
            }
            int amount = source.getAmount(aspect);
            stack.clear();
            stack.add(aspect);
            ArrayList<Integer> amounts = new ArrayList<>();
            amounts.add(amount);
            while (!stack.isEmpty()) {
                Aspect current = stack.remove(stack.size() - 1);
                int currentAmount = amounts.remove(amounts.size() - 1);
                if (current.isPrimal()) {
                    out.merge(current, currentAmount);
                } else if (current.getComponents() != null) {
                    for (Aspect component : current.getComponents()) {
                        if (component != null) {
                            stack.add(component);
                            amounts.add(currentAmount);
                        }
                    }
                }
            }
        }
        return out;
    }
    public void setupNode() {
        this.visBase = new AspectList();
        AspectList temp = reduceToPrimals(getAuraBase());
        for (Aspect aspect : temp.getAspects()) {
            if (aspect == null) {
                continue;
            }
            int amt = temp.getAmount(aspect);
            if (getNodeModifier() == NodeModifier.BRIGHT) {
                amt = (int) (amt * 1.2F);
            }
            if (getNodeModifier() == NodeModifier.PALE) {
                amt = (int) (amt * 0.8F);
            }
            if (getNodeModifier() == NodeModifier.FADING) {
                amt = (int) (amt * 0.5F);
            }
            amt = Math.max(0, Math.round((float) Math.sqrt(amt)));
            if (getNodeType() == NodeType.UNSTABLE && level != null) {
                amt += level.random.nextInt(5) - 2;
            }
            if (amt >= 1) {
                this.visBase.merge(aspect, amt);
            }
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
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
        this.auraBase = aspects.copy();
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
    @Override
    public byte getAttunement() {
        return -1;
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("nodeId", this.id);
        tag.putByte("type", (byte) getNodeType().ordinal());
        tag.putByte("modifier", getNodeModifier() == null ? (byte) -1 : (byte) getNodeModifier().ordinal());
        ListTag tlist = new ListTag();
        tag.put("AEB", tlist);
        for (Aspect aspect : this.visBase.getAspects()) {
            if (aspect != null) {
                CompoundTag f = new CompoundTag();
                f.putString("key", aspect.getTag());
                f.putInt("amount", this.visBase.getAmount(aspect));
                tlist.add(f);
            }
        }
        this.auraBase.writeToNBT(tag);
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadEnergized(tag);
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
        setNodeType(NodeType.values()[tag.getByte("type") % NodeType.values().length]);
        byte mod = tag.getByte("modifier");
        setNodeModifier(mod >= 0 && mod < NodeModifier.values().length
                ? NodeModifier.values()[mod] : null);
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
    }
}
