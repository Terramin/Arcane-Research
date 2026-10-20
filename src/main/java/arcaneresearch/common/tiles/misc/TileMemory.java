package arcaneresearch.common.tiles.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("oldBlock", NbtUtils.writeBlockState(oldBlock));
        if (tileEntityCompound != null) {
            tag.put("tileData", tileEntityCompound);
        }
    }
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("oldBlock")) {
            oldBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("oldBlock"));
        } else {
            oldBlock = Blocks.AIR.defaultBlockState();
        }
        if (tag.contains("tileData")) {
            tileEntityCompound = tag.getCompound("tileData");
        }
    }
}
