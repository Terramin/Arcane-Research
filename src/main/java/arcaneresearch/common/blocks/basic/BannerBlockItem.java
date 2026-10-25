package arcaneresearch.common.blocks.basic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.tiles.misc.TileBanner;
import javax.annotation.Nullable;
import java.util.List;
public class BannerBlockItem extends BlockItem {
    public BannerBlockItem(Block block) {
        super(block, new Properties());
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (stack.hasTag() && stack.getTag().contains("aspect")) {
            Aspect aspect = Aspect.getAspect(stack.getTag().getString("aspect"));
            if (aspect != null) {
                tooltip.add(Component.literal(aspect.getName()));
            }
        }
    }
    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (context.getClickedFace() == Direction.DOWN) {
            return false;
        }
        if (!super.placeBlock(context, state)) {
            return false;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileBanner tile) {
            Direction side = context.getClickedFace();
            if (side == Direction.UP) {
                int i = Mth.floor(((context.getRotation() + 180.0f) * 16.0f / 360.0f) + 0.5f) & 15;
                tile.setBannerFacing((byte) i);
            } else {
                tile.setWall(true);
                int i = 0;
                if (side == Direction.NORTH) {
                    i = 8;
                }
                if (side == Direction.WEST) {
                    i = 4;
                }
                if (side == Direction.EAST) {
                    i = 12;
                }
                tile.setBannerFacing((byte) i);
            }
            ItemStack held = context.getItemInHand();
            if (held.hasTag() && held.getTag().contains("aspect")) {
                Aspect aspect = Aspect.getAspect(held.getTag().getString("aspect"));
                if (aspect != null) {
                    tile.setAspect(aspect);
                }
            }
            tile.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        return true;
    }
}
