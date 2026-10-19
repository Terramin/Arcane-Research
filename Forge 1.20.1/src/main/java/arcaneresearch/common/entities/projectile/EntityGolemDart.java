package arcaneresearch.common.entities.projectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import arcaneresearch.init.ModEntities;
public class EntityGolemDart extends AbstractArrow {
    public EntityGolemDart(EntityType<? extends EntityGolemDart> type, Level level) {
        super(type, level);
    }
    public EntityGolemDart(Level level, double x, double y, double z) {
        super(ModEntities.GOLEM_DART.get(), x, y, z, level);
    }
    public EntityGolemDart(Level level, LivingEntity owner) {
        super(ModEntities.GOLEM_DART.get(), owner, level);
    }
    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}
