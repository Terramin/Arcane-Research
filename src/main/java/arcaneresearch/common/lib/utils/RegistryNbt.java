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





public final class RegistryNbt {
      
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
