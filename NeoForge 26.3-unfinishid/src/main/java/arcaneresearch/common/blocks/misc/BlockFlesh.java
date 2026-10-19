package arcaneresearch.common.blocks.misc;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import arcaneresearch.common.blocks.BlockTC;
import arcaneresearch.init.ModSounds;

public class BlockFlesh extends BlockTC {

    public BlockFlesh(BlockBehaviour.Properties props) {
        super(props
                .mapColor(MapColor.COLOR_RED)
                .strength(0.25f, 2.0f)
                .sound(ModSounds.GORE_TYPE));
    }
}
