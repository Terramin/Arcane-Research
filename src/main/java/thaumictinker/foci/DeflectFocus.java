package thaumictinker.foci;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;

 

 
public class DeflectFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.ORDER, 8).add(Aspect.AIR, 4);

    public DeflectFocus() {
        super("focusdeflect", 0xFFFFFF);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0xFFFFFF;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "DEFLECT";
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        float modifier = getUpgradeLevel(focus, FocusUpgradeType.frugal) * 0.10F;
        AspectList cost = COST.copy();
        for (Aspect aspect : COST.getAspects()) {
            cost.remove(aspect, Math.round(cost.getAmount(aspect) * modifier));
        }
        return cost;
    }

    @Nullable
    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[]{FocusUpgradeType.frugal};
    }

    public static boolean isDeflectable(Projectile projectile) {
        if (projectile instanceof ThrownExperienceBottle) {
            return false;
        }
        return projectile instanceof AbstractArrow || projectile instanceof ThrowableProjectile;
    }

    public static List<Projectile> findProjectiles(Player player) {
        double x = player.getX();
        double y = player.getEyeY();
        double z = player.getZ();
        AABB area = new AABB(x - 4, y - 4, z - 4, x + 3, y + 3, z + 3);
        return player.level().getEntitiesOfClass(
                Projectile.class,
                area,
                projectile -> isDeflectable(projectile) && projectile.getOwner() != player);
    }

    public static void protect(Player player) {
        Level level = player.level();
        Vec3 origin = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        for (Projectile projectile : findProjectiles(player)) {
            if (level.isClientSide) {
                FociFx.sparkle(level, player, projectile.getX(), projectile.getY(), projectile.getZ(), 1.0F, 1.0F, 1.0F);
                continue;
            }
            Vec3 motion = projectile.getDeltaMovement();
            if (motion.lengthSqr() < 1.0E-7) {
                continue;
            }
            Vec3 shift = motion.normalize().scale(projectile.position().distanceTo(origin) * 2);
            projectile.setPos(projectile.position().add(shift));
        }
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (findProjectiles(player).isEmpty()) {
            return false;
        }
        protect(player);
        return true;
    }
}
