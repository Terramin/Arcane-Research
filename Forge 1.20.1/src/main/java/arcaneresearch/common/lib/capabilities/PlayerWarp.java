package arcaneresearch.common.lib.capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerWarp;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
public class PlayerWarp {
    public static final ResourceLocation ID = new ResourceLocation(Arcaneresearch.MODID, "warp");
    public static class DefaultImpl implements IPlayerWarp {
        private int permanentWarp = 0;
        private int normalWarp = 0;
        private int temporaryWarp = 0;
        private int counter = 0;
        @Override
        public void clear() {
            permanentWarp = 0;
            normalWarp = 0;
            temporaryWarp = 0;
            counter = 0;
        }
        @Override
        public int get(@Nonnull EnumWarpType type) {
            return switch (type) {
                case PERMANENT -> permanentWarp;
                case NORMAL -> normalWarp;
                case TEMPORARY -> temporaryWarp;
            };
        }
        @Override
        public void set(@Nonnull EnumWarpType type, int amount) {
            amount = Math.min(500, Math.max(0, amount));
            switch (type) {
                case PERMANENT -> permanentWarp = amount;
                case NORMAL -> normalWarp = amount;
                case TEMPORARY -> temporaryWarp = amount;
            }
        }
        @Override
        public int add(@Nonnull EnumWarpType type, int amount) {
            int current = get(type);
            int newValue = Math.min(500, Math.max(0, current + amount));
            set(type, newValue);
            return newValue;
        }
        @Override
        public void reduce(@Nonnull EnumWarpType type, int amount) {
            set(type, get(type) - amount);
        }
        @Override
        public int getTotalWarp() {
            return permanentWarp + normalWarp + temporaryWarp;
        }
        @Override
        public int getPermanentWarp() {
            return permanentWarp + normalWarp;
        }
        @Override
        public boolean shouldTick() {
            return temporaryWarp > 0;
        }
        @Override
        public int getCounter() {
            return counter;
        }
        @Override
        public void setCounter(int count) {
            this.counter = count;
        }
        @Override
        public void sync(@Nonnull ServerPlayer player) {
            arcaneresearch.common.lib.network.PacketHandler.sendToPlayer(
                    new arcaneresearch.common.lib.network.playerdata.PacketSyncWarp(player),
                    player
            );
        }
        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("perm", permanentWarp);
            tag.putInt("norm", normalWarp);
            tag.putInt("temp", temporaryWarp);
            tag.putInt("counter", counter);
            return tag;
        }
        @Override
        public void deserializeNBT(CompoundTag tag) {
            if (tag == null) {
                return;
            }
            if (!tag.contains("perm") && !tag.contains("norm") && !tag.contains("temp")
                    && tag.contains("warp", 11)) {
                int[] old = tag.getIntArray("warp");
                permanentWarp = old.length > 0 ? Math.min(500, Math.max(0, old[0])) : 0;
                normalWarp = old.length > 1 ? Math.min(500, Math.max(0, old[1])) : 0;
                temporaryWarp = old.length > 2 ? Math.min(500, Math.max(0, old[2])) : 0;
                counter = tag.getInt("counter");
                return;
            }
            permanentWarp = tag.getInt("perm");
            normalWarp = tag.getInt("norm");
            temporaryWarp = tag.getInt("temp");
            counter = tag.getInt("counter");
        }
    }
    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        public static final ResourceLocation NAME = ID;
        private final DefaultImpl warp = new DefaultImpl();
        private final LazyOptional<IPlayerWarp> optional = LazyOptional.of(() -> warp);
        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == ArcaneresearchCapabilities.WARP) {
                return optional.cast();
            }
            return LazyOptional.empty();
        }
        @Override
        public CompoundTag serializeNBT() {
            return warp.serializeNBT();
        }
        @Override
        public void deserializeNBT(CompoundTag nbt) {
            warp.deserializeNBT(nbt);
        }
        public void invalidate() {
            optional.invalidate();
        }
    }
}
