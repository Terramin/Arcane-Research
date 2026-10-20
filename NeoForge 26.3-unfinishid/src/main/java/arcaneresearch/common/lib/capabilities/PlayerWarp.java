package arcaneresearch.common.lib.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerWarp;

import javax.annotation.Nonnull;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 common/lib/capabilities/PlayerWarp.java.
// DefaultImpl logic preserved VERBATIM (clamp 0..500, legacy int[] "warp" migration, NBT keys).
// REQUIRED changes: removed inner Provider class (Forge ICapabilitySerializable + LazyOptional);
//   storage now via AttachmentType in ArcaneresearchCapabilities (common).
public class PlayerWarp {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "warp");

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
                    && tag.contains("warp")) {
                int[] old = tag.getIntArray("warp").orElse(new int[0]);
                permanentWarp = old.length > 0 ? Math.min(500, Math.max(0, old[0])) : 0;
                normalWarp = old.length > 1 ? Math.min(500, Math.max(0, old[1])) : 0;
                temporaryWarp = old.length > 2 ? Math.min(500, Math.max(0, old[2])) : 0;
                counter = tag.getIntOr("counter", 0);
                return;
            }
            permanentWarp = tag.getIntOr("perm", 0);
            normalWarp = tag.getIntOr("norm", 0);
            temporaryWarp = tag.getIntOr("temp", 0);
            counter = tag.getIntOr("counter", 0);
        }

        // NEOFORGE_PORT: ValueIOSerializable persistence (see PlayerKnowledge.DefaultImpl javadoc).
        // Keys identical to NBT ("perm"/"norm"/"temp"/"counter"), incl. legacy int[] "warp" path.
        @Override
        public void serialize(ValueOutput output) {
            output.putInt("perm", permanentWarp);
            output.putInt("norm", normalWarp);
            output.putInt("temp", temporaryWarp);
            output.putInt("counter", counter);
        }

        @Override
        public void deserialize(ValueInput input) {
            int[] old = input.getIntArray("warp").orElse(new int[0]);
            if (old.length > 0) {
                permanentWarp = Math.min(500, Math.max(0, old[0]));
                normalWarp = old.length > 1 ? Math.min(500, Math.max(0, old[1])) : 0;
                temporaryWarp = old.length > 2 ? Math.min(500, Math.max(0, old[2])) : 0;
                counter = input.getIntOr("counter", 0);
                return;
            }
            permanentWarp = input.getIntOr("perm", 0);
            normalWarp = input.getIntOr("norm", 0);
            temporaryWarp = input.getIntOr("temp", 0);
            counter = input.getIntOr("counter", 0);
        }
    }
}
