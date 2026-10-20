package arcaneresearch.common.blocks.basic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import arcaneresearch.common.tiles.misc.TileBanner;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class BlockBannerTC extends Block implements EntityBlock {

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape STANDING_SHAPE = Block.box(4, 0, 4, 12, 32, 12);
    private static final VoxelShape WALL_NORTH = Block.box(0, -16, 0, 16, 16, 4);
    private static final VoxelShape WALL_SOUTH = Block.box(0, -16, 12, 16, 16, 16);
    private static final VoxelShape WALL_WEST = Block.box(0, -16, 0, 4, 16, 16);
    private static final VoxelShape WALL_EAST = Block.box(12, -16, 0, 16, 16, 16);
    private final DyeColor dyeColor;

    public BlockBannerTC(BlockBehaviour.Properties props, @Nullable DyeColor color) {
        super(props
                .mapColor(color != null ? color.getMapColor() : MapColor.COLOR_RED)
                .strength(1.0f)
                .sound(SoundType.WOOD)
                .noOcclusion()
                .noCollision());
        this.dyeColor = color;
        registerDefaultState(stateDefinition.any().setValue(ROTATION, 0));
    }

    public static BlockBannerTC createWhite(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.WHITE);
    }

    public static BlockBannerTC createOrange(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.ORANGE);
    }

    public static BlockBannerTC createMagenta(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.MAGENTA);
    }

    public static BlockBannerTC createLightBlue(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.LIGHT_BLUE);
    }

    public static BlockBannerTC createYellow(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.YELLOW);
    }

    public static BlockBannerTC createLime(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.LIME);
    }

    public static BlockBannerTC createPink(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.PINK);
    }

    public static BlockBannerTC createGray(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.GRAY);
    }

    public static BlockBannerTC createLightGray(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.LIGHT_GRAY);
    }

    public static BlockBannerTC createCyan(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.CYAN);
    }

    public static BlockBannerTC createPurple(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.PURPLE);
    }

    public static BlockBannerTC createBlue(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.BLUE);
    }

    public static BlockBannerTC createBrown(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.BROWN);
    }

    public static BlockBannerTC createGreen(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.GREEN);
    }

    public static BlockBannerTC createRed(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.RED);
    }

    public static BlockBannerTC createBlack(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, DyeColor.BLACK);
    }

    public static BlockBannerTC createCrimsonCult(BlockBehaviour.Properties props) {
        return new BlockBannerTC(props, null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Nullable
    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        int rotation = (int) Math.floor((context.getRotation() + 180.0f) * 16.0f / 360.0f + 0.5) & 15;
        return defaultBlockState().setValue(ROTATION, rotation);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileBanner banner) {
            if (!banner.getWall()) {
                return STANDING_SHAPE;
            }

            int facing = banner.getBannerFacing();
            return switch (facing) {
                case 0 -> WALL_NORTH;
                case 8 -> WALL_SOUTH;
                case 12 -> WALL_WEST;
                case 4 -> WALL_EAST;
                default -> STANDING_SHAPE;
            };
        }
        return STANDING_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileBanner(pos, state);
    }

    @Override
    // NEOFORGE_PORT: Block.use(state, level, pos, player, hand, hit) removed in 26.x
    // -> useItemOn (uses held item/hand).

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileBanner banner && dyeColor != null) {
                ItemStack heldItem = player.getItemInHand(hand);

                if (player.isShiftKeyDown()) {

                    banner.setAspect(null);
                    syncBanner(level, pos, state, banner);
                    level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
                    return InteractionResult.SUCCESS;
                } else if (heldItem.getItem() instanceof IEssentiaContainerItem container) {

                    if (container.getAspects(heldItem) != null && container.getAspects(heldItem).size() > 0) {
                        Aspect aspect = container.getAspects(heldItem).getAspects()[0];
                        banner.setAspect(aspect);
                        if (!player.isCreative()) {
                            heldItem.shrink(1);
                        }
                        syncBanner(level, pos, state, banner);
                        level.playSound(null, pos, SoundEvents.WOOL_HIT, SoundSource.BLOCKS, 1.0f, 1.0f);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }

    private void syncBanner(Level level, BlockPos pos, BlockState state, TileBanner banner) {
        banner.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        ItemStack drop = new ItemStack(this);

        if (be instanceof TileBanner banner && banner.getAspect() != null) {
            String aspectTag = banner.getAspect().getTag();
            // NEOFORGE_PORT: stack root tag -> DataComponents.CUSTOM_DATA (same NBT shape).
            net.minecraft.world.item.component.CustomData.update(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA, drop,
                    tag -> tag.putString("aspect", aspectTag));
        }

        return Collections.singletonList(drop);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(this);
        BlockEntity be = level.getBlockEntity(pos);

        // NEOFORGE_PORT (26.3): no includeData guard - the 1.20.1 override always copied the aspect.
        if (be instanceof TileBanner banner && banner.getAspect() != null) {
            String aspectTag = banner.getAspect().getTag();
            // NEOFORGE_PORT: stack root tag -> DataComponents.CUSTOM_DATA (same NBT shape).
            net.minecraft.world.item.component.CustomData.update(
                    net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack,
                    tag -> tag.putString("aspect", aspectTag));
        }

        return stack;
    }
}
