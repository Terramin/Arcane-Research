package arcaneresearch.common.golems.ai;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import java.util.EnumSet;
public class AIFollowOwner extends Goal {
    private final EntityArcaneresearchGolem pet;
    private final double speed;
    private final float minDist;
    private final float maxDist;
    private LivingEntity owner;
    private int recalcTime;
    private float waterCost;
    public AIFollowOwner(EntityArcaneresearchGolem pet, double speed, float minDist, float maxDist) {
        this.pet = pet;
        this.speed = speed;
        this.minDist = minDist;
        this.maxDist = maxDist;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(pet.getNavigation() instanceof GroundPathNavigation)
                && !(pet.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }
    @Override
    public boolean canUse() {
        LivingEntity living = pet.getOwner();
        if (living == null) {
            return false;
        }
        if (pet.distanceToSqr(living) < minDist * minDist) {
            return false;
        }
        owner = living;
        return true;
    }
    @Override
    public boolean canContinueToUse() {
        return !pet.getNavigation().isDone()
                && pet.distanceToSqr(owner) > maxDist * maxDist;
    }
    @Override
    public void start() {
        recalcTime = 0;
        waterCost = pet.getPathfindingMalus(BlockPathTypes.WATER);
        pet.setPathfindingMalus(BlockPathTypes.WATER, 0.0f);
    }
    @Override
    public void stop() {
        owner = null;
        pet.getNavigation().stop();
        pet.setPathfindingMalus(BlockPathTypes.WATER, waterCost);
    }
    private boolean isOpenBlock(BlockPos pos) {
        var state = pet.level().getBlockState(pos);
        return state.isAir() || !state.isSolidRender(pet.level(), pos);
    }
    @Override
    public void tick() {
        pet.getLookControl().setLookAt(owner, 10.0f, pet.getMaxHeadXRot());
        if (--recalcTime <= 0) {
            recalcTime = 10;
            if (!pet.getNavigation().moveTo(owner, speed)) {
                if (!pet.isLeashed() && pet.distanceToSqr(owner) >= 144.0) {
                    int x = net.minecraft.util.Mth.floor(owner.getX()) - 2;
                    int z = net.minecraft.util.Mth.floor(owner.getZ()) - 2;
                    int y = net.minecraft.util.Mth.floor(owner.getBoundingBox().minY);
                    for (int l = 0; l <= 4; l++) {
                        for (int i1 = 0; i1 <= 4; i1++) {
                            if ((l < 1 || i1 < 1 || l > 3 || i1 > 3)
                                    && pet.level().getBlockState(new BlockPos(x + l, y - 1, z + i1))
                                    .isSolidRender(pet.level(), new BlockPos(x + l, y - 1, z + i1))
                                    && isOpenBlock(new BlockPos(x + l, y, z + i1))
                                    && isOpenBlock(new BlockPos(x + l, y + 1, z + i1))) {
                                pet.moveTo(x + l + 0.5f, y, z + i1 + 0.5f, pet.getYRot(), pet.getXRot());
                                pet.resetFallDistance();
                                pet.getNavigation().stop();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
