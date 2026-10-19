package arcaneresearch.api.research;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
public class ScanBlockState implements IScanThing {
    private final String research;
    private final BlockState blockState;
    public ScanBlockState(BlockState blockState) {
        this.research = "!" + blockState.toString();
        this.blockState = blockState;
    }
    public ScanBlockState(String research, BlockState blockState) {
        this.research = research;
        this.blockState = blockState;
    }
    public ScanBlockState(String research, BlockState blockState, boolean item) {
        this.research = research;
        this.blockState = blockState;
        if (item) {
            ScanningManager.addScannableThing(new ScanItem(research,
                new ItemStack(blockState.getBlock())));
        }
    }
    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj instanceof BlockPos pos) {
            return player.level().getBlockState(pos) == blockState;
        }
        return false;
    }
    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}