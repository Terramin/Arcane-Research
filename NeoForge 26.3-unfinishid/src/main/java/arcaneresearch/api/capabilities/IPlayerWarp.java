package arcaneresearch.api.capabilities;

import javax.annotation.Nonnull;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

// NEOFORGE_PORT: same INBTSerializable -> ValueIOSerializable migration as IPlayerKnowledge.
public interface IPlayerWarp extends ValueIOSerializable {
    // NEOFORGE_PORT: kept for NBT clone/sync paths (impls in common.lib.capabilities).
    net.minecraft.nbt.CompoundTag serializeNBT();

    void deserializeNBT(net.minecraft.nbt.CompoundTag tag);


    void clear();

    int get(@Nonnull EnumWarpType type);

    void set(@Nonnull EnumWarpType type, int amount);

    int add(@Nonnull EnumWarpType type, int amount);

    void reduce(@Nonnull EnumWarpType type, int amount);

    int getTotalWarp();

    int getPermanentWarp();

    boolean shouldTick();

    int getCounter();

    void setCounter(int count);

    void sync(ServerPlayer player);

    enum EnumWarpType {

        PERMANENT,

        NORMAL,

        TEMPORARY
    }
}
