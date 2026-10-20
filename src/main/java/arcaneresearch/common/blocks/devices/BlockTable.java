package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.common.blocks.BlockTC;
import arcaneresearch.common.blocks.crafting.BlockResearchTable;
import arcaneresearch.common.tiles.crafting.TileResearchTable;
import arcaneresearch.init.ModBlocks;

public class BlockTable extends BlockTC {

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(1.0, 0.0, 1.0, 4.0, 12.0, 4.0),
            Block.box(12.0, 0.0, 1.0, 15.0, 12.0, 4.0),
            Block.box(1.0, 0.0, 12.0, 4.0, 12.0, 15.0),
            Block.box(12.0, 0.0, 12.0, 15.0, 12.0, 15.0),
            Block.box(3.0, 3.0, 3.0, 13.0, 5.0, 13.0)
    );
    private final boolean wooden;

    public BlockTable(Properties properties) {
        super(properties);
        this.wooden = false;
    }

    private BlockTable(Properties properties, boolean wooden) {
        super(properties);
        this.wooden = wooden;
    }

    public static BlockTable createWooden(BlockBehaviour.Properties props) {
        return new BlockTable(props
                .mapColor(MapColor.WOOD)
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .noOcclusion(), true);
    }

    public static BlockTable createStone(BlockBehaviour.Properties props) {
        return new BlockTable(props
                .mapColor(MapColor.STONE)
                .strength(2.0f)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);
        if (wooden && held.getItem() instanceof IScribeTools) {
            Direction facing = player.getDirection();
            BlockState rt = ModBlocks.RESEARCH_TABLE.get().defaultBlockState()
                    .setValue(BlockResearchTable.FACING, facing);
            level.setBlock(pos, rt, 3);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileResearchTable tile) {
                tile.setItem(TileResearchTable.SLOT_SCRIBING_TOOLS, held.copy());
                tile.setChanged();
            }
            player.setItemInHand(hand, ItemStack.EMPTY);
            player.getInventory().setChanged();

            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(
                    new net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent(
                            player, new ItemStack(ModBlocks.RESEARCH_TABLE.get()), null));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
