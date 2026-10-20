package arcaneresearch.common.entities.construct;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import arcaneresearch.init.ModEntities;
import java.util.List;
public class ItemTurretPlacer extends Item {
    private final TurretType turretType;
    public ItemTurretPlacer(TurretType type) {
        super(new Item.Properties().stacksTo(16));
        this.turretType = type;
    }
    public static ItemTurretPlacer createBasic() {
        return new ItemTurretPlacer(TurretType.BASIC_CROSSBOW);
    }
    public static ItemTurretPlacer createAdvanced() {
        return new ItemTurretPlacer(TurretType.ADVANCED_CROSSBOW);
    }
    public static ItemTurretPlacer createBore() {
        return new ItemTurretPlacer(TurretType.ARCANE_BORE);
    }
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction side = context.getClickedFace();
        ItemStack stack = context.getItemInHand();
        if (side == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        boolean replaceable = level.getBlockState(clickedPos).canBeReplaced();
        BlockPos placePos = replaceable ? clickedPos : clickedPos.relative(side);
        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(placePos, side, stack)) {
            return InteractionResult.PASS;
        }
        BlockPos upperPos = placePos.above();
        boolean canPlace = (level.isEmptyBlock(placePos) || level.getBlockState(placePos).canBeReplaced()) &&
                (level.isEmptyBlock(upperPos) || level.getBlockState(upperPos).canBeReplaced());
        if (!canPlace) {
            return InteractionResult.PASS;
        }
        double x = placePos.getX();
        double y = placePos.getY();
        double z = placePos.getZ();
        List<Entity> entities = level.getEntities(null, new AABB(x, y, z, x + 1, y + 2, z + 1));
        if (!entities.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.removeBlock(placePos, false);
            level.removeBlock(upperPos, false);
            EntityOwnedConstruct turret = createTurret(level, placePos, context);
            if (turret != null) {
                level.addFreshEntity(turret);
                turret.setOwned(true);
                turret.setValidSpawn();
                if (context.getPlayer() != null) {
                    turret.setOwnerUUID(context.getPlayer().getUUID());
                }
                level.playSound(null, turret.getX(), turret.getY(), turret.getZ(),
                        SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75f, 0.8f);
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    private EntityOwnedConstruct createTurret(Level level, BlockPos pos, UseOnContext context) {
        return switch (turretType) {
            case BASIC_CROSSBOW -> new EntityTurretCrossbow(
                    ModEntities.TURRET_CROSSBOW.get(), level, pos);
            case ADVANCED_CROSSBOW -> new EntityTurretCrossbowAdvanced(
                    ModEntities.TURRET_CROSSBOW_ADVANCED.get(), level, pos);
            case ARCANE_BORE -> {
                Direction facing = context.getPlayer() != null ?
                        context.getPlayer().getDirection() : Direction.NORTH;
                EntityArcaneBore bore = new EntityArcaneBore(ModEntities.ARCANE_BORE.get(), level);
                bore.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                bore.setFacing(facing);
                yield bore;
            }
        };
    }
    public enum TurretType {
        BASIC_CROSSBOW,
        ADVANCED_CROSSBOW,
        ARCANE_BORE
    }
}
