package arcaneresearch.common.items.consumables;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.blocks.ILabelable;
public class ItemLabel extends Item {
    private final boolean isFilled;
    public ItemLabel(boolean filled) {
        super(new Item.Properties());
        this.isFilled = filled;
    }
    public static ItemLabel createBlank() {
        return new ItemLabel(false);
    }
    public static ItemLabel createFilled() {
        return new ItemLabel(true);
    }
    public Aspect getAspect(ItemStack stack) {
        if (isFilled && stack.hasTag() && stack.getTag().contains("aspect")) {
            return Aspect.getAspect(stack.getTag().getString("aspect"));
        }
        return null;
    }
    public void setAspect(ItemStack stack, Aspect aspect) {
        if (aspect != null) {
            stack.getOrCreateTag().putString("aspect", aspect.getTag());
        }
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ILabelable labelable) {
            if (labelable.applyLabel(player, pos, side, stack)) {
                stack.shrink(1);
                player.inventoryMenu.broadcastChanges();
                return InteractionResult.SUCCESS;
            }
        }
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof ILabelable labelable) {
            if (labelable.applyLabel(player, pos, side, stack)) {
                stack.shrink(1);
                player.inventoryMenu.broadcastChanges();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
