package thaumictinker.tablet;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

 

 
public class TabletFakePlayer extends FakePlayer {

    private static final GameProfile PROFILE = new GameProfile(
            UUID.fromString("a8f026a0-135b-11e4-9191-0800200c9a66"), "[ThaumcraftTablet]");

    public TabletFakePlayer(ServerLevel level) {
        super(level, PROFILE);
    }

    public void align(BlockPos tablet, Direction facing, ItemStack held) {
        setPos(tablet.getX() + 0.5D, tablet.getY() + 1.6D, tablet.getZ() + 0.5D);
        float rotation = facing.toYRot();
        setYRot(rotation);
        setYHeadRot(rotation);
        setXRot(-15.0F);
        setDeltaMovement(Vec3.ZERO);
        setHealth(20.0F);
        getInventory().clearContent();
        getInventory().setItem(0, held);
    }

    public ItemStack release(ServerLevel level, BlockPos tablet) {
        ItemStack held = getInventory().getItem(0);
        for (int slot = 1; slot < getInventory().getContainerSize(); slot++) {
            ItemStack extra = getInventory().getItem(slot);
            if (!extra.isEmpty()) {
                Block.popResource(level, tablet.above(), extra.copy());
            }
        }
        getInventory().clearContent();
        return held;
    }

    @Override
    public float getAttackStrengthScale(float adjustTicks) {
        return 1.0F;
    }
}
