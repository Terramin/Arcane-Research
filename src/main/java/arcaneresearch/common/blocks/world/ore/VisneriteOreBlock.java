package arcaneresearch.common.blocks.world.ore;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class VisneriteOreBlock extends DropExperienceBlock {
    public VisneriteOreBlock(UniformInt experience, BlockBehaviour.Properties properties) {
        super(experience, properties);
    }

    public static VisneriteOreBlock createVisneriteOre(BlockBehaviour.Properties baseProperties) {
        return new VisneriteOreBlock(UniformInt.of(1, 4), baseProperties
                .mapColor(MapColor.STONE)
                .strength(35.0f, 2000.0f)
                .sound(SoundType.ANCIENT_DEBRIS)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 15));
    }
}
