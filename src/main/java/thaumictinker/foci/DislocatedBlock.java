package thaumictinker.foci;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

 



 
public record DislocatedBlock(BlockState state, CompoundTag tile) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("state", net.minecraft.nbt.NbtUtils.writeBlockState(state));
        tag.put("tile", tile);
        return tag;
    }

    public static DislocatedBlock load(CompoundTag tag) {
        BlockState state = readState(tag.getCompound("state"));
        if (state == null) {
            return null;
        }
        return new DislocatedBlock(state, tag.getCompound("tile").copy());
    }

    private static BlockState readState(CompoundTag payload) {
        String name = payload.getString("Name");
        if (name.isEmpty()) {
            return null;
        }
        net.minecraft.world.level.block.Block block =
                net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(new net.minecraft.resources.ResourceLocation(name));
        BlockState state = block.defaultBlockState();
        if (payload.contains("Properties")) {
            CompoundTag properties = payload.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                state = applyValue(state, state.getBlock().getStateDefinition().getProperty(key), properties.getString(key));
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState applyValue(BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property, String value) {
        if (property == null) {
            return state;
        }
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }
}
