package arcaneresearch.common.tiles.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
    private static final double EFFECT_RADIUS = 6.0D;

    private int counter = 0;

    private AABB effectBox;
    private AABB renderBox;

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
        if (level == null || level.isClientSide) return;

        int tick = this.counter++;
        if (tick % 20 != 0) return;

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectBox());

        for (LivingEntity entity : entities) {
            if (!(entity instanceof IEldritchMob)) continue;
            if (entity.hasEffect(MobEffects.REGENERATION)) continue;

            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, true));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0, true, true));
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void clientTick() {
        Level level = getLevel();
        if (level == null || !level.isClientSide) return;

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, effectBox());
        if (entities.isEmpty()) return;

        double ox = this.worldPosition.getX() + 0.5D;
        double oy = this.worldPosition.getY() + 1.0D;
        double oz = this.worldPosition.getZ() + 0.5D;

        for (LivingEntity entity : entities) {
            if (!(entity instanceof IEldritchMob)) continue;

            arcaneresearch.client.fx.FXDispatcher.INSTANCE.wispFXEG(
                    ox, oy + level.random.nextFloat() * 3.0F, oz, entity);
        }
    }

    private AABB effectBox() {
        if (effectBox == null) {
            effectBox = new AABB(this.worldPosition).inflate(EFFECT_RADIUS);
        }

        return effectBox;
    }

    @Override
    public AABB getRenderBoundingBox() {
        if (renderBox == null) {
            renderBox = new AABB(
                    this.worldPosition.getX() - 0.3D, this.worldPosition.getY() - 0.3D,
                    this.worldPosition.getZ() - 0.3D,
                    this.worldPosition.getX() + 1.3D, this.worldPosition.getY() + 5.3D,
                    this.worldPosition.getZ() + 1.3D
            );
        }

        return renderBox;
    }
}