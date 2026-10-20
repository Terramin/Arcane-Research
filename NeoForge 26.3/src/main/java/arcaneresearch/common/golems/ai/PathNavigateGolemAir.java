package arcaneresearch.common.golems.ai;

import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;

public class PathNavigateGolemAir extends FlyingPathNavigation {
    public PathNavigateGolemAir(net.minecraft.world.entity.Mob mob, net.minecraft.world.level.Level level) {
        super(mob, level);
    }

    public boolean isDirectPathBetweenPoints(net.minecraft.world.phys.Vec3 from, net.minecraft.world.phys.Vec3 to) {
        net.minecraft.world.phys.Vec3 start = new net.minecraft.world.phys.Vec3(
                from.x, from.y + mob.getBbHeight() * 0.5, from.z);
        net.minecraft.world.phys.Vec3 end = new net.minecraft.world.phys.Vec3(
                to.x, to.y + mob.getBbHeight() * 0.5, to.z);
        net.minecraft.world.phys.BlockHitResult hit = level.clip(new net.minecraft.world.level.ClipContext(
                start, end, net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, mob));
        return hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
