package arcaneresearch.common.tiles.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import arcaneresearch.api.entities.IEldritchMob;
import arcaneresearch.common.tiles.TileArcaneresearch;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.List;
public class EldritchObeliskTile extends TileArcaneresearch {
    private int counter = 0;
    public EldritchObeliskTile(BlockEntityType<?> tileType, BlockPos pos, BlockState state) {
        super(tileType, pos, state);
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, EldritchObeliskTile tile) {
        tile.serverTick();
    }
    public static void clientTick(Level level, BlockPos pos, BlockState state, EldritchObeliskTile tile) {
        tile.clientTick();
    }
    protected void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (this.counter++ % 20 != 0) {
            return;
        }
        try {
            AABB box = new AABB(this.worldPosition).inflate(6.0D, 6.0D, 6.0D);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
            for (LivingEntity entity : entities) {
                if (!(entity instanceof IEldritchMob)) {
                    continue;
                }
                if (entity.hasEffect(MobEffects.REGENERATION)) {
                    continue;
                }
                try {
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, true));
                    entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
    @OnlyIn(Dist.CLIENT)
    protected void clientTick() {
        Level level = getLevel();
        if (level == null || !level.isClientSide) {
            return;
        }
        try {
            AABB box = new AABB(this.worldPosition).inflate(6.0D, 6.0D, 6.0D);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box);
            if (entities.isEmpty()) {
                return;
            }
            for (LivingEntity entity : entities) {
                if (!(entity instanceof IEldritchMob)) {
                    continue;
                }
                try {
                    arcaneresearch.client.fx.FXDispatcher.INSTANCE.wispFXEG(
                            this.worldPosition.getX() + 0.5D,
                            this.worldPosition.getY() + 1.0D + level.random.nextFloat() * 3.0F,
                            this.worldPosition.getZ() + 0.5D,
                            (Entity) entity);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition.getX() - 0.3D, this.worldPosition.getY() - 0.3D,
                this.worldPosition.getZ() - 0.3D, this.worldPosition.getX() + 1.3D,
                this.worldPosition.getY() + 5.3D, this.worldPosition.getZ() + 1.3D);
    }
}
