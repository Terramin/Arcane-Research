package arcaneresearch.common.lib;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import arcaneresearch.common.entities.projectile.EntityAlumentum;






public class BehaviorDispenseAlumetum extends DefaultDispenseItemBehavior {
    public static void register() {
        DispenserBlock.registerBehavior(arcaneresearch.init.ModItems.ALUMENTUM.get(), new BehaviorDispenseAlumetum());
    }

    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
        return new EntityAlumentum(level, position.x(), position.y(), position.z());
    }

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        ServerLevel level = source.level();
        Position dispensePos = DispenserBlock.getDispensePosition(source);
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Projectile projectile = getProjectile(level, dispensePos, stack);
        projectile.shoot(direction.getStepX(), direction.getStepY() + 0.1F, direction.getStepZ(), 1.1F, 6.0F);
        level.addFreshEntity(projectile);
        stack.shrink(1);
        return stack;
    }

    
    
    @Override
    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
