package arcaneresearch.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TileMemory extends BlockEntity {

    protected BlockState oldBlock = Blocks.AIR.defaultBlockState();

    protected CompoundTag tileEntityCompound = null;

    public TileMemory(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileMemory(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockState originalBlock) {
        super(type, pos, state);
        this.oldBlock = originalBlock;
    }

    public BlockState getOldBlock() {
        return oldBlock;
    }

    public void setOldBlock(BlockState state) {
        this.oldBlock = state;
        setChanged();
    }

    public CompoundTag getTileEntityCompound() {
        return tileEntityCompound;
    }

    public void setTileEntityCompound(CompoundTag tag) {
        this.tileEntityCompound = tag;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        
        tag.store("oldBlock", CompoundTag.CODEC, NbtUtils.writeBlockState(oldBlock));
        if (tileEntityCompound != null) {
            tag.store("tileData", CompoundTag.CODEC, tileEntityCompound);
        }
    }

    @Override
    public void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        
        oldBlock = tag.read("oldBlock", CompoundTag.CODEC)
                .map(nbt -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK, nbt))
                .orElse(Blocks.AIR.defaultBlockState());
        tileEntityCompound = tag.read("tileData", CompoundTag.CODEC).orElse(null);
    }
}
