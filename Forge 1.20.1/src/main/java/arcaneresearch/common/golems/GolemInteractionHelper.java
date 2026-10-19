package arcaneresearch.common.golems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.common.lib.utils.InventoryUtils;
import java.util.UUID;
public class GolemInteractionHelper {
    private static final GameProfile GOLEM_PROFILE = new GameProfile(
            UUID.fromString("41C82C87-7F55-4A23-8F37-4B2B6F2E3A5E"),
            "FakeArcaneresearchGolem");
    public static void golemClick(Level level, IGolemAPI golem, BlockPos pos, Direction face,
                                  ItemStack clickStack, boolean sneaking, boolean rightClick) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        FakePlayer fp = FakePlayerFactory.get(serverLevel, GOLEM_PROFILE);
        if (golem.getGolemEntity() != null) {
            fp.setPos(golem.getGolemEntity().getX(),
                    golem.getGolemEntity().getY(),
                    golem.getGolemEntity().getZ());
            fp.setYRot(golem.getGolemEntity().getYRot());
            fp.setXRot(golem.getGolemEntity().getXRot());
        }
        fp.setItemInHand(InteractionHand.MAIN_HAND, clickStack);
        fp.setShiftKeyDown(sneaking);
        BlockState blockState = level.getBlockState(pos);
        if (!rightClick) {
            try {
                fp.gameMode.handleBlockBreakAction(pos,
                        net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                        face, level.getMaxBuildHeight(), 0);
            } catch (Exception ignored) {
            }
        } else {
            if (clickStack.getItem() instanceof BlockItem blockItem) {
                if (!mayPlace(level, blockItem.getBlock(), pos)) {
                    if (golem.getGolemEntity() != null) {
                        golem.getGolemEntity().setPos(
                                golem.getGolemEntity().getX() + face.getStepX(),
                                golem.getGolemEntity().getY() + face.getStepY(),
                                golem.getGolemEntity().getZ() + face.getStepZ());
                        golem.getGolemEntity().resetFallDistance();
                    }
                }
            }
            try {
                Vec3 hitVec = new Vec3(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5);
                BlockHitResult hitResult = new BlockHitResult(hitVec, face, pos, false);
                fp.gameMode.useItemOn(fp, level, fp.getItemInHand(InteractionHand.MAIN_HAND),
                        InteractionHand.MAIN_HAND, hitResult);
            } catch (Exception ignored) {
            }
        }
        golem.addRankXp(1);
        if (!fp.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() &&
                fp.getItemInHand(InteractionHand.MAIN_HAND).getCount() <= 0) {
            fp.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        dropSomeItems(fp, golem);
        golem.swingArm();
    }
    private static boolean mayPlace(Level level, net.minecraft.world.level.block.Block blockIn, BlockPos pos) {
        net.minecraft.world.phys.shapes.VoxelShape shape =
                blockIn.defaultBlockState().getShape(level, pos);
        return shape.isEmpty()
                || level.noCollision((net.minecraft.world.entity.Entity) null, shape.bounds().move(pos));
    }
    private static void dropSomeItems(FakePlayer fp, IGolemAPI golem) {
        for (int i = 0; i < fp.getInventory().items.size(); i++) {
            ItemStack stack = fp.getInventory().items.get(i);
            if (!stack.isEmpty()) {
                if (golem.canCarry(stack, true)) {
                    ItemStack remaining = golem.holdItem(stack);
                    fp.getInventory().items.set(i, remaining);
                }
                if (!fp.getInventory().items.get(i).isEmpty()) {
                    InventoryUtils.dropItemAtEntity(golem.getGolemWorld(),
                            fp.getInventory().items.get(i), golem.getGolemEntity());
                    fp.getInventory().items.set(i, ItemStack.EMPTY);
                }
            }
        }
        for (int i = 0; i < fp.getInventory().armor.size(); i++) {
            ItemStack stack = fp.getInventory().armor.get(i);
            if (!stack.isEmpty()) {
                if (golem.canCarry(stack, true)) {
                    ItemStack remaining = golem.holdItem(stack);
                    fp.getInventory().armor.set(i, remaining);
                }
                if (!fp.getInventory().armor.get(i).isEmpty()) {
                    InventoryUtils.dropItemAtEntity(golem.getGolemWorld(),
                            fp.getInventory().armor.get(i), golem.getGolemEntity());
                    fp.getInventory().armor.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
    public static boolean harvestCrop(Level level, IGolemAPI golem, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        golemClick(level, golem, pos, Direction.UP, ItemStack.EMPTY, false, false);
        return true;
    }
    public static boolean useItemOnBlock(Level level, IGolemAPI golem, BlockPos pos,
                                         ItemStack item, Direction face) {
        if (item.isEmpty()) {
            return false;
        }
        golemClick(level, golem, pos, face, item, false, true);
        return true;
    }
}
