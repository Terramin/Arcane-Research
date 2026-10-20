package arcaneresearch.common.lib.utils;

import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

// NEOFORGE_PORT (26.3): 1.20.1 ItemStack.save/of needed no registries. ItemStack.CODEC with
// plain NbtOps fails on stacks carrying dynamic-registry components (enchantments, trims...),
// so such stacks silently became empty. These ops resolve registries on either side:
// the running server, else the client level (wired by RegistryNbtClient).
public final class RegistryNbt {
    /** Set on the physical client only; returns null while no level is loaded. */
    public static volatile Supplier<HolderLookup.Provider> clientLookup = () -> null;

    private RegistryNbt() {
    }

    public static DynamicOps<Tag> ops() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        }
        HolderLookup.Provider client = clientLookup.get();
        return client != null ? client.createSerializationContext(NbtOps.INSTANCE) : NbtOps.INSTANCE;
    }

    public static Tag saveStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundTag();
        }
        return ItemStack.CODEC.encodeStart(ops(), stack).result().orElseGet(CompoundTag::new);
    }

    public static ItemStack loadStack(Tag tag) {
        if (tag == null) {
            return ItemStack.EMPTY;
        }
        return ItemStack.OPTIONAL_CODEC.parse(ops(), tag).result().orElse(ItemStack.EMPTY);
    }
}
