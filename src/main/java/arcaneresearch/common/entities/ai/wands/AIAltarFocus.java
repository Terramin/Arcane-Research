package arcaneresearch.common.entities.ai.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import arcaneresearch.api.blocks.BlocksTC;
import arcaneresearch.common.entities.monster.cult.EntityCultistCleric;

import java.util.EnumSet;

public class AIAltarFocus extends Goal {
    private final EntityCultistCleric cleric;

    public AIAltarFocus(EntityCultistCleric cleric) {
        this.cleric = cleric;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.cleric.isRitualist() && this.cleric.hasHome();
    }

    @Override
    public boolean canContinueToUse() {
        return this.cleric.isRitualist() && this.cleric.hasHome();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void tick() {
        BlockPos home = this.cleric.getHomePos();
        if (home != null && this.cleric.tickCount % 40 == 0
                && (home.distSqr(this.cleric.blockPosition()) > 16.0D || this.isAltarGone(home))) {
            this.cleric.setRitualist(false);
        }
    }

    private boolean isAltarGone(BlockPos home) {
        try {
            Block eldritch = BlocksTC.stoneEldritchTile.get();
            return this.cleric.level().getBlockState(home).getBlock() != eldritch;
        } catch (Exception e) {
            return false;
        }
    }
}
