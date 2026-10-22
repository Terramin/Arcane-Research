package thaumictinker.camo;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

 



 
public class CamoBlockEntity extends BlockEntity {

    public static final ModelProperty<BlockState> CAMO = new ModelProperty<>();

    private BlockState camo;

    public CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public BlockState getCamo() {
        return camo;
    }

    public void setCamo(BlockState camo) {
        this.camo = camo;
        if (level != null && !level.isClientSide) {
            requestModelDataUpdate();
            sync();
        } else {
            setChanged();
        }
    }

    @Override
    public ModelData getModelData() {
        return camo == null ? ModelData.EMPTY : ModelData.builder().with(CAMO, camo).build();
    }

    protected void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 8);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        camo = tag.contains("camo") ? readCamoState(tag.getCompound("camo")) : null;
    }

     



 
    private static BlockState readCamoState(CompoundTag payload) {
        String name = payload.getString("Name");
        if (name.isEmpty()) {
            return null;
        }
        Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(new net.minecraft.resources.ResourceLocation(name));
        BlockState state = block.defaultBlockState();
        if (payload.contains("Properties")) {
            CompoundTag properties = payload.getCompound("Properties");
            for (String key : properties.getAllKeys()) {
                state = applyStateValue(state, state.getBlock().getStateDefinition().getProperty(key), properties.getString(key));
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState applyStateValue(BlockState state, net.minecraft.world.level.block.state.properties.Property<T> property, String value) {
        if (property == null) {
            return state;
        }
        return property.getValue(value).map(v -> state.setValue(property, v)).orElse(state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (camo != null) {
            tag.put("camo", net.minecraft.nbt.NbtUtils.writeBlockState(camo));
        }
    }
}
