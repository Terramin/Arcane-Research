package arcaneresearch.common.golems.parts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.GolemLeg;
import java.util.HashMap;
public class GolemLegWheels implements GolemLeg.ILegFunction {
    public static HashMap<Integer, Float> ani = new HashMap<>();
    @Override
    public void onUpdateTick(IGolemAPI golem) {
        LivingEntity entity = golem.getGolemEntity();
        Level world = golem.getGolemWorld();
        if (!world.isClientSide) {
            return;
        }
        double dist = entity.distanceToSqr(entity.xo, entity.yo, entity.zo);
        dist = Math.sqrt(dist);
        float lastRot = 0.0F;
        Integer id = entity.getId();
        if (ani.containsKey(id)) {
            lastRot = ani.get(id);
        }
        double d0 = entity.getX() - entity.xo;
        double d2 = entity.getZ() - entity.zo;
        float dirTravel = (float) (Mth.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        double dir = 360.0F - (entity.getYRot() - dirTravel);
        lastRot = (float) (lastRot + dist / 1.571D * dir);
        if (lastRot > 360.0F) lastRot -= 360.0F;
        ani.put(id, lastRot);
        if (entity.onGround() && !entity.isInWater() && dist > 0.25D) {
            int i = Mth.floor(entity.getX());
            int j = Mth.floor(entity.getY() - 0.20000000298023224D);
            int k = Mth.floor(entity.getZ());
            BlockPos blockpos = new BlockPos(i, j, k);
            BlockState state = world.getBlockState(blockpos);
            if (state.getRenderShape() != net.minecraft.world.level.block.RenderShape.INVISIBLE) {
                world.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state),
                        entity.getX() + (world.random.nextFloat() - 0.5D) * entity.getBbWidth(),
                        entity.getBoundingBox().minY + 0.1D,
                        entity.getZ() + (world.random.nextFloat() - 0.5D) * entity.getBbWidth(),
                        -entity.getDeltaMovement().x * 4.0D, 1.5D,
                        -entity.getDeltaMovement().z * 4.0D);
            }
        }
    }
}
