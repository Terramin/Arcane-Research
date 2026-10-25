package arcaneresearch.common.blocks.aura;
import arcaneresearch.common.tiles.aura.AuraTotemPoleTile;
import arcaneresearch.common.tiles.aura.AuraTotemPullTile;
import arcaneresearch.common.tiles.aura.AuraTotemPushTile;
import arcaneresearch.common.tiles.aura.AuraTotemTile;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import arcaneresearch.common.blocks.BlockTCDevice;
import arcaneresearch.common.blocks.aura.AuraBlockEntities;
import javax.annotation.Nullable;
public class AuraTotemBlock extends BlockTCDevice {
    public static final EnumProperty<TotemType> TYPE = EnumProperty.create("type", TotemType.class);
    private static final VoxelShape POLE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape FULL = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public AuraTotemBlock() {
        super(BlockBehaviour.Properties.of().strength(2.0F, 2000.0F).sound(net.minecraft.world.level.block.SoundType.STONE));
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, TotemType.PUSH));
    }
    @Override
    protected boolean hasFacing() {
        return false;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            switch (state.getValue(TYPE)) {
                case PULL:
                    return new AuraTotemPullTile(AuraBlockEntities.AURA_TOTEM_PULL.get(), pos, state);
                case POLE_OUTER:
                case POLE_INNER:
                case POLE_PURE:
                    return new AuraTotemPoleTile(AuraBlockEntities.AURA_TOTEM_POLE.get(), pos, state);
                default:
                    return new AuraTotemPushTile(AuraBlockEntities.AURA_TOTEM_PUSH.get(), pos, state);
            }
        } catch (Exception e) {
            return null;
        }
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        try {
            if (type != AuraBlockEntities.AURA_TOTEM.get()
                    && type != AuraBlockEntities.AURA_TOTEM_PULL.get()
                    && type != AuraBlockEntities.AURA_TOTEM_PUSH.get()
                    && type != AuraBlockEntities.AURA_TOTEM_POLE.get()) {
                return null;
            }
            return (lvl, p, s, be) -> {
                if (be instanceof AuraTotemTile totem) {
                    if (lvl.isClientSide) {
                        AuraTotemTile.clientTick(lvl, p, s, totem);
                    } else {
                        AuraTotemTile.serverTick(lvl, p, s, totem);
                    }
                }
            };
        } catch (Exception e) {
            return null;
        }
    }
    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                               BlockPos pos, CollisionContext ctx) {
        return state.getValue(TYPE).isPole() ? POLE : FULL;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                BlockPos neighborPos, boolean moved) {
        super.neighborChanged(state, level, pos, block, neighborPos, moved);
        if (level.getBlockEntity(pos) instanceof AuraTotemTile totem) {
            totem.checkPoles();
        }
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).isEmpty()
                && player.getItemInHand(hand).is(this.asItem())) {
            return InteractionResult.PASS;
        }
        if (state.getValue(TYPE).isPole()) {
            BlockState above = level.getBlockState(pos.above());
            if (above.is(this) && above.getBlock() instanceof AuraTotemBlock totemBlock) {
                return totemBlock.use(above, level, pos.above(), player, hand, hit);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isCrouching()) {
            if (!level.isClientSide) {
                TotemType[] variants = TotemType.values();
                TotemType next = variants[(state.getValue(TYPE).ordinal() + 1) % variants.length];
                level.setBlock(pos, state.setValue(TYPE, next), 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AuraTotemTile totem) {
            net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,
                    totem, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
    }
    public enum TotemType implements StringRepresentable {
        PUSH(false),
        PULL(false),
        POLE_OUTER(true),
        POLE_INNER(true),
        POLE_PURE(true);
        private final boolean pole;
        TotemType(boolean pole) {
            this.pole = pole;
        }
        public boolean isPole() {
            return this.pole;
        }
        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
