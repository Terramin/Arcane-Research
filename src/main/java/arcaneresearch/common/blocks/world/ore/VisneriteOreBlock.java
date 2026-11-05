package arcaneresearch.common.blocks.world.ore;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class VisneriteOreBlock extends DropExperienceBlock {
    public VisneriteOreBlock(BlockBehaviour.Properties properties, UniformInt experience) {
        super(properties, experience);
    }

    public static VisneriteOreBlock createVisneriteOre() {
        return new VisneriteOreBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.STONE)
                        .strength(35.0f, 2000.0f)
                        .sound(SoundType.ANCIENT_DEBRIS)
                        .requiresCorrectToolForDrops(),
                UniformInt.of(1, 4));
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        ItemStack tool = player.getMainHandItem();
        if (tool.getItem() instanceof PickaxeItem pickaxe) {
            return pickaxe.getTier().getLevel() >= Tiers.NETHERITE.getLevel();
        }
        return false;
    }
}
