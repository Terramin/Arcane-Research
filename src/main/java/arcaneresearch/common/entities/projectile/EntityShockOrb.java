package arcaneresearch.common.entities.projectile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.ArrayList;
import java.util.List;
public class EntityShockOrb extends ThrowableItemProjectile {
    public int area = 4;
    public int damage = 5;
    public EntityShockOrb(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
    }
    public EntityShockOrb(EntityType<? extends EntityShockOrb> type, Level level, LivingEntity owner) {
        super(type, owner, level);
    }
    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }
    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 500) {
            this.discard();
        }
    }
    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!this.level().isClientSide) {
            List<Entity> list = new ArrayList<>(this.level().getEntities(this,
                    new AABB(this.getX() - this.area, this.getY() - this.area, this.getZ() - this.area,
                            this.getX() + this.area, this.getY() + this.area, this.getZ() + this.area)));
            for (Entity e : list) {
                e.hurt(this.damageSources().indirectMagic(this,
                        this.getOwner() instanceof LivingEntity o ? o : this), this.damage);
            }
            for (int a = 0; a < 20; a++) {
                int xx = (int) Math.floor(this.getX()) + this.random.nextInt(this.area) - this.random.nextInt(this.area);
                int yy = (int) Math.floor(this.getY()) + this.area;
                int zz = (int) Math.floor(this.getZ()) + this.random.nextInt(this.area) - this.random.nextInt(this.area);
                BlockPos bp = new BlockPos(xx, yy, zz);
                while (this.level().isEmptyBlock(bp) && yy > Math.floor(this.getY()) - this.area) {
                    yy--;
                    bp = new BlockPos(xx, yy, zz);
                }
            }
        } else {
            spawnBurstFX();
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_THUNDER, net.minecraft.sounds.SoundSource.HOSTILE,
                1.0F, 1.0F);
        this.discard();
    }
    @OnlyIn(Dist.CLIENT)
    private void spawnBurstFX() {
        FXDispatcher.INSTANCE.burst(this.getX(), this.getY(), this.getZ(), 3.0F);
    }
}
