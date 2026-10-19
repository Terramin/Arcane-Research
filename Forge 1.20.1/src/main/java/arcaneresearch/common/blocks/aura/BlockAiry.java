package arcaneresearch.common.blocks.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
import javax.annotation.Nullable;
import java.util.Locale;
public class BlockAiry extends BaseEntityBlock {
    public static final EnumProperty<AiryType> TYPE =
            EnumProperty.create("type", AiryType.class);
    public BlockAiry() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                .noCollission()
                .noOcclusion()
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .sound(SoundType.EMPTY));
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, AiryType.AIR));
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        try {
            if (state.getValue(TYPE) == AiryType.STAGING) {
                return new TileNodeEnergized(AuraBlockEntities.ENERGIZED_NODE.get(), pos, state);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return null;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }
    public enum AiryType implements StringRepresentable {
        AIR,
        STAGING;
        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
