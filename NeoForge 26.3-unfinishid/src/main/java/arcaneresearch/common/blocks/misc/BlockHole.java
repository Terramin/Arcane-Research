package arcaneresearch.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.tiles.misc.TileHole;
import arcaneresearch.init.ModBlockEntities;

import javax.annotation.Nullable;

public class BlockHole extends BaseEntityBlock {

    public BlockHole(BlockBehaviour.Properties props) {
        super(props
                .strength(-1.0f, 6000000.0f)
                .sound(SoundType.WOOL)
                .lightLevel(state -> 11)
                .noLootTable()
                .noOcclusion()
                .pushReaction(PushReaction.IMMOVEABLE));
    }

    public static boolean createHole(Level level, BlockPos pos, BlockState originalBlock,
                                     short duration, byte depth, @Nullable net.minecraft.core.Direction direction) {
        if (level.isClientSide()) return false;

        if (originalBlock.isAir()) return false;

        if (originalBlock.getDestroySpeed(level, pos) < 0) return false;

        net.minecraft.nbt.CompoundTag tileData = null;
        BlockEntity existingTile = level.getBlockEntity(pos);
        if (existingTile != null) {
            // NEOFORGE_PORT: saveWithoutMetadata() now takes registries (returns CompoundTag as before).
            tileData = existingTile.saveWithoutMetadata(level.registryAccess());
        }

        Block holeBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("arcaneresearch", "hole"));

        if (holeBlock == null) return false;

        level.setBlock(pos, holeBlock.defaultBlockState(), 3);

        if (level.getBlockEntity(pos) instanceof TileHole holeTile) {
            holeTile.configure(originalBlock, duration, depth, direction);
            if (tileData != null) {
                holeTile.setTileEntityCompound(tileData);
            }
            return true;
        }

        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        return Shapes.empty();
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {

        return Shapes.empty();
    }

    // NEOFORGE_PORT: 26.x propagatesSkylightDown takes only the state.
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {

        return ItemStack.EMPTY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileHole(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, ModBlockEntities.HOLE.get(), TileHole::clientTick);
        } else {
            return createTickerHelper(type, ModBlockEntities.HOLE.get(), TileHole::serverTick);
        }
    }
}
