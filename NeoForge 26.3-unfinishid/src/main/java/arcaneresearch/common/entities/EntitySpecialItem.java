package arcaneresearch.common.entities;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModEntities;

public class EntitySpecialItem extends ItemEntity {

    public final float bobOffs;

    public EntitySpecialItem(EntityType<? extends EntitySpecialItem> type, Level level) {
        super(type, level);
        this.bobOffs = (float) (Math.random() * Math.PI * 2.0);
    }

    public EntitySpecialItem(Level level) {
        super(ModEntities.SPECIAL_ITEM.get(), level);
        this.bobOffs = (float) (Math.random() * Math.PI * 2.0);
    }

    public EntitySpecialItem(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.SPECIAL_ITEM.get(), level);
        this.bobOffs = (float) (Math.random() * Math.PI * 2.0);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setYRot((float) (Math.random() * 360.0));
        this.setDeltaMovement(
                (Math.random() * 0.2 - 0.1),
                0.2,
                (Math.random() * 0.2 - 0.1)
        );
    }

    @Override
    public void tick() {

        if (tickCount > 1) {

            if (getDeltaMovement().y > 0) {
                setDeltaMovement(getDeltaMovement().multiply(1.0, 0.9, 1.0));
            }

            setDeltaMovement(getDeltaMovement().add(0, 0.04, 0));
            super.tick();
        }
    }

    // NEOFORGE_PORT: Forge overrode hurt() to ignore IS_EXPLOSION damage.
    // 26.x routes explosion damage through ignoreExplosion (hurtServer is final
    // in ItemEntity), so immunity is expressed here instead — same behavior.
    @Override
    public boolean ignoreExplosion(net.minecraft.world.level.Explosion explosion) {
        return true;
    }
}
