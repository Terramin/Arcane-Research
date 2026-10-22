package thaumictinker.kami.tool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class AwakenedSwordEvents {

    public static final int AREA_RANGE = 3;

    private static boolean ignoreLeftClick;

    private AwakenedSwordEvents() {
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (ignoreLeftClick || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof AwakenedTool tool) || tool.kind() != ToolKind.SWORD) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity target) || target.hurtTime != 0 || !target.isAlive()) {
            return;
        }
        switch (ToolHandler.getMode(stack)) {
            case 1 -> attackArea(level, player, target);
            case 2 -> {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1, 1));
                SoulHeartHandler.addHearts(player);
            }
            default -> {
            }
        }
    }

    private static void attackArea(ServerLevel level, Player player, LivingEntity target) {
        AABB area = new AABB(
                target.getX() - AREA_RANGE, target.getY() - AREA_RANGE, target.getZ() - AREA_RANGE,
                target.getX() + AREA_RANGE, target.getY() + AREA_RANGE, target.getZ() + AREA_RANGE
        );
        ignoreLeftClick = true;
        try {
            for (Entity entity : level.getEntitiesOfClass(target.getClass(), area)) {
                if (entity != target && entity != player && entity.isAlive()) {
                    player.attack(entity);
                }
            }
        } finally {
            ignoreLeftClick = false;
        }
    }
}
