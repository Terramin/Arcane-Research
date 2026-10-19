package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileJarNode extends TileArcaneresearch implements INode, IWandable {
    private AspectList aspects = new AspectList();
    private AspectList aspectsBase = new AspectList();

    private NodeType nodeType = NodeType.NORMAL;
    private NodeModifier nodeModifier = null;

    private String id = "";

    private transient net.minecraft.world.entity.Entity drainTarget;
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
        tile.regenTick++;
        if (tile.regenTick < 400) {
            return;
        }
        tile.regenTick = 0;
        try {
            AspectList base = tile.getAspectsBase();
            if (base.size() <= 0) {
                return;
            }
            java.util.ArrayList<Aspect> below = new java.util.ArrayList<>();
            for (Aspect a : base.getAspects()) {
                if (a != null && tile.getAspects().getAmount(a) < base.getAmount(a)) {
                    below.add(a);
                }
            }
            if (below.isEmpty()) {
                return;
            }
            Aspect pick = below.get(level.getRandom().nextInt(below.size()));
            tile.addToContainer(pick, 1);
            tile.markDirtyAndSync();
        } catch (Exception ignored) {
        }
    }

    public void rememberDrain(net.minecraft.world.entity.Entity target, Aspect aspect) {
        this.drainTarget = target;
        this.drainAspect = aspect;
        if (level != null) {
            this.lastTapGameTime = level.getLevelData().getGameTime();
        }

        if (level != null && !level.isClientSide() && target != null) {
            this.drainEntityId = target.getId();
            this.drainAspectTag = aspect != null ? aspect.getTag() : "";
            this.drainTime = level.getLevelData().getGameTime();
            syncTile(false);
        }
    }

    public void clearDrain() {
        this.drainTarget = null;
        this.drainAspect = null;
        if (level != null && !level.isClientSide()) {
            this.drainEntityId = -1;
            this.drainAspectTag = "";
            syncTile(false);
        }
    }

    public net.minecraft.world.entity.Entity getDrainTarget() {

        if (this.drainTarget == null && this.drainEntityId >= 0 && level != null
                && level.isClientSide()) {
            try {
                net.minecraft.world.entity.Entity e =
                        level.getEntity(this.drainEntityId);
                if (e != null) {
                    return e;
                }
            } catch (Exception ignored) {
            }
        }
        return this.drainTarget;
    }

    public Aspect getDrainAspect() {
        if (this.drainAspect == null && this.drainAspectTag != null
                && !this.drainAspectTag.isEmpty()) {
            Aspect a = Aspect.getAspect(this.drainAspectTag);
            if (a != null) {
                return a;
            }
        }
        return this.drainAspect;
    }

    public long getLastTapGameTime() {
        if (level != null && level.isClientSide()) {
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
        int out = 0;
        if (this.aspects.getAmount(aspect) + amount > this.aspectsBase.getAmount(aspect)) {
            out = this.aspects.getAmount(aspect) + amount - this.aspectsBase.getAmount(aspect);
        }
        this.aspects.add(aspect, amount - out);
        setChanged();
        return out;
    }

    @Override
    public boolean takeFromContainer(Aspect aspect, int amount) {
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
        return this.aspects.getAmount(tag) >= amount;
    }

    @Override
    @Deprecated
    public boolean doesContainerContain(AspectList ot) {
        for (Aspect tt : ot.getAspects()) {
            if (this.aspects.getAmount(tt) < ot.getAmount(tt)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int containerContains(Aspect tag) {
        return this.aspects.getAmount(tag);
    }

    @Override
    public NodeType getNodeType() {
        return this.nodeType;
    }

    @Override
    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
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
        return this.aspectsBase.getAmount(aspect);
    }

    @Override
    public void setNodeVisBase(Aspect aspect, short nodeVisBase) {
        if (this.aspectsBase.getAmount(aspect) < nodeVisBase) {
            this.aspectsBase.merge(aspect, nodeVisBase);
        } else {
            this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean onWandRightClick(Level level, ItemStack wandstack, Player player, BlockPos pos,
                                    Direction facing) {

        if (player != null && !player.isCrouching()) {
            return false;
        }
        if (!level.isClientSide()) {
            net.minecraft.world.level.block.Block nodeBlock;
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
            // TC4: частицы разбитого стекла (2001) при высвобождении ноды
            try {
                level.levelEvent(2001, pos,
                        net.minecraft.world.level.block.Block.getId(
                                AuraBlocks.JAR_NODE.get().defaultBlockState()));
            } catch (Exception ignored) {
            }
        }

        level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        player.stopUsingItem();
        return true;
    }

    @Override
    public void onUsingWandTick(ItemStack wand, Player player, int count) {

        if (level != null) {
            Aspect drained = NodeTapHelper.tap(level, worldPosition, getAspects(),
                    (aspect, amount) -> takeFromContainer(aspect, amount),
                    this::markDirtyAndSync, wand, player, count);
            if (drained != null) {
                rememberDrain(player, drained);
            }
        }
    }

    @Override
    public void onWandStoppedUsing(ItemStack wand, Level level, Player player, int count) {
        clearDrain();
    }

    @Override
    protected void writeSyncNBT(ValueOutput tag) {
        super.writeSyncNBT(tag);
        this.aspects.writeToNBT(tag);
        tag.putString("nodeId", this.id);
        tag.putLong("animate", this.animate);
        java.util.List<CompoundTag> baseList = new java.util.ArrayList<>();
        for (Aspect aspect : this.aspectsBase.getAspects()) {
            if (aspect != null) {
                CompoundTag f = new CompoundTag();
                f.putString("key", aspect.getTag());
                f.putInt("amount", this.aspectsBase.getAmount(aspect));
                baseList.add(f);
            }
        }
        writeTagList(tag, "AspectsBase", baseList);
        tag.putByte("type", (byte) getNodeType().ordinal());
        tag.putByte("modifier", getNodeModifier() == null ? (byte) -1 : (byte) getNodeModifier().ordinal());

        tag.putInt("drainId", this.drainEntityId);
        tag.putString("drainAspect", this.drainAspectTag != null ? this.drainAspectTag : "");
        tag.putLong("drainTime", this.drainTime);
    }

    @Override
    public void loadAdditional(ValueInput tag) {

        super.loadAdditional(tag);
        if (level == null || !level.isClientSide()) {
            this.drainEntityId = -1;
            this.drainAspectTag = "";
            this.drainTime = -100L;
        }
    }

    @Override
    protected void readSyncNBT(ValueInput tag) {
        // NEOFORGE_PORT: Forge body; CompoundTag getters -> ValueInput Or-getters.
        super.readSyncNBT(tag);
        long prevDrain = this.drainTime;
        try {
            loadJar(tag);
        } catch (Exception ignored) {
        }

        try {
            if (level != null && level.isClientSide()) {
                long incoming = this.drainTime;
                if (incoming > prevDrain && incoming > 0L) {
                    this.drainSeenTick = level.getLevelData().getGameTime();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void loadJar(ValueInput tag) {
        // NEOFORGE_PORT: Forge body; AspectsBase list via readTagList helper.
        this.aspects.readFromNBT(tag);
        this.id = tag.getStringOr("nodeId", "");
        this.animate = tag.getLongOr("animate", this.animate);
        AspectList al = new AspectList();
        for (CompoundTag rs : readTagList(tag, "AspectsBase")) {
            if (rs.contains("key")) {
                Aspect a = Aspect.getAspect(rs.getStringOr("key", ""));
                if (a != null) {
                    al.add(a, rs.getIntOr("amount", 0));
                }
            }
        }
        // NEOFORGE_PORT: ValueInput has no getShortOr; read via Codec.SHORT (cf. TileAlembic).
        short oldBase = tag.read("nodeVisBase", com.mojang.serialization.Codec.SHORT).orElse((short) 0);
        this.aspectsBase = new AspectList();
        if (oldBase > 0 && al.size() == 0) {
            for (Aspect a : this.aspects.getAspects()) {
                this.aspectsBase.merge(a, oldBase);
            }
        } else {
            this.aspectsBase = al.copy();
        }
        setNodeType(NodeType.values()[tag.getByteOr("type", (byte) 0) % NodeType.values().length]);
        byte mod = tag.getByteOr("modifier", (byte) 0);
        setNodeModifier(mod >= 0 && mod < NodeModifier.values().length ? NodeModifier.values()[mod] : null);

        this.drainEntityId = tag.getIntOr("drainId", -1);
        this.drainAspectTag = tag.getStringOr("drainAspect", "");
        this.drainTime = tag.getLongOr("drainTime", -100L);
    }
}
