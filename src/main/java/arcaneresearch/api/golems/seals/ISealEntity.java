package arcaneresearch.api.golems.seals;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public interface ISealEntity {

    void tickSealEntity(Level level);

    ISeal getSeal();

    SealPos getSealPos();

    byte getPriority();

    void setPriority(byte priority);

    void readNBT(CompoundTag nbt);

    CompoundTag writeNBT();

    void syncToClient(Level level);

    BlockPos getArea();

    void setArea(BlockPos area);

    boolean isLocked();

    void setLocked(boolean locked);

    boolean isRedstoneSensitive();

    void setRedstoneSensitive(boolean redstone);

    String getOwner();

    void setOwner(String owner);

    byte getColor();

    void setColor(byte color);

    boolean isStoppedByRedstone(Level level);
}
