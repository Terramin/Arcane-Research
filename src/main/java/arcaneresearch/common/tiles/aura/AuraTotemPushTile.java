package arcaneresearch.common.tiles.aura;

import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.client.fx.FXDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AuraTotemPushTile extends AuraTotemTile {

    public AuraTotemPushTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }

    @Override
    protected void performMagnet() {
        Level level = getLevel();
        if (level == null || level.isClientSide) return;
        if (this.zoneInner < 0 || this.zoneOuter < 0) return;

        final int zoneInner = this.zoneInner;
        final int zoneOuter = this.zoneOuter;
        final int outerBound = zoneInner + zoneOuter + 1;
        if (outerBound < 1) return;

        final int x1 = Mth.randomBetweenInclusive(level.random, -zoneInner, zoneInner);
        final int z1 = Mth.randomBetweenInclusive(level.random, -zoneInner, zoneInner);

        int x2 = 0;
        int z2 = 0;
        int cc = 0;
        while (cc < 100 && x2 >= -zoneInner && x2 <= zoneInner && z2 >= -zoneInner && z2 <= zoneInner) {
            x2 = Mth.randomBetweenInclusive(level.random, -outerBound, outerBound);
            z2 = Mth.randomBetweenInclusive(level.random, -outerBound, outerBound);
            cc++;
        }
        if (cc >= 100) return;

        final int wx = this.worldPosition.getX();
        final int wz = this.worldPosition.getZ();

        int x = wx + x1 * 16;
        int z = wz + z1 * 16;
        if (AuraHelper.drainVis(level, new BlockPos(x, 0, z), 1.0F, false) > 0.0F) {
            final float tc = 0.0125F * (zoneInner + zoneOuter + 2) - 0.1F * this.zonePure;
            x = wx + x2 * 16;
            z = wz + z2 * 16;
            if (level.random.nextFloat() < tc) {
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
        if (level == null || !level.isClientSide) return;

        final int wx = this.worldPosition.getX();
        final int wy = this.worldPosition.getY();
        final int wz = this.worldPosition.getZ();

        final double spread = 5 + this.zoneOuter;
        final double dx = 0.5D + (level.random.nextFloat() - level.random.nextFloat()) * spread;
        final double dy = level.random.nextInt(4) - level.random.nextInt(3);
        final double dz = 0.5D + (level.random.nextFloat() - level.random.nextFloat()) * spread;

        FXDispatcher.INSTANCE.drawWispyMotes(
                wx + 0.5D, wy + 0.5D, wz + 0.5D,
                dx * 0.05D, dy * 0.05D, dz * 0.05D,
                40, 0.0F);
    }
}