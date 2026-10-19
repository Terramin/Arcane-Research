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

import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Тайл обелиска, порт TC4 TileEldritchObelisk (через TC5):
 * сервер — каждые 20 тиков вешает баффы на элдритч-мобов в радиусе 6;
 * клиент — тянучки-виспы от пилона к мобам (TC4 wispFX4).
 * Лежит в arcaneresearch.arcaneresearch.common.tiles.aura.
 */
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
        if (level == null || level.isClientSide()) {
            return;
        }
        if (this.counter++ % 20 != 0) {
            return;
        }
        try {
            AABB box = new AABB(this.worldPosition).inflate(6.0D, 6.0D, 6.0D);
            List<LivingEntity> entities = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), box, e -> true);
            for (LivingEntity entity : entities) {
                if (!(entity instanceof IEldritchMob)) {
                    continue;
                }
                // 1:1 с TC5 (порт 1.8.9): бафф только если нет регенерации.
                if (entity.hasEffect(MobEffects.REGENERATION)) {
                    continue;
                }
                try {
                    entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 40, 0, true, true));
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
        if (level == null || !level.isClientSide()) {
            return;
        }
        try {
            AABB box = new AABB(this.worldPosition).inflate(6.0D, 6.0D, 6.0D);
            List<LivingEntity> entities = level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(LivingEntity.class), box, e -> true);
            if (entities.isEmpty()) {
                return;
            }
            for (LivingEntity entity : entities) {
                if (!(entity instanceof IEldritchMob)) {
                    continue;
                }
                try {
                    // TC4: Arcaneresearch.proxy.wispFX4(world, x+0.5, y+1+rand*3, z+0.5, e, 5, true, 1.0F).
                    arcaneresearch.client.fx.FXDispatcher.INSTANCE.wispFXEG(
                            this.worldPosition.getX() + 0.5D,
                            this.worldPosition.getY() + 1.0D + level.getRandom().nextFloat() * 3.0F,
                            this.worldPosition.getZ() + 0.5D,
                            (Entity) entity);
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
    }

    // NEOFORGE_PORT: BlockEntity.getRenderBoundingBox removed in 26.x; kept as
    // a plain bounds helper (EldritchObeliskRenderer opts out of frustum
    // culling via shouldRenderOffScreen, so vanilla never needs this).
    public AABB getRenderBoundingBox() {
        // 1:1 с TC5 (границы рендера пилона y..y+5).
        return new AABB(this.worldPosition.getX() - 0.3D, this.worldPosition.getY() - 0.3D,
                this.worldPosition.getZ() - 0.3D, this.worldPosition.getX() + 1.3D,
                this.worldPosition.getY() + 5.3D, this.worldPosition.getZ() + 1.3D);
    }
}
