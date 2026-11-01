package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWandable;
import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.common.lib.aura.INode;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeTapHelper;
import arcaneresearch.common.lib.aura.NodeType;
import arcaneresearch.common.tiles.TileArcaneresearch;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class TileJarNode extends TileArcaneresearch implements INode, IWandable {
    private AspectList aspects = new AspectList();
    private AspectList aspectsBase = new AspectList();
    private NodeType nodeType = NodeType.NORMAL;
    private NodeModifier nodeModifier = null;
    private String id = "";

    private transient Entity drainTarget;
    private transient Aspect drainAspect;
    private transient long lastTapGameTime = -100L;

    private int drainEntityId = -1;
    private String drainAspectTag = "";
    private long drainTime = -100L;
    private transient long drainSeenTick = -100L;

    private long animate = 0L;
    private int regenTick = 0;

    public TileJarNode(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileJarNode tile) {
        if (level == null) return;

        tile.regenTick++;

        if (tile.regenTick < 400) return;

        tile.regenTick = 0;

        AspectList base = tile.getAspectsBase();

        if (base.size() <= 0) return;

        ArrayList<Aspect> below = new ArrayList<>();

        for (Aspect a : base.getAspects()) {
            if (a != null && tile.getAspects().getAmount(a) < base.getAmount(a)) {
                below.add(a);
            }
        }

        if (below.isEmpty()) return;

        Aspect pick = below.get(level.random.nextInt(below.size()));

        tile.addToContainer(pick, 1);
        tile.markDirtyAndSync();
    }

    public void rememberDrain(Entity target, Aspect aspect) {
        this.drainTarget = target;
        this.drainAspect = aspect;

        if (level == null) return;

        this.lastTapGameTime = level.getGameTime();

        if (!level.isClientSide && target != null) {
            this.drainEntityId = target.getId();
            this.drainAspectTag = aspect != null ? aspect.getTag() : "";
            this.drainTime = level.getGameTime();
            syncTile(false);
        }
    }

    public void clearDrain() {
        this.drainTarget = null;
        this.drainAspect = null;

        if (level != null && !level.isClientSide) {
            this.drainEntityId = -1;
            this.drainAspectTag = "";
            syncTile(false);
        }
    }

    public Entity getDrainTarget() {
        if (this.drainTarget == null && this.drainEntityId >= 0 && level != null && level.isClientSide) {
            Entity e = level.getEntity(this.drainEntityId);

            if (e != null) {
                return e;
            }
        }

        return this.drainTarget;
    }

    public Aspect getDrainAspect() {
        if (this.drainAspect == null && this.drainAspectTag != null && !this.drainAspectTag.isEmpty()) {
            Aspect a = Aspect.getAspect(this.drainAspectTag);

            if (a != null) {
                return a;
            }
        }

        return this.drainAspect;
    }

    public long getLastTapGameTime() {
        if (level != null && level.isClientSide) {
            return Math.max(this.lastTapGameTime, this.drainSeenTick);
        }

        return this.lastTapGameTime;
    }

    public long getAnimate() {
        return this.animate;
    }

    public void setAnimate(long animate) {
        this.animate = animate;
        syncTile(true);
    }

    @Override
    public AspectList getAspects() {
        return this.aspects;
    }

    @Override
    public void setAspects(AspectList aspects) {
        if (aspects == null) return;

        this.aspects = aspects.copy();
        this.aspectsBase = aspects.copy();
    }

    @Override
    public AspectList getAspectsBase() {
        return this.aspectsBase;
    }

    @Override
    public boolean doesContainerAccept(Aspect tag) {
        return true;
    }

    @Override
    public int addToContainer(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return amount;

        int base = this.aspectsBase.getAmount(aspect);
        int current = this.aspects.getAmount(aspect);
        int out = Math.max(0, current + amount - base);

        this.aspects.add(aspect, amount - out);
        setChanged();
        return out;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
        if (aspect == null || amount <= 0) return false;

        if (this.aspects.getAmount(aspect) >= amount) {
            this.aspects.remove(aspect, amount);
            setChanged();
            return true;
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean takeFromContainer(AspectList ot) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Aspect tag, int amount) {
        if (tag == null) return false;
        return this.aspects.getAmount(tag) >= amount;
    }

    @Override
    @Deprecated
    public boolean doesContainerContain(AspectList ot) {
        if (ot == null) return false;

        for (Aspect tt : ot.getAspects()) {
            if (tt == null) continue;

            if (this.aspects.getAmount(tt) < ot.getAmount(tt)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int containerContains(Aspect tag) {
        if (tag == null) return 0;
        return this.aspects.getAmount(tag);
    }

    @Override
    public NodeType getNodeType() {
        return this.nodeType;
    }

    @Override
    public void setNodeType(NodeType nodeType) {
        if (nodeType != null) {
            this.nodeType = nodeType;
        }
    }

    @Override
    public NodeModifier getNodeModifier() {
        return this.nodeModifier;
    }

    @Override
    public void setNodeModifier(NodeModifier nodeModifier) {
        this.nodeModifier = nodeModifier;
    }

    @Override
    public int getNodeVisBase(Aspect aspect) {
        if (aspect == null) return 0;
        return this.aspectsBase.getAmount(aspect);
    }

    @Override
    public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
        if (aspect == null) return;

        int target = Math.max(0, nodeVisBase);
        int current = this.aspectsBase.getAmount(aspect);

        if (current < target) {
            this.aspectsBase.merge(aspect, target);
        } else if (current > target) {
            this.aspectsBase.reduce(aspect, current - target);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    @Override
    public boolean onWandRightClick(Level level, ItemStack wandstack, Player player, BlockPos pos,
                                    Direction facing) {
        if (level == null || player == null) return false;

        if (!player.isCrouching()) {
            return false;
        }

        if (!level.isClientSide) {
            Block nodeBlock;

            try {
                nodeBlock = AuraBlocks.AURA_NODE.get();
            } catch (Exception e) {
                return false;
            }

            AspectList held = getAspects().copy();
            NodeModifier heldMod = getNodeModifier();
            NodeType heldType = getNodeType();
            String heldId = getId();

            level.removeBlock(pos, false);
            level.setBlockAndUpdate(pos, nodeBlock.defaultBlockState());

            if (level.getBlockEntity(pos) instanceof AuraNodeTile node) {
                node.setAspects(held);
                node.setNodeModifier(heldMod);
                node.setNodeType(heldType);
                node.setId(heldId);
                node.syncTile(true);
            }

            level.sendBlockUpdated(pos, getBlockState(), level.getBlockState(pos), 3);

            try {
                level.levelEvent(2001, pos,
                        Block.getId(AuraBlocks.JAR_NODE.get().defaultBlockState()));
            } catch (Exception ignored) {
            }
        }

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F,
                0.9F + level.random.nextFloat() * 0.2F);

        player.stopUsingItem();
        return true;
    }

    @Override
    public void onUsingWandTick(ItemStack wand, Player player, int count) {
        if (level == null || player == null) return;

        Aspect drained = NodeTapHelper.tap(level, worldPosition, getAspects(),
                this::takeFromContainer,
                this::markDirtyAndSync, wand, player, count);

        if (drained != null) {
            rememberDrain(player, drained);
        }
    }

    @Override
    public void onWandStoppedUsing(ItemStack wand, Level level, Player player, int count) {
        clearDrain();
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);

        this.aspects.writeToNBT(tag);
        tag.putString("nodeId", this.id);
        tag.putLong("animate", this.animate);

        ListTag tlist = new ListTag();

        for (Aspect aspect : this.aspectsBase.getAspects()) {
            if (aspect == null) continue;

            CompoundTag f = new CompoundTag();
            f.putString("key", aspect.getTag());
            f.putInt("amount", this.aspectsBase.getAmount(aspect));
            tlist.add(f);
        }

        tag.put("AspectsBase", tlist);

        NodeType type = getNodeType();
        NodeModifier mod = getNodeModifier();

        tag.putByte("type", (byte) (type != null ? type.ordinal() : 0));
        tag.putByte("modifier", (byte) (mod != null ? mod.ordinal() : -1));

        tag.putInt("drainId", this.drainEntityId);
        tag.putString("drainAspect", this.drainAspectTag != null ? this.drainAspectTag : "");
        tag.putLong("drainTime", this.drainTime);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        if (level == null || !level.isClientSide) {
            this.drainEntityId = -1;
            this.drainAspectTag = "";
            this.drainTime = -100L;
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);

        long prevDrain = this.drainTime;

        try {
            loadJar(tag);
        } catch (Exception ignored) {
        }

        try {
            if (level != null && level.isClientSide && tag.contains("drainTime")) {
                long incoming = tag.getLong("drainTime");

                if (incoming > prevDrain && incoming > 0L) {
                    this.drainSeenTick = level.getGameTime();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void loadJar(CompoundTag tag) {
        this.aspects.readFromNBT(tag);
        this.id = tag.getString("nodeId");

        if (tag.contains("animate")) {
            this.animate = tag.getLong("animate");
        }

        AspectList al = new AspectList();

        if (tag.contains("AspectsBase", Tag.TAG_LIST)) {
            ListTag tlist = tag.getList("AspectsBase", Tag.TAG_COMPOUND);

            for (int j = 0; j < tlist.size(); j++) {
                CompoundTag rs = tlist.getCompound(j);

                if (rs.contains("key")) {
                    Aspect a = Aspect.getAspect(rs.getString("key"));

                    if (a != null) {
                        al.add(a, rs.getInt("amount"));
                    }
                }
            }
        }

        short oldBase = tag.getShort("nodeVisBase");

        if (oldBase > 0 && al.size() == 0) {
            this.aspectsBase = new AspectList();

            for (Aspect a : this.aspects.getAspects()) {
                if (a != null) {
                    this.aspectsBase.merge(a, oldBase);
                }
            }
        } else {
            this.aspectsBase = al.copy();
        }

        NodeType[] types = NodeType.values();
        int typeOrd = tag.getByte("type");

        if (typeOrd < 0 || typeOrd >= types.length) {
            typeOrd = 0;
        }

        setNodeType(types[typeOrd]);

        NodeModifier[] modifiers = NodeModifier.values();
        int modOrd = tag.getByte("modifier");

        setNodeModifier(modOrd >= 0 && modOrd < modifiers.length ? modifiers[modOrd] : null);

        this.drainEntityId = tag.contains("drainId") ? tag.getInt("drainId") : -1;
        this.drainAspectTag = tag.contains("drainAspect") ? tag.getString("drainAspect") : "";
        this.drainTime = tag.contains("drainTime") ? tag.getLong("drainTime") : -100L;
    }
}