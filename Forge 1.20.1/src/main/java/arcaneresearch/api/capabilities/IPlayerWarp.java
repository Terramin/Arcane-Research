package arcaneresearch.api.capabilities;
import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.INBTSerializable;
public interface IPlayerWarp extends INBTSerializable<CompoundTag> {
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
