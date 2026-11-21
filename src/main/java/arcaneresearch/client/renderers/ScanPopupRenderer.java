package arcaneresearch.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.debug.ArcaneLogger;
import arcaneresearch.debug.LogLevel;
import arcaneresearch.debug.ProfileCategory;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ScanPopupRenderer {
    private static final ArcaneLogger LOGGER = ArcaneLogger.forCategory(ProfileCategory.RENDER);
    public static volatile Entity thaumTarget = null;
    public static volatile BlockPos thaumBlockTarget = null;

    public static AspectList getCurrentAspects(@NotNull Player player) {
        Level level = player.level();

        Entity target = thaumTarget;
        if (target != null && target.isAlive()) {
            Level targetLevel = target.level();
            if (targetLevel == level) {
                AspectList tags = AspectHelper.getEntityAspects(target);
                if (tags != null && tags.size() > 0) {
                    LOGGER.once("render.scan.entity", LogLevel.DEBUG,
                            "Scan popup resolved entity {} to {} aspects", target, tags.size());
                    return tags;
                }
            }
        }

        BlockPos blockTarget = thaumBlockTarget;
        if (blockTarget != null) {
            BlockState state = level.getBlockState(blockTarget);
            if (!state.isAir()) {
                Block block = state.getBlock();
                ItemStack stack = ItemStack.EMPTY;
                try {
                    stack = block.getCloneItemStack(level, blockTarget, state);
                } catch (Exception ignored) {
                }
                if (stack.isEmpty()) {
                    stack = new ItemStack(block);
                }
                AspectList tags = arcaneresearch.api.ArcaneresearchApi.internalMethods.getObjectAspects(stack);
                if (tags != null && tags.size() > 0) {
                    LOGGER.once("render.scan.block", LogLevel.DEBUG,
                            "Scan popup resolved block {} at {} to {} aspects", block, blockTarget, tags.size());
                    return tags;
                }
            }
        }
        return null;
    }

    public static Object getCurrentTarget(@NotNull Player player) {
        Level level = player.level();

        Entity target = thaumTarget;
        if (target != null && target.isAlive()) {
            Level targetLevel = target.level();
            if (targetLevel == level) {
                return target;
            }
        }

        BlockPos blockTarget = thaumBlockTarget;
        if (blockTarget != null) {
            BlockState state = level.getBlockState(blockTarget);
            if (!state.isAir()) {
                return blockTarget;
            }
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