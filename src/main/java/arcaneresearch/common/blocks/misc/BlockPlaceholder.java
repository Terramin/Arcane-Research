package arcaneresearch.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import arcaneresearch.common.blocks.devices.BlockInfernalFurnace;
import arcaneresearch.init.ModBlocks;

import java.util.Collections;
import java.util.List;

public class BlockPlaceholder extends Block {

    private final PlaceholderType type;

    public BlockPlaceholder(BlockBehaviour.Properties props, PlaceholderType type) {
        super(props
                .mapColor(MapColor.STONE)
                .strength(2.5f, 2.0f)
                .sound(SoundType.STONE)
                .noOcclusion());
        this.type = type;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {

        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {

        return PushReaction.IMMOVEABLE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {

        return switch (type) {
            case NETHERBRICK -> Collections.singletonList(new ItemStack(Blocks.NETHER_BRICKS));
            case OBSIDIAN -> Collections.singletonList(new ItemStack(Blocks.OBSIDIAN));
            case BARS -> Collections.singletonList(new ItemStack(Blocks.IRON_BARS));
            case ANVIL -> Collections.singletonList(new ItemStack(Blocks.ANVIL));
            case CAULDRON -> Collections.singletonList(new ItemStack(Blocks.CAULDRON));
            case TABLE -> Collections.singletonList(new ItemStack(ModBlocks.TABLE_STONE.get()));
        };
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader level, BlockPos pos, BlockState state, boolean includeData) {

        return switch (type) {
            case BARS, ANVIL, CAULDRON, TABLE -> new ItemStack(ModBlocks.GOLEM_BUILDER.get());
            
            
            case NETHERBRICK -> new ItemStack(Blocks.NETHER_BRICKS);
            case OBSIDIAN -> new ItemStack(Blocks.OBSIDIAN);
        };
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {

        if (type == PlaceholderType.CAULDRON) {
            return 13;
        }
        return super.getLightEmission(state, level, pos);
    }

    @Override
    
    

    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (type == PlaceholderType.NETHERBRICK || type == PlaceholderType.OBSIDIAN) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        for (int a = -1; a <= 1; ++a) {
            for (int b = -1; b <= 1; ++b) {
                for (int c = -1; c <= 1; ++c) {
                    BlockPos p = pos.offset(a, b, c);
                    if (level.getBlockEntity(p) instanceof arcaneresearch.common.tiles.crafting.TileGolemBuilder builder) {
                        ((ServerPlayer) player).openMenu(builder, buf -> buf.writeBlockPos(p));
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        
        
        
        
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        if (!state.is(level.getBlockState(pos).getBlock()) && !level.isClientSide()) {
            if ((type == PlaceholderType.NETHERBRICK || type == PlaceholderType.OBSIDIAN)
                    && !BlockInfernalFurnace.ignore) {

                outer:
                for (int a = -1; a <= 1; ++a) {
                    for (int b = -1; b <= 1; ++b) {
                        for (int c = -1; c <= 1; ++c) {
                            BlockPos p = pos.offset(a, b, c);
                            BlockState s = level.getBlockState(p);
                            if (s.is(ModBlocks.INFERNAL_FURNACE.get())) {
                                BlockInfernalFurnace.destroyFurnace(level, p, s, pos);
                                break outer;
                            }
                        }
                    }
                }
            } else if (type != PlaceholderType.NETHERBRICK && type != PlaceholderType.OBSIDIAN
                    && !arcaneresearch.common.blocks.crafting.BlockGolemBuilder.ignore) {

                outer:
                for (int a = -1; a <= 1; ++a) {
                    for (int b = -1; b <= 1; ++b) {
                        for (int c = -1; c <= 1; ++c) {
                            BlockPos p = pos.offset(a, b, c);
                            BlockState s = level.getBlockState(p);
                            if (s.is(ModBlocks.GOLEM_BUILDER.get())) {
                                arcaneresearch.common.blocks.crafting.BlockGolemBuilder.destroyPress(level, p, pos);
                                break outer;
                            }
                        }
                    }
                }
            }
        }

    }

    public enum PlaceholderType {
        NETHERBRICK,
        OBSIDIAN,
        BARS,
        ANVIL,
        CAULDRON,
        TABLE
    }
}
