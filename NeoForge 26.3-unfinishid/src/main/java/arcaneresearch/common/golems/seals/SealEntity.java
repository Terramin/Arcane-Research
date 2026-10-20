package arcaneresearch.common.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.golems.seals.*;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.tasks.TaskHandler;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketSealToClient;

public class SealEntity implements ISealEntity {

    SealPos sealPos;
    ISeal seal;
    byte priority = 0;
    byte color = 0;
    boolean locked = false;
    boolean redstone = false;
    String owner = "";
    boolean stopped = false;
    private BlockPos area = new BlockPos(1, 1, 1);

    public SealEntity() {
    }

    public SealEntity(Level level, SealPos sealPos, ISeal seal) {
        this.sealPos = sealPos;
        this.seal = seal;

        if (seal instanceof ISealConfigArea) {
            int x = (sealPos.face.getStepX() == 0) ? 3 : 1;
            int y = (sealPos.face.getStepY() == 0) ? 3 : 1;
            int z = (sealPos.face.getStepZ() == 0) ? 3 : 1;
            area = new BlockPos(x, y, z);
        }
    }

    @Override
    public void tickSealEntity(Level level) {
        if (seal == null) return;

        if (isStoppedByRedstone(level)) {
            if (!stopped) {
                for (Task task : TaskHandler.getTasks(level.dimension()).values()) {
                    if (task.getSealPos() != null && task.getSealPos().equals(sealPos)) {
                        task.setSuspended(true);
                    }
                }
            }
            stopped = true;
            return;
        }

        stopped = false;
        seal.tickSeal(level, this);
    }

    @Override
    public boolean isStoppedByRedstone(Level level) {
        if (!isRedstoneSensitive()) return false;

        BlockPos pos = getSealPos().pos;
        Direction face = getSealPos().face;

        return level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.relative(face));
    }

    @Override
    public ISeal getSeal() {
        return seal;
    }

    @Override
    public SealPos getSealPos() {
        return sealPos;
    }

    @Override
    public byte getPriority() {
        return priority;
    }

    @Override
    public void setPriority(byte priority) {
        this.priority = priority;
    }

    @Override
    public byte getColor() {
        return color;
    }

    @Override
    public void setColor(byte color) {
        this.color = color;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isRedstoneSensitive() {
        return redstone;
    }

    @Override
    public void setRedstoneSensitive(boolean redstone) {
        this.redstone = redstone;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        BlockPos pos = BlockPos.of(nbt.getLongOr("pos", 0L));
        Direction face = Direction.values()[nbt.getByteOr("face", (byte) 0)];
        sealPos = new SealPos(pos, face);

        setPriority(nbt.getByteOr("priority", (byte) 0));
        setColor(nbt.getByteOr("color", (byte) 0));
        setLocked(nbt.getBooleanOr("locked", false));
        setRedstoneSensitive(nbt.getBooleanOr("redstone", false));
        setOwner(nbt.getStringOr("owner", ""));

        try {
            ISeal template = SealHandler.getSeal(nbt.getStringOr("type", ""));
            if (template != null) {
                seal = template.getClass().getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
        }

        if (seal != null) {
            seal.readCustomNBT(nbt);

            if (seal instanceof ISealConfigArea) {
                area = BlockPos.of(nbt.getLongOr("area", 0L));
            }

            if (seal instanceof ISealConfigToggles toggleSeal) {
                for (ISealConfigToggles.SealToggle toggle : toggleSeal.getToggles()) {
                    if (nbt.contains(toggle.getKey())) {
                        // NEOFORGE_PORT: getBoolean removed -> getBooleanOr.
                        toggle.setValue(nbt.getBooleanOr(toggle.getKey(), false));
                    }
                }
            }
        }
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();

        nbt.putLong("pos", sealPos.pos.asLong());
        nbt.putByte("face", (byte) sealPos.face.ordinal());
        // NEOFORGE_PORT (26.3): null-safe like Forge 1.20.1 (seal is null for removal syncs).
        nbt.putString("type", seal != null ? seal.getKey() : "arcaneresearch:blank");
        nbt.putByte("priority", getPriority());
        nbt.putByte("color", getColor());
        nbt.putBoolean("locked", isLocked());
        nbt.putBoolean("redstone", isRedstoneSensitive());
        nbt.putString("owner", getOwner());

        if (seal != null) {
            seal.writeCustomNBT(nbt);

            if (seal instanceof ISealConfigArea) {
                nbt.putLong("area", area.asLong());
            }

            if (seal instanceof ISealConfigToggles toggleSeal) {
                for (ISealConfigToggles.SealToggle toggle : toggleSeal.getToggles()) {
                    nbt.putBoolean(toggle.getKey(), toggle.getValue());
                }
            }
        }

        return nbt;
    }

    @Override
    public void syncToClient(Level level) {
        // NEOFORGE_PORT: PacketHandler.sendToDimension(packet, dimension) ->
        // PacketDistributor.sendToPlayersInDimension(ServerLevel, payload).
        if (!level.isClientSide() && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersInDimension(
                    serverLevel, new arcaneresearch.common.lib.network.misc.PacketSealToClient(this));
        }
    }

    @Override
    public BlockPos getArea() {
        return area;
    }

    @Override
    public void setArea(BlockPos area) {
        this.area = area;
    }
}
