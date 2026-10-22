package arcaneresearch.common.blocks.aura;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

import java.util.Locale;

 




 
public class BlockObsidianTotem extends Block {
    public static final EnumProperty<TotemFace> VARIANT =
            EnumProperty.create("variant", TotemFace.class);

    public BlockObsidianTotem(BlockBehaviour.Properties props) {
        super(props.mapColor(MapColor.COLOR_BLACK)
                .strength(30.0F, 999.0F).sound(SoundType.STONE));
        this.registerDefaultState(this.defaultBlockState().setValue(VARIANT, TotemFace.PILLAR_1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    public enum TotemFace implements StringRepresentable {
        BASE,
        PILLAR_1,
        PILLAR_2,
        PILLAR_3,
        PILLAR_4;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
