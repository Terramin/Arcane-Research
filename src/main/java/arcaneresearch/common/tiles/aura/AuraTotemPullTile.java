package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aura.AuraHelper;

 




 
public class AuraTotemPullTile extends AuraTotemTile {
    public AuraTotemPullTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    @Override
    protected void performMagnet() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        int x1 = Mth.randomBetweenInclusive(level.getRandom(), -this.zoneInner, this.zoneInner);
        int z1 = Mth.randomBetweenInclusive(level.getRandom(), -this.zoneInner, this.zoneInner);
        int x2 = 0;
        int z2 = 0;
        int cc = 0;
        while (cc < 100 && x2 >= -this.zoneInner && x2 <= this.zoneInner && z2 >= -this.zoneInner
                && z2 <= this.zoneInner) {
            x2 = Mth.randomBetweenInclusive(level.getRandom(), -(this.zoneInner + this.zoneOuter + 1),
                    this.zoneInner + this.zoneOuter + 1);
            z2 = Mth.randomBetweenInclusive(level.getRandom(), -(this.zoneInner + this.zoneOuter + 1),
                    this.zoneInner + this.zoneOuter + 1);
            cc++;
        }
        if (cc >= 100) {
            return;
        }
        int x = this.worldPosition.getX() + x2 * 16;
        int z = this.worldPosition.getZ() + z2 * 16;
        if (AuraHelper.drainVis(level, new BlockPos(x, 0, z), 1.0F, false) > 0.0F) {
            float tc = 0.0125F * (this.zoneInner + this.zoneOuter + 2) - 0.025F * this.zonePure;
            x = this.worldPosition.getX() + x1 * 16;
            z = this.worldPosition.getZ() + z1 * 16;
            if (level.getRandom().nextFloat() < tc) {
                AuraHelper.polluteAura(level, this.worldPosition, 1.0F, true);
            } else {
                AuraHelper.addVis(level, new BlockPos(x, 0, z), 1.0F);
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void drawEffect() {
        super.drawEffect();
        Level level = getLevel();
        if (level == null || !level.isClientSide()) {
            return;
        }
        Aspect aspect = getAspect();
        float r = 0.65F;
        float g = 1.0F;
        float b = 1.0F;
        if (aspect != null) {
            int color = aspect.getColor();
            r = (color >> 16 & 255) / 255.0F;
            g = (color >> 8 & 255) / 255.0F;
            b = (color & 255) / 255.0F;
        }
        double sx = this.worldPosition.getX() + 0.5D
                + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * (5 + this.zoneOuter);
        double sy = this.worldPosition.getY() + level.getRandom().nextInt(4) - level.getRandom().nextInt(3);
        double sz = this.worldPosition.getZ() + 0.5D
                + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * (5 + this.zoneOuter);
        arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawWispyMotes(
                sx, sy, sz,
                (this.worldPosition.getX() + 0.5D - sx) * 0.05D,
                (this.worldPosition.getY() + 0.5D - sy) * 0.05D,
                (this.worldPosition.getZ() + 0.5D - sz) * 0.05D,
                40, 0.0F);
    }
}
