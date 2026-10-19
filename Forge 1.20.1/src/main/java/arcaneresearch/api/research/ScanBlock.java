package arcaneresearch.api.research;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
public class ScanBlock implements IScanThing {
    private final String research;
    private final Block[] blocks;
    public ScanBlock(Block block) {
        this("!" + block.builtInRegistryHolder().key().location().toString(), block);
    }
    public ScanBlock(String research, Block... blocks) {
        this.research = research;
        this.blocks = blocks;
        for (Block block : blocks) {
            ItemStack stack = new ItemStack(block);
            if (!stack.isEmpty()) {
                ScanningManager.addScannableThing(new ScanItem(research, stack));
            }
        }
    }
    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj instanceof BlockPos pos) {
            Block worldBlock = player.level().getBlockState(pos).getBlock();
            for (Block block : blocks) {
                if (worldBlock == block) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}
