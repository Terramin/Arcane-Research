package arcaneresearch.common.lib;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import arcaneresearch.common.entities.projectile.EntityAlumentum;
public class BehaviorDispenseAlumetum extends AbstractProjectileDispenseBehavior {
    public static void register() {
        DispenserBlock.registerBehavior(arcaneresearch.init.ModItems.ALUMENTUM.get(), new BehaviorDispenseAlumetum());
    }
    @Override
    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
        return new EntityAlumentum(level, position.x(), position.y(), position.z());
    }
}
