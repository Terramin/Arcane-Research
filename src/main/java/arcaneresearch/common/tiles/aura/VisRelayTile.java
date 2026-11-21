package arcaneresearch.common.tiles.aura;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.wands.IWandable;
import arcaneresearch.common.lib.aura.AuraFX;
import arcaneresearch.common.lib.aura.VisNetHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * Thaumcraft 4 style Vis Relay ("Vis Channels"). A non-source vis network
 * node that attaches to the energized aura node tree and passes centi-vis
 * (CV) up to its parent. Attunement with a primal crystal only controls which
 * relays may connect to each other - it does not restrict the vis flowing
 * through it.
 */
public class VisRelayTile extends TileVisNode implements IWandable {
    /** TC4 shard order: AIR, FIRE, WATER, EARTH, ORDER, ENTROPY. */
    public static final Aspect[] ATTUNEMENTS = new Aspect[]{
            Aspect.AIR, Aspect.FIRE, Aspect.WATER,
            Aspect.EARTH, Aspect.ORDER, Aspect.ENTROPY
    };

    /** -1 = untuned, 0..5 = attuned to the aspect at that index. */
    private byte attunement = -1;
    private int pulse = 0;

    /** Parent position mirrored to the client for drawing the power beam. */
    private BlockPos clientParentPos = null;

    public VisRelayTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VisRelayTile tile) {
        tile.tickNetwork(level);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, VisRelayTile tile) {
        if (tile.pulse > 0) tile.pulse--;
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public byte getAttunement() {
        return this.attunement;
    }

    public Aspect getAttunementAspect() {
        return attunement >= 0 && attunement < ATTUNEMENTS.length ? ATTUNEMENTS[attunement] : null;
    }

    public static byte indexFor(Aspect aspect) {
        if (aspect == null) return -1;
        for (byte i = 0; i < ATTUNEMENTS.length; i++) {
            if (ATTUNEMENTS[i] == aspect) return i;
        }
        return -1;
    }

    public static int colorFor(byte attunement) {
        return attunement >= 0 && attunement < ATTUNEMENTS.length
                ? ATTUNEMENTS[attunement].getColor()
                : 0xFFFFFF;
    }

    public int getPulse() {
        return this.pulse;
    }

    public BlockPos getClientParentPos() {
        return this.clientParentPos;
    }

    public void setAttunement(byte value, boolean reconnect) {
        this.attunement = value;
        if (reconnect) {
            removeThisNode();
            this.nodeRefresh = true;
        }
        markDirtyAndSync();
    }

    @Override
    public void removeThisNode() {
        super.removeThisNode();
        this.clientParentPos = null;
    }

    @Override
    public void setRemoved() {
        // Detach without the block update / packet sync: the block is already
        // gone. A stale WeakReference in the parent would only be cleared by GC.
        WeakReference<TileVisNode> ref = getParent();
        if (VisNetHandler.isNodeValid(ref)) {
            TileVisNode parent = ref.get();
            if (parent != null) parent.nodeRefresh = true;
        }
        getChildren().clear();
        setParent(null);
        super.setRemoved();
    }

    @Override
    public void parentChanged() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        WeakReference<TileVisNode> ref = getParent();
        BlockPos actual = VisNetHandler.isNodeValid(ref) ? ref.get().getBlockPos() : null;
        if ((actual == null) == (this.clientParentPos == null)
                && (actual == null || actual.equals(this.clientParentPos))) {
            return;
        }

        this.clientParentPos = actual;
        CompoundTag tag = new CompoundTag();
        if (actual != null) {
            tag.putInt("px", actual.getX());
            tag.putInt("py", actual.getY());
            tag.putInt("pz", actual.getZ());
        } else {
            tag.putBoolean("noParent", true);
        }
        sendMessageToClients(tag);
        markDirtyAndSync();
    }

    @Override
    public void messageFromServer(CompoundTag nbt) {
        if (nbt == null) return;
        if (nbt.getBoolean("noParent") || !nbt.contains("px")) {
            this.clientParentPos = null;
        } else {
            this.clientParentPos = new BlockPos(nbt.getInt("px"), nbt.getInt("py"), nbt.getInt("pz"));
        }
    }

    @Override
    protected void writeSyncNBT(CompoundTag tag) {
        super.writeSyncNBT(tag);
        tag.putByte("visAttunement", this.attunement);
        if (this.clientParentPos != null) {
            tag.putInt("px", this.clientParentPos.getX());
            tag.putInt("py", this.clientParentPos.getY());
            tag.putInt("pz", this.clientParentPos.getZ());
        } else {
            tag.putBoolean("noParent", true);
        }
    }

    @Override
    protected void readSyncNBT(CompoundTag tag) {
        super.readSyncNBT(tag);
        if (tag.contains("visAttunement")) this.attunement = tag.getByte("visAttunement");
        messageFromServer(tag);
    }

    /** Each relay in the chain flashes when CV passes through it. */
    @Override
    public void triggerConsumeEffect(Aspect aspect) {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;

        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, indexFor(aspect) + 1);
        if (level.getGameTime() % 5 == 0) {
            AuraFX.nodeSparkle(level, worldPosition, aspect == null ? 0xFFFFFF : aspect.getColor());
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == 1 && level != null && level.isClientSide) {
            this.pulse = 5;
            return true;
        }
        return super.triggerEvent(id, param);
    }

    @Override
    public boolean onWandRightClick(Level level, ItemStack wand, Player player, BlockPos pos, Direction facing) {
        if (level == null || level.isClientSide) return false;
        byte next = (byte) (this.attunement >= ATTUNEMENTS.length - 1 ? -1 : this.attunement + 1);
        setAttunement(next, true);
        return true;
    }

    @Override
    public void onUsingWandTick(ItemStack wand, Player player, int count) {
    }

    @Override
    public void onWandStoppedUsing(ItemStack wand, Level level, Player player, int count) {
    }
}
