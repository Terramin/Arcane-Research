package thaumictinker.kami.gateway;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;

 


 
public record PearlLocation(ResourceKey<Level> dimension, BlockPos pos) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Dim", dimension.location().toString());
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    @Nullable
    public static PearlLocation load(CompoundTag tag) {
        if (tag == null || !tag.contains("Dim")) {
            return null;
        }
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION,
                new ResourceLocation(tag.getString("Dim")));
        return new PearlLocation(dimension, new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z")));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(dimension.location());
        buf.writeBlockPos(pos);
    }

    public static PearlLocation read(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        return new PearlLocation(ResourceKey.create(Registries.DIMENSION, id), buf.readBlockPos());
    }
}
