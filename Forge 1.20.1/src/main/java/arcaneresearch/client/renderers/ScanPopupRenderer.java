package arcaneresearch.client.renderers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
@OnlyIn(Dist.CLIENT)
public class ScanPopupRenderer {
    public static volatile Entity thaumTarget = null;
    public static volatile BlockPos thaumBlockTarget = null;
    public static AspectList getCurrentAspects(Player player) {
        if (player == null || player.level() == null) {
            return null;
        }
        if (thaumTarget != null && thaumTarget.isAlive()
                && thaumTarget.level() == player.level()) {
            AspectList tags = AspectHelper.getEntityAspects(thaumTarget);
            if (tags != null && tags.size() > 0) {
                return tags;
            }
        }
        if (thaumBlockTarget != null) {
            BlockState state = player.level().getBlockState(thaumBlockTarget);
            if (!state.isAir()) {
                ItemStack stack = ItemStack.EMPTY;
                try {
                    stack = state.getBlock().getCloneItemStack(player.level(), thaumBlockTarget, state);
                } catch (Exception ignored) {
                }
                if (stack.isEmpty()) {
                    stack = new ItemStack(state.getBlock());
                }
                AspectList tags = arcaneresearch.api.ArcaneresearchApi.internalMethods.getObjectAspects(stack);
                if (tags != null && tags.size() > 0) {
                    return tags;
                }
            }
        }
        return null;
    }
    public static Object getCurrentTarget(Player player) {
        if (player == null || player.level() == null) {
            return null;
        }
        if (thaumTarget != null && thaumTarget.isAlive()
                && thaumTarget.level() == player.level()) {
            return thaumTarget;
        }
        if (thaumBlockTarget != null && !player.level().getBlockState(thaumBlockTarget).isAir()) {
            return thaumBlockTarget;
        }
        return null;
    }
    @Deprecated
    public static void render(PoseStack poseStack, float partialTick) {
    }
    public static void reset() {
        thaumTarget = null;
        thaumBlockTarget = null;
    }
}
