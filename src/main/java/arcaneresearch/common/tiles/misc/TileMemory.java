package arcaneresearch.common.tiles.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileMemory extends BlockEntity {

    protected BlockState oldBlock = Blocks.AIR.defaultBlockState();
    protected CompoundTag tileEntityCompound = null;

    public TileMemory(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileMemory(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockState originalBlock) {
        super(type, pos, state);
        this.oldBlock = originalBlock != null ? originalBlock : Blocks.AIR.defaultBlockState();
    }

    public BlockState getOldBlock() {
        return oldBlock;
    }

    public void setOldBlock(BlockState state) {
        BlockState next = state != null ? state : Blocks.AIR.defaultBlockState();
        if (next != oldBlock) {
            this.oldBlock = next;
            setChanged();
        }
    }

    public CompoundTag getTileEntityCompound() {
        return tileEntityCompound;
    }

    public void setTileEntityCompound(CompoundTag tag) {
        if (this.tileEntityCompound != tag) {
            this.tileEntityCompound = tag;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("oldBlock", NbtUtils.writeBlockState(oldBlock));
        if (tileEntityCompound != null) {
            tag.put("tileData", tileEntityCompound);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("oldBlock")) {
            oldBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("oldBlock"));
        } else {
            oldBlock = Blocks.AIR.defaultBlockState();
        }
        tileEntityCompound = tag.contains("tileData") ? tag.getCompound("tileData") : null;
    }
}