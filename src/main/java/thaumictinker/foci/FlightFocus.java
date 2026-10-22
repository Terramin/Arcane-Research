package thaumictinker.foci;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModSounds;

 



 
public class FlightFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.AIR, 15);

    public FlightFocus() {
        super("focusflight", 0x9EF2FF);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9EF2FF;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "FLIGHT";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Nullable
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.treasure, FocusUpgradeType.potency};
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        IWand wandItem = (IWand) wand.getItem();
        if (!wandItem.consumeAllVis(wand, player, COST.copy(), true, false)) {
            return false;
        }
        Vec3 look = player.getLookAngle();
        double force = 1.0 / 1.5 * (1.0 + wandItem.getFocusPotency(wand) * 0.2);
        if (level instanceof ServerLevel serverLevel) {
            player.setDeltaMovement(look.x * force, look.y * force, look.z * force);
            player.resetFallDistance();
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.WIND.get(), SoundSource.PLAYERS, 0.4F, 1.0F);
        } else {
            for (int index = 0; index < 5; index++) {
                FXDispatcher.INSTANCE.smokeSpiral(
                        player.getX(),
                        player.getEyeY() - look.y * force,
                        player.getZ(),
                        2.0F,
                        level.random.nextInt(360),
                        (int) player.getEyeY(),
                        0x9E2FF);
            }
        }
        return true;
    }
}
