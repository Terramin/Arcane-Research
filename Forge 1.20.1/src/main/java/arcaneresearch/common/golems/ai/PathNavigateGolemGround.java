package arcaneresearch.common.golems.ai;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import javax.annotation.Nullable;
public class PathNavigateGolemGround extends GroundPathNavigation {
    private boolean enterDoors = true;
    public PathNavigateGolemGround(Mob mob, Level level) {
        super(mob, level);
        setCanPassDoors(true);
        setCanFloat(true);
    }
    @Override
    public GolemNodeProcessor getNodeEvaluator() {
        return new GolemNodeProcessor();
    }
    @Nullable
    @Override
    public Path createPath(BlockPos pos, int accuracy) {
        if (level.getBlockState(pos).isAir()) {
            BlockPos ground = pos;
            while (ground.getY() > level.getMinBuildHeight()
                    && level.getBlockState(ground).isAir()) {
                ground = ground.below();
            }
            if (ground.getY() > level.getMinBuildHeight()) {
                return liftCauldronNodes(super.createPath(ground.above(), accuracy));
            }
            BlockPos top = pos;
            while (top.getY() < level.getMaxBuildHeight()
                    && level.getBlockState(top).isAir()) {
                top = top.above();
            }
            pos = top;
        }
        if (level.getBlockState(pos).isSolidRender(level, pos)) {
            BlockPos up = pos.above();
            while (up.getY() < level.getMaxBuildHeight()
                    && level.getBlockState(up).isSolidRender(level, up)) {
                up = up.above();
            }
            return liftCauldronNodes(super.createPath(up, accuracy));
        }
        return liftCauldronNodes(super.createPath(pos, accuracy));
    }
    @Nullable
    @Override
    public Path createPath(net.minecraft.world.entity.Entity entity, int accuracy) {
        return liftCauldronNodes(super.createPath(entity, accuracy));
    }
    public boolean getEnterDoors() {
        return enterDoors;
    }
    public void setEnterDoors(boolean enterDoors) {
        this.enterDoors = enterDoors;
        setCanPassDoors(enterDoors);
    }
    public void setAvoidSun(boolean avoidSun) {
    }
    @Nullable
    private Path liftCauldronNodes(@Nullable Path path) {
        if (path != null) {
            for (int i = 0; i < path.getNodeCount(); i++) {
                net.minecraft.world.level.pathfinder.Node node = path.getNode(i);
                if (level.getBlockState(new BlockPos(node.x, node.y, node.z))
                        .is(net.minecraft.world.level.block.Blocks.CAULDRON)) {
                    path.replaceNode(i, new net.minecraft.world.level.pathfinder.Node(
                            node.x, node.y + 1, node.z));
                }
            }
        }
        return path;
    }
}
