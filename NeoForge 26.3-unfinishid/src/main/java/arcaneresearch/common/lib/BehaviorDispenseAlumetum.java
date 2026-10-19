package arcaneresearch.common.lib;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import arcaneresearch.common.entities.projectile.EntityAlumentum;

// NEOFORGE_PORT: AbstractProjectileDispenseBehavior removed in 26.x; DispenseItemBehavior
// is now an interface with dispense(BlockSource, ItemStack). The dispense logic below
// replicates the old base 1:1 (dispense position = center + facing * 0.7, shoot with
// power 1.1 / uncertainty 6.0, spawn, shrink). getProjectile kept with the same signature.
public class BehaviorDispenseAlumetum implements DispenseItemBehavior {
    public static void register() {
        DispenserBlock.registerBehavior(arcaneresearch.init.ModItems.ALUMENTUM.get(), new BehaviorDispenseAlumetum());
    }

    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
        return new EntityAlumentum(level, position.x(), position.y(), position.z());
    }

    @Override
    public ItemStack dispense(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        BlockPos pos = source.pos();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        // NEOFORGE_PORT: DispenseItemBehavior.getDispensePosition removed; same math inline.
        // PositionImpl removed -> Vec3 implements Position.
        Position dispensePos = new net.minecraft.world.phys.Vec3(
                pos.getX() + 0.5 + 0.7 * direction.getStepX(),
                pos.getY() + 0.5 + 0.7 * direction.getStepY(),
                pos.getZ() + 0.5 + 0.7 * direction.getStepZ());
        Projectile projectile = getProjectile(level, dispensePos, stack);
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1F, direction.getStepZ(), 1.1F, 6.0F);
        level.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }
}
