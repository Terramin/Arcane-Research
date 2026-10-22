package thaumictinker.kami.trinkets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import thaumictinker.util.TtCurios;

 


 
public class CatAmuletItem extends Item implements ICurioItem {

    public static final int RANGE = 10;
    public static final int RANGE_Y = 4;

    public CatAmuletItem(Properties properties) {
        super(properties);
    }

    public static boolean isWearing(LivingEntity entity) {
        return entity instanceof Player player
                && TtCurios.isEquipped(player, TrinketsFeature.CAT_AMULET);
    }

    public static boolean inRange(LivingEntity wearer, LivingEntity other) {
        return Math.abs(other.getX() - wearer.getX()) <= RANGE
                && Math.abs(other.getZ() - wearer.getZ()) <= RANGE
                && Math.abs(other.getY() - wearer.getY()) <= RANGE_Y;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("necklace");
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity wearer = slotContext.entity();
        if (!(wearer.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = new AABB(wearer.getX() - RANGE, wearer.getY() - RANGE_Y, wearer.getZ() - RANGE,
                wearer.getX() + RANGE, wearer.getY() + RANGE_Y, wearer.getZ() + RANGE);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
            if (mob instanceof Creeper creeper) {
                creeper.setTarget(null);
                creeper.setSwellDir(-1);
            } else if (mob.getTarget() == wearer) {
                mob.setTarget(null);
            }
        }
    }
}
