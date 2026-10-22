package thaumictinker.foci;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.FocusUpgradeType;

 

 
public class TelekinesisFocus extends TtFocusBase {

    private static final AspectList COST = new AspectList().add(Aspect.AIR, 5).add(Aspect.ENTROPY, 5);
    private static final int RANGE = 6;
    private static final double DISTANCE = RANGE - 1;

    public TelekinesisFocus() {
        super("focustelekinesis", 0x9C00BE);
    }

    @Override
    public int getFocusColor(ItemStack focus) {
        return 0x9C00BE;
    }

    @Override
    public String getSortingHelper(ItemStack focus) {
        return "TELEKINESIS";
    }

    @Override
    public AspectList getVisCost(ItemStack focus) {
        return COST.copy();
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focus) {
        return true;
    }

    @Override
    @Nullable
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focus, int rank) {
        return new FocusUpgradeType[0];
    }

    public static Vec3 target(Player player) {
        Vec3 target = player.position().add(0.0, player.getBbHeight() / 2.0, 0.0);
        if (!player.isShiftKeyDown()) {
            target = target.add(player.getLookAngle().scale(DISTANCE));
        }
        return target.add(0.0, 0.5, 0.0);
    }

    public static void pull(ItemEntity entity, Vec3 target, float modifier) {
        Vec3 center = entity.position().add(0.0, entity.getBbHeight() / 2.0, 0.0);
        Vec3 difference = target.subtract(center);
        if (difference.length() > 1.0) {
            difference = difference.normalize();
        }
        entity.setDeltaMovement(difference.scale(modifier));
    }

    @Override
    public boolean onFocusActivation(ItemStack wand, Level level, LivingEntity entity, @Nullable HitResult hit, int useCount) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        Vec3 target = target(player);
        AABB area = new AABB(target.x - RANGE, target.y - RANGE, target.z - RANGE, target.x + RANGE, target.y + RANGE, target.z + RANGE);
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, area);
        if (entities.isEmpty()) {
            return false;
        }
        for (ItemEntity itemEntity : entities) {
            pull(itemEntity, target, 0.3333F);
            FociFx.sparkle(level, player, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 0.6F, 0.0F, 0.75F);
        }
        return true;
    }
}
