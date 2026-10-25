package arcaneresearch.common.entities.ai.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;
import arcaneresearch.init.ModBlocks;
import java.util.EnumSet;
public class AIAltarFocus extends Goal {
    private final EntityCultistCleric cleric;
    public AIAltarFocus(EntityCultistCleric cleric) {
        this.cleric = cleric;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }
    @Override
    public boolean canUse() {
        return cleric.isRitualist() && cleric.hasHome();
    }
    @Override
    public boolean canContinueToUse() {
        return cleric.isRitualist() && cleric.hasHome();
    }
    @Override
    public void tick() {
        BlockPos home = cleric.getHomePos();
        if (home != null && cleric.tickCount % 40 == 0
                && (home.distSqr(cleric.blockPosition()) > 16.0
                || cleric.level().getBlockState(home).getBlock()
                != ModBlocks.ELDRITCH_STONE_TILE.get())) {
            cleric.setRitualist(false);
        }
    }
}
